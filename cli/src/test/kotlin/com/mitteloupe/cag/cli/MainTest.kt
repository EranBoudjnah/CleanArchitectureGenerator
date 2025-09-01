package com.mitteloupe.cag.cli

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

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

class MainTest : BaseMainTest() {
    @Test
    fun `Given no args when main then prints updated usage`() {
        // When
        main(emptyArray())

        // Then
        assertEquals(
            "usage: cag [--new-feature=FeatureName [--package=PackageName]]... " +
                "[--new-datasource=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... " +
                "[--new-use-case=UseCaseName [--path=TargetPath]]...\n" +
                "Run with --help or -h for more options.\n",
            output.toString()
        )
    }
}

class MainHelpTest : BaseMainTest() {
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
            """usage: cag [--new-feature=FeatureName [--package=PackageName]]... [--new-datasource=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case=UseCaseName [--path=TargetPath]]...

Options:
  --new-feature=FeatureName | --new-feature FeatureName | -nf=FeatureName | -nf FeatureName | -nfFeatureName
    Generate a new feature named FeatureName
  --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
    Override the feature package for the preceding feature
  --new-datasource=Name | --new-datasource Name | -nds=Name | -nds Name | -ndsName
    Generate a new data source named NameDataSource
  --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
    Attach dependencies to the preceding new data source
  --new-use-case=UseCaseName | --new-use-case UseCaseName | -nuc=UseCaseName | -nuc UseCaseName | -nucUseCaseName
    Generate a new use case named UseCaseName
  --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
    Specify the target directory for the preceding use case
  --help, -h
    Show this help message and exit
"""
    }
}
