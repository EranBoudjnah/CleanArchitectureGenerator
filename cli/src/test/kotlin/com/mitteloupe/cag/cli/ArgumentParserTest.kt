package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.flag.PrimaryFlag
import com.mitteloupe.cag.cli.flag.SecondaryFlag
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

    private fun createPrimaryFlag(
        long: String,
        short: String,
        secondaryFlags: List<SecondaryFlag>
    ): PrimaryFlag =
        object : PrimaryFlag {
            override val long = long
            override val short = short
            override val secondaryFlags = secondaryFlags
        }

    @Test
    fun `Given no arguments when parsePrimaryWithSecondaries then returns empty list`() {
        // Given
        val givenArguments = emptyArray<String>()

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag = createPrimaryFlag("--alpha", "-a", listOf(SecondaryFlag("--beta", "-b")))
            )

        // Then
        assertEquals(emptyList<Map<String, String>>(), result)
    }

    @Test
    fun `Given valueless primary with secondaries when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=x", "--gamma", "y", "--alpha", "--beta", "z")

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"), SecondaryFlag("--gamma", "-g"))
                    )
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
        val givenArguments = arrayOf("--alpha", "--beta=x", "--gamma=y", "--alpha", "--beta=z")

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"), SecondaryFlag("--gamma", "-g"))
                    )
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
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
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
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
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
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (exception: IllegalArgumentException) {
            // Then
            assertEquals("Beta is required", exception.message)
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
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
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
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
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
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (exception: IllegalArgumentException) {
            // Then
            assertEquals("Beta is required", exception.message)
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
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            listOf(
                                SecondaryFlag(
                                    long = "--beta",
                                    short = "-b",
                                    isMandatory = true
                                )
                            )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            // Then
            assertEquals("Missing mandatory flag: --beta", e.message)
        }
    }

    @Test
    fun `Given long primary with short secondaries when parsePrimaryWithSecondaries then throws informative exception`() {
        // Given
        val givenArguments = arrayOf("--alpha", "-b", "value")
        val expectedErrorMessage =
            "Cannot mix long form (--alpha) with short form secondary flags (-b). Use --beta instead."

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (exception: IllegalArgumentException) {
            // Then
            assertEquals(expectedErrorMessage, exception.message)
        }
    }

    @Test
    fun `Given short primary with long secondaries when parsePrimaryWithSecondaries then throws informative exception`() {
        // Given
        val givenArguments = arrayOf("-a", "--beta", "value")
        val expectedErrorMessage = "Cannot mix short form (-a) with long form secondary flags (--beta). Use -b instead."

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (exception: IllegalArgumentException) {
            // Then
            assertEquals(expectedErrorMessage, exception.message)
        }
    }

    @Test
    fun `Given long primary with matching long secondaries when parsePrimaryWithSecondaries then processes correctly`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta", "value")
        val expectedParsedArguments = listOf(mapOf("--beta" to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given short primary with matching short secondaries when parsePrimaryWithSecondaries then processes correctly`() {
        // Given
        val givenArguments = arrayOf("-a", "-b", "value")
        val expectedParsedArguments = listOf(mapOf("--beta" to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given primary without secondaries when parsePrimaryWithSecondaries then returns empty map`() {
        // Given
        val givenArguments = arrayOf("--alpha")
        val expectedParsedArguments = listOf(emptyMap<String, String>())

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given boolean flag when parsePrimaryWithSecondaries then sets empty value`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta")
        val expectedParsedArguments = listOf(mapOf("--beta" to ""))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b", isBoolean = true))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given boolean flag with value when parsePrimaryWithSecondaries then ignores value`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta", "ignored")
        val expectedParsedArguments = listOf(mapOf("--beta" to ""))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b", isBoolean = true))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given short form boolean flag when parsePrimaryWithSecondaries then sets empty value`() {
        // Given
        val givenArguments = arrayOf("-a", "-b")
        val expectedParsedArguments = listOf(mapOf("--beta" to ""))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b", isBoolean = true))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given inline long form argument when parsePrimaryWithSecondaries then parses correctly`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=value")
        val expectedParsedArguments = listOf(mapOf("--beta" to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given inline short form argument when parsePrimaryWithSecondaries then parses correctly`() {
        // Given
        val givenArguments = arrayOf("-a", "-bvalue")
        val expectedParsedArguments = listOf(mapOf("--beta" to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given inline short form with equals when parsePrimaryWithSecondaries then parses correctly`() {
        // Given
        val givenArguments = arrayOf("-a", "-b=value")
        val expectedParsedArguments = listOf(mapOf("--beta" to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given empty inline value when parsePrimaryWithSecondaries then ignores flag`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=")
        val expectedParsedArguments = emptyList<Map<String, String>>()

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given whitespace in values when parsePrimaryWithSecondaries then trims values`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta", "  value  ")
        val expectedParsedArguments = listOf(mapOf("--beta" to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given special characters in values when parsePrimaryWithSecondaries then preserves values`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta", "value-with-special.chars@123")
        val expectedParsedArguments = listOf(mapOf("--beta" to "value-with-special.chars@123"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given multiple primaries with mixed secondaries when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta", "x", "--alpha", "--gamma", "y", "--alpha")
        val expectedParsedArguments =
            listOf(
                mapOf("--beta" to "x"),
                mapOf("--gamma" to "y"),
                emptyMap()
            )

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"), SecondaryFlag("--gamma", "-g"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given unknown flags when parsePrimaryWithSecondaries then ignores unknown flags`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--unknown", "value", "--beta", "x")
        val expectedParsedArguments = listOf(mapOf("--beta" to "x"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given flags without primary when parsePrimaryWithSecondaries then returns empty list`() {
        // Given
        val givenArguments = arrayOf("--beta", "value", "--gamma", "other")
        val expectedParsedArguments = emptyList<Map<String, String>>()

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"), SecondaryFlag("--gamma", "-g"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given multiple mandatory flags when all provided when parsePrimaryWithSecondaries then succeeds`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta", "x", "--gamma", "y")
        val expectedParsedArguments = listOf(mapOf("--beta" to "x", "--gamma" to "y"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            listOf(
                                SecondaryFlag(
                                    "--beta",
                                    "-b",
                                    isMandatory = true,
                                    missingErrorMessage = "Beta required"
                                ),
                                SecondaryFlag(
                                    "--gamma",
                                    "-g",
                                    isMandatory = true,
                                    missingErrorMessage = "Gamma required"
                                )
                            )
                    )
            )

        // Then
        assertEquals(
            expectedParsedArguments,
            result
        )
    }

    @Test
    fun `Given multiple mandatory flags when some missing when parsePrimaryWithSecondaries then throws with all missing`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta", "x")
        val expectedFirstErrorMessage = "Gamma required"
        val expectedSecondErrorMessage = "Delta required"
        val expectedMessage = "$expectedFirstErrorMessage\n$expectedSecondErrorMessage"

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            listOf(
                                SecondaryFlag(
                                    long = "--beta",
                                    short = "-b",
                                    isMandatory = true,
                                    missingErrorMessage = "Beta required"
                                ),
                                SecondaryFlag(
                                    long = "--gamma",
                                    short = "-g",
                                    isMandatory = true,
                                    missingErrorMessage = expectedFirstErrorMessage
                                ),
                                SecondaryFlag(
                                    long = "--delta",
                                    short = "-d",
                                    isMandatory = true,
                                    missingErrorMessage = expectedSecondErrorMessage
                                )
                            )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            // Then
            assertEquals(expectedMessage, e.message)
        }
    }

    @Test
    fun `Given boolean mandatory flag when provided when parsePrimaryWithSecondaries then succeeds`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta")
        val expectedParsedArguments = listOf(mapOf("--beta" to ""))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            listOf(
                                SecondaryFlag(
                                    long = "--beta",
                                    short = "-b",
                                    isMandatory = true,
                                    isBoolean = true,
                                    missingErrorMessage = "Beta required"
                                )
                            )
                    )
            )

        // Then
        assertEquals(
            expectedParsedArguments,
            result
        )
    }

    @Test
    fun `Given boolean mandatory flag when missing when parsePrimaryWithSecondaries then throws exception`() {
        // Given
        val givenArguments = arrayOf("--alpha")
        val expectedErrorMessage = "Beta required"

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            listOf(
                                SecondaryFlag(
                                    long = "--beta",
                                    short = "-b",
                                    isMandatory = true,
                                    isBoolean = true,
                                    missingErrorMessage = expectedErrorMessage
                                )
                            )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (exception: IllegalArgumentException) {
            // Then
            assertEquals(expectedErrorMessage, exception.message)
        }
    }

    @Test
    fun `Given complex mixed scenario when parsePrimaryWithSecondaries then handles correctly`() {
        // Given
        val givenArguments =
            arrayOf(
                "--alpha",
                "--beta=inline",
                "--gamma",
                "separate",
                "--delta",
                "--alpha",
                "--beta",
                "separate",
                "--gamma=inline"
            )
        val expectedParsedArguments =
            listOf(
                mapOf("--beta" to "inline", "--gamma" to "separate", "--delta" to ""),
                mapOf("--beta" to "separate", "--gamma" to "inline")
            )

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            listOf(
                                SecondaryFlag("--beta", "-b"),
                                SecondaryFlag("--gamma", "-g"),
                                SecondaryFlag("--delta", "-d", isBoolean = true)
                            )
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given long primary with short secondary and missing mandatory name when parsePrimaryWithSecondaries then throws mixed form exception`() {
        // Given
        val givenArguments = arrayOf("--alpha", "-b", "value")
        val expectedErrorMessage =
            "Cannot mix long form (--alpha) with short form secondary flags (-b). Use --beta instead."

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
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
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (exception: IllegalArgumentException) {
            // Then
            assertEquals(expectedErrorMessage, exception.message)
        }
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given short primary with long secondary and missing mandatory name when parsePrimaryWithSecondaries then throws mixed form exception`() {
        // Given
        val givenArguments = arrayOf("-a", "--beta", "value")
        val expectedErrorMessage = "Cannot mix short form (-a) with long form secondary flags (--beta). Use -b instead."

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
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
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (exception: IllegalArgumentException) {
            // Then
            assertEquals(expectedErrorMessage, exception.message)
        }
    }

    @Test
    fun `Given mixed invalid and valid flags when parsePrimaryWithSecondaries then ignores invalid flags`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--invalid", "--beta", "value", "-unknown", "test")
        val expectedParsedArguments = listOf(mapOf("--beta" to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = listOf(SecondaryFlag("--beta", "-b"))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }
}
