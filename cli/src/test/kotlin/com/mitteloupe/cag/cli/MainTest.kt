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
            "usage: cag [--new-feature=FeatureName]... [--new-datasource=DataSourceName]...\nRun with --help or -h for more options.\n",
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
            """usage: cag [--new-feature=FeatureName]... [--new-datasource=DataSourceName]...

Options:
  --new-feature=FeatureName, -nf=FeatureName    Generate a new feature named FeatureName
  --new-datasource=Name, -nds=Name              Generate a new data source named NameDataSource
  --help, -h                                    Show this help message and exit
"""
    }
}
