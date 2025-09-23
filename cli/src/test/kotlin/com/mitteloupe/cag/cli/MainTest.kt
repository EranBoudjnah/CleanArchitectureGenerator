package com.mitteloupe.cag.cli

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Files

private const val SHORT_USAGE =
    "usage: cag [--new-project --name=ProjectName --package=PackageName [--no-compose] [--ktlint] [--detekt] [--ktor] [--retrofit]]... " +
        "[--new-architecture [--no-compose] [--ktlint] [--detekt]]... " +
        "[--new-feature --name=FeatureName [--package=PackageName] [--ktlint] [--detekt]]... " +
        "[--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... " +
        "[--new-use-case --name=UseCaseName [--path=TargetPath]]... " +
        "[--new-view-model --name=ViewModelName [--path=TargetPath]]..."

@RunWith(Enclosed::class)
@SuiteClasses(
    MainTest.NoArguments::class,
    MainTest.Help::class,
    MainTest.InvalidFlags::class,
    MainTest.GenerationException::class,
    MainTest.SuccessfulGeneration::class
)
class MainTest {
    abstract class BaseMainTest {
        private lateinit var originalOutput: PrintStream
        protected lateinit var output: OutputStream

        @Before
        fun setUp() {
            originalOutput = System.out
            output = ByteArrayOutputStream()
            System.setOut(PrintStream(output))
        }

        @After
        fun tearDown() {
            System.setOut(originalOutput)
        }

        protected fun runMainInSeparateProcess(
            arguments: Array<String>,
            workingDirectory: File = Files.createTempDirectory("cag-cli-test").toFile()
        ): Int {
            val processBuilder =
                ProcessBuilder(
                    System.getProperty("java.home") + "/bin/java",
                    "-classpath",
                    System.getProperty("java.class.path"),
                    "com.mitteloupe.cag.cli.MainKt"
                )
            processBuilder.directory(workingDirectory)
            processBuilder.command().addAll(arguments.toList())
            return processBuilder.start().waitFor()
        }
    }

    class NoArguments : BaseMainTest() {
        @Test
        fun `Given no args when main then prints updated usage`() {
            // Give
            val expected =
                "$SHORT_USAGE\n\n" +
                    "Run with --help or -h for more options.\n"

            // When
            main(emptyArray())

            // Then
            assertEquals(expected, output.toString())
        }
    }

    class Help : BaseMainTest() {
        @Test
        fun `Given --help when main then prints help document`() {
            // When
            main(arrayOf("--help"))

            // Then
            assertEquals(EXPECTED_HELP, output.toString())
        }

        @Test
        fun `Given -h when main then prints help document`() {
            // When
            main(arrayOf("-h"))

            // Then
            assertEquals(EXPECTED_HELP, output.toString())
        }

        companion object {
            private const val EXPECTED_HELP =
                """$SHORT_USAGE

Note: You must use either long form (--flag) or short form (-f) arguments consistently throughout your command. Mixing both forms is not allowed.

Options:
  --new-project | -np
      Generate a complete Clean Architecture project template
    --name=ProjectName | -n=ProjectName | -n ProjectName | -nProjectName
        Specify the project name (required)
    --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
        Specify the package name (required)
    --no-compose | -nc
      Disable Compose support for the project
    --ktlint | -kl
      Enable ktlint for the project
    --detekt | -d
      Enable detekt for the project
    --ktor | -kt
      Enable Ktor for data sources
    --retrofit | -rt
      Enable Retrofit for data sources
  --new-architecture | -na
      Generate a new Clean Architecture package with domain, presentation, and UI layers
    --no-compose | -nc
      Disable Compose support for the preceding architecture package
    --ktlint | -kl
      Enable ktlint for the preceding architecture package
    --detekt | -d
      Enable detekt for the preceding architecture package
  --new-feature | -nf
      Generate a new feature
    --name=FeatureName | -n=FeatureName | -n FeatureName | -nFeatureName
        Specify the feature name (required)
    --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
        Override the feature package for the preceding feature
    --ktlint | -kl
      Enable ktlint for the preceding feature (adds plugin and .editorconfig if missing)
    --detekt | -d
      Enable detekt for the preceding feature (adds plugin and detekt.yml if missing)
  --new-datasource | -nds
      Generate a new data source
    --name=DataSourceName | -n=DataSourceName | -n DataSourceName | -nDataSourceName
        Specify the data source name (required, DataSource suffix will be added automatically)
    --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
        Attach dependencies to the preceding new data source
  --new-use-case | -nuc
      Generate a new use case
    --name=UseCaseName | -n=UseCaseName | -n UseCaseName | -nUseCaseName
        Specify the use case name (required)
    --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
        Specify the target directory for the preceding use case
  --new-view-model | -nvm
      Generate a new ViewModel
    --name=ViewModelName | -n=ViewModelName | -n ViewModelName | -nViewModelName
        Specify the ViewModel name (required)
    --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
        Specify the target directory for the preceding ViewModel
  --help, -h
      Show this help message and exit
"""
        }
    }

    class InvalidFlags : BaseMainTest() {
        @Test
        fun `Given invalid flags when main then process exits with code 1`() {
            // When
            val exitCode = runMainInSeparateProcess(arrayOf("--invalid-flag"))

            // Then
            assertEquals(1, exitCode)
        }
    }

    class GenerationException : BaseMainTest() {
        @Test
        fun `Given generation exception when main then process exits with code 1`() {
            // Given
            val temporaryDirectory = Files.createTempDirectory("cag-cli-test-").toFile()
            val existingProjectDirectory = File(temporaryDirectory, "TestProject")
            existingProjectDirectory.mkdirs()

            // When
            val exitCode =
                runMainInSeparateProcess(
                    arrayOf("--new-project", "--name=TestProject", "--package=com.test"),
                    temporaryDirectory
                )

            // Then
            assertEquals(1, exitCode)
        }
    }

    class SuccessfulGeneration : BaseMainTest() {
        @Test
        fun `Given valid arguments when main then prints Done!`() {
            // When
            main(arrayOf("--help"))

            // Then
            assertThat(output.toString(), containsString("usage: cag"))
        }
    }
}
