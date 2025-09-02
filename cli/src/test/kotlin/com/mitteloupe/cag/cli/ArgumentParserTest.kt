package com.mitteloupe.cag.cli

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ArgumentParserTest {
    private lateinit var classUnderTest: ArgumentParser

    @Before
    fun setUp() {
        classUnderTest = ArgumentParser()
    }

    @Test
    fun `Given no arguments when parsePrimaryWithSecondaries then returns empty list`() {
        // Given
        val givenArguments = emptyArray<String>()

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryLong = "--alpha",
                primaryShort = "-a",
                secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
            )

        // Then
        assertEquals(emptyList<Map<String, String>>(), result)
    }

    @Test
    fun `Given valueless primary with secondaries when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=x", "-g", "y", "--alpha", "-b", "z")

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryLong = "--alpha",
                primaryShort = "-a",
                secondaryFlags = listOf(SecondaryFlag("--beta", "-b"), SecondaryFlag("--gamma", "-g"))
            )

        // Then
        assertEquals(
            listOf(
                mapOf("--beta" to "x", "--gamma" to "y"),
                mapOf("--beta" to "z")
            ),
            result
        )
    }

    @Test
    fun `Given valueless primary with equals syntax when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=x", "-g=y", "--alpha", "-b=z")

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryLong = "--alpha",
                primaryShort = "-a",
                secondaryFlags = listOf(SecondaryFlag("--beta", "-b"), SecondaryFlag("--gamma", "-g"))
            )

        // Then
        assertEquals(
            listOf(
                mapOf("--beta" to "x", "--gamma" to "y"),
                mapOf("--beta" to "z")
            ),
            result
        )
    }

    @Test
    fun `Given mandatory flag is provided when parsePrimaryWithSecondaries then succeeds`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=x")

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryLong = "--alpha",
                primaryShort = "-a",
                secondaryFlags =
                    listOf(
                        SecondaryFlag(
                            long = "--beta",
                            short = "-b",
                            isMandatory = true,
                            missingErrorMessage = "Beta is required"
                        )
                    )
            )

        // Then
        assertEquals(
            listOf(mapOf("--beta" to "x")),
            result
        )
    }

    @Test
    fun `Given mandatory flag is missing when parsePrimaryWithSecondaries then throws exception`() {
        // Given
        val givenArguments = arrayOf("--alpha")

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryLong = "--alpha",
                primaryShort = "-a",
                secondaryFlags =
                    listOf(
                        SecondaryFlag(
                            long = "--beta",
                            short = "-b",
                            isMandatory = true,
                            missingErrorMessage = "Beta is required"
                        )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            // Then
            assertEquals("Beta is required", e.message)
        }
    }

    @Test
    fun `Given multiple mandatory flags when parsePrimaryWithSecondaries then validates all`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=x")

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryLong = "--alpha",
                primaryShort = "-a",
                secondaryFlags =
                    listOf(
                        SecondaryFlag(
                            long = "--beta",
                            short = "-b",
                            isMandatory = true,
                            missingErrorMessage = "Beta is required"
                        ),
                        SecondaryFlag(
                            long = "--gamma",
                            short = "-g",
                            isMandatory = true,
                            missingErrorMessage = "Gamma is required"
                        )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            // Then
            assertEquals("Gamma is required", e.message)
        }
    }

    @Test
    fun `Given mandatory flag with empty value when parsePrimaryWithSecondaries then throws exception`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=")

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryLong = "--alpha",
                primaryShort = "-a",
                secondaryFlags =
                    listOf(
                        SecondaryFlag(
                            long = "--beta",
                            short = "-b",
                            isMandatory = true,
                            missingErrorMessage = "Beta is required"
                        )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            // Then
            assertEquals("Beta is required", e.message)
        }
    }

    @Test
    fun `Given mandatory flag with default error message when parsePrimaryWithSecondaries then uses default message`() {
        // Given
        val givenArguments = arrayOf("--alpha")

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryLong = "--alpha",
                primaryShort = "-a",
                secondaryFlags =
                    listOf(
                        SecondaryFlag(
                            long = "--beta",
                            short = "-b",
                            isMandatory = true
                        )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            // Then
            assertEquals("Missing mandatory flag: --beta", e.message)
        }
    }
}
