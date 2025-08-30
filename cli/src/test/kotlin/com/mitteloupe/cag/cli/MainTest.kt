package com.mitteloupe.cag.cli

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

class MainTest {
    private lateinit var originalOutput: PrintStream
    private lateinit var output: OutputStream

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

    @Test
    fun `Given no args when main then prints updated usage`() {
        // When
        main(emptyArray())

        // Then
        assertEquals(
            "usage: cag [--new-feature=FeatureName]... [--new-datasource=DataSourceName]...\n",
            output.toString()
        )
    }
}
