package com.mitteloupe.cag.cli

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

@RunWith(Suite::class)
@SuiteClasses(
    MainTest.NoArgsTest::class,
    MainTest.HelpTest::class
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
    }

    class NoArgsTest : BaseMainTest() {
        @Test
        fun `Given no args when main then prints updated usage`() {
            // When
            main(emptyArray())

            // Then
            assertEquals(
                "usage: cag [--new-architecture [--no-compose]]... " +
                    "[--new-feature --name=FeatureName [--package=PackageName]]... " +
                    "[--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... " +
                    "[--new-use-case --name=UseCaseName [--path=TargetPath]]...\n" +
                    "\n" +
                    "Run with --help or -h for more options.\n",
                output.toString()
            )
        }
    }

    class HelpTest : BaseMainTest() {
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
                """usage: cag [--new-architecture [--no-compose]]... [--new-feature --name=FeatureName [--package=PackageName]]... [--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case --name=UseCaseName [--path=TargetPath]]...


Options:
  --new-architecture | -na
      Generate a new Clean Architecture package with domain, presentation, and UI layers
    --no-compose | -nc
      Disable Compose support for the preceding architecture package
  --new-feature | -nf
      Generate a new feature
    --name=FeatureName | -n=FeatureName
        Specify the feature name (required)
    --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
        Override the feature package for the preceding feature
  --new-datasource | -nds
      Generate a new data source
    --name=DataSourceName | -n=DataSourceName
        Specify the data source name (required, DataSource suffix will be added automatically)
    --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
        Attach dependencies to the preceding new data source
  --new-use-case | -nuc
      Generate a new use case
    --name=UseCaseName | -n=UseCaseName
        Specify the use case name (required)
    --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
        Specify the target directory for the preceding use case
  --help, -h
      Show this help message and exit
"""
        }
    }
}
