package com.mitteloupe.cag.cli.argument

import com.mitteloupe.cag.cli.flag.FlagOption
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
        secondaryFlags: Set<SecondaryFlag>
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
        val secondaryOption = FlagOption("--beta", "-b")
        val expectedParsedArguments = emptyList<Map<FlagOption, String>>()

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = setOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given valueless primary with secondaries when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val secondaryOption1 = FlagOption("--beta", "-b")
        val secondaryOption2 = FlagOption("--gamma", "-g")
        val primaryLong = "--primary"
        val givenArguments =
            arrayOf(primaryLong, "${secondaryOption1.long}=x", secondaryOption2.long, "y", primaryLong, secondaryOption1.long, "z")
        val expectedParsedArguments =
            listOf(
                mapOf(secondaryOption1 to "x", secondaryOption2 to "y"),
                mapOf(secondaryOption1 to "z")
            )

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-p",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption1), SecondaryFlag(secondaryOption2))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given valueless primary with equals syntax when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val primaryLong = "--alpha"
        val secondaryOption1 = FlagOption("--beta", "-b")
        val secondaryOption2 = FlagOption("--gamma", "-g")
        val givenArguments =
            arrayOf(primaryLong, "${secondaryOption1.long}=x", "${secondaryOption2.long}=y", primaryLong, "${secondaryOption1.long}=z")
        val expectedParsedArguments =
            listOf(
                mapOf(secondaryOption1 to "x", secondaryOption2 to "y"),
                mapOf(secondaryOption1 to "z")
            )

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption1), SecondaryFlag(secondaryOption2))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given mandatory flag is provided when parsePrimaryWithSecondaries then succeeds`() {
        // Given
        val secondaryOption = FlagOption(long = "--beta", short = "-b")
        val primaryLong = "--alpha"
        val secondaryValue = "x"
        val givenArguments = arrayOf(primaryLong, "${secondaryOption.long}=$secondaryValue")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to secondaryValue))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(
                                    option = secondaryOption,
                                    isMandatory = true,
                                    missingErrorMessage = "Beta is required"
                                )
                            )
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given mandatory flag is missing when parsePrimaryWithSecondaries then throws exception`() {
        // Given
        val givenArguments = arrayOf("--alpha")
        val expectedErrorMessage = "Beta is required"

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(
                                    FlagOption(long = "--beta", short = "-b"),
                                    isMandatory = true,
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
    fun `Given multiple mandatory flags when parsePrimaryWithSecondaries then validates all`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=x")
        val expectedErrorMessage = "Gamma is required"

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(
                                    FlagOption(long = "--beta", short = "-b"),
                                    isMandatory = true,
                                    missingErrorMessage = "Beta is required"
                                ),
                                SecondaryFlag(
                                    FlagOption(long = "--gamma", short = "-g"),
                                    isMandatory = true,
                                    missingErrorMessage = expectedErrorMessage
                                )
                            )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            // Then
            assertEquals(expectedErrorMessage, e.message)
        }
    }

    @Test
    fun `Given mandatory flag with empty value when parsePrimaryWithSecondaries then throws exception`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=")
        val expectedErrorMessage = "Beta is required"

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(
                                    FlagOption(long = "--beta", short = "-b"),
                                    isMandatory = true,
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
    fun `Given mandatory flag with default error message when parsePrimaryWithSecondaries then uses default message`() {
        // Given
        val givenArguments = arrayOf("--alpha")
        val expectedErrorMessage = "Missing mandatory flag: --beta"

        // When
        try {
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(
                                    FlagOption(long = "--beta", short = "-b"),
                                    isMandatory = true
                                )
                            )
                    )
            )
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            // Then
            assertEquals(expectedErrorMessage, e.message)
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
                        secondaryFlags = linkedSetOf(SecondaryFlag(FlagOption("--beta", "-b")))
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
                        secondaryFlags = linkedSetOf(SecondaryFlag(FlagOption("--beta", "-b")))
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
        val secondaryOption = FlagOption("--beta", "-b")
        val primaryLong = "--alpha"
        val givenArguments = arrayOf(primaryLong, secondaryOption.long, "value")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given short primary with matching short secondaries when parsePrimaryWithSecondaries then processes correctly`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val primaryShort = "-a"
        val secondaryValue = "value"
        val givenArguments = arrayOf(primaryShort, secondaryOption.short, secondaryValue)
        val expectedParsedArguments = listOf(mapOf(secondaryOption to secondaryValue))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = primaryShort,
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given primary without secondaries when parsePrimaryWithSecondaries then returns empty map`() {
        // Given
        val givenArguments = arrayOf("--alpha")
        val expectedParsedArguments = listOf(emptyMap<FlagOption, String>())

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(FlagOption("--beta", "-b")))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given boolean flag when parsePrimaryWithSecondaries then sets empty value`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val givenArguments = arrayOf("--alpha", secondaryOption.long)
        val expectedParsedArguments = listOf(mapOf(secondaryOption to ""))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption, isBoolean = true))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given boolean flag with value when parsePrimaryWithSecondaries then ignores value`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val primaryLong = "--alpha"
        val givenArguments = arrayOf(primaryLong, secondaryOption.long, "ignored")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to ""))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption, isBoolean = true))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given short form boolean flag when parsePrimaryWithSecondaries then sets empty value`() {
        // Given
        val givenArguments = arrayOf("-a", "-b")
        val secondaryOption = FlagOption("--beta", "-b")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to ""))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption, isBoolean = true))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given inline long form argument when parsePrimaryWithSecondaries then parses correctly`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val secondaryValue = "value"
        val primaryLong = "--alpha"
        val givenArguments = arrayOf(primaryLong, "${secondaryOption.long}=$secondaryValue")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to secondaryValue))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given inline short form argument when parsePrimaryWithSecondaries then parses correctly`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val primaryShort = "-a"
        val givenArguments = arrayOf(primaryShort, "${secondaryOption.short}value")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = primaryShort,
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given inline short form with equals when parsePrimaryWithSecondaries then parses correctly`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val primaryShort = "-a"
        val givenArguments = arrayOf(primaryShort, "${secondaryOption.short}=value")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = primaryShort,
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given empty inline value when parsePrimaryWithSecondaries then ignores flag`() {
        // Given
        val givenArguments = arrayOf("--alpha", "--beta=")
        val expectedParsedArguments = emptyList<Map<FlagOption, String>>()

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(FlagOption("--beta", "-b")))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given whitespace in values when parsePrimaryWithSecondaries then trims values`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val primaryLong = "--alpha"
        val givenArguments = arrayOf(primaryLong, secondaryOption.long, "  value  ")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given special characters in values when parsePrimaryWithSecondaries then preserves values`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val secondaryValue = "value-with-special.chars@123"
        val primaryLong = "--alpha"
        val givenArguments = arrayOf(primaryLong, secondaryOption.long, secondaryValue)
        val expectedParsedArguments = listOf(mapOf(secondaryOption to secondaryValue))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given multiple primaries with mixed secondaries when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val primaryLong = "--alpha"
        val secondaryOption1 = FlagOption("--beta", "-b")
        val secondaryOption2 = FlagOption("--gamma", "-g")
        val secondaryValue1 = "x"
        val secondaryValue2 = "y"
        val givenArguments =
            arrayOf(primaryLong, secondaryOption1.long, secondaryValue1, primaryLong, secondaryOption2.long, secondaryValue2, primaryLong)
        val expectedParsedArguments =
            listOf(
                mapOf(secondaryOption1 to secondaryValue1),
                mapOf(secondaryOption2 to secondaryValue2),
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
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption1), SecondaryFlag(secondaryOption2))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given unknown flags when parsePrimaryWithSecondaries then ignores unknown flags`() {
        // Given
        val secondaryOption = FlagOption("--beta", "-b")
        val givenArguments = arrayOf("--alpha", "--unknown", "value", secondaryOption.long, "x")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to "x"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given flags without primary when parsePrimaryWithSecondaries then returns empty list`() {
        // Given
        val givenArguments = arrayOf("--beta", "value", "--gamma", "other")
        val expectedParsedArguments = emptyList<Map<FlagOption, String>>()

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(FlagOption("--beta", "-b")),
                                SecondaryFlag(FlagOption("--gamma", "-g"))
                            )
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }

    @Test
    fun `Given multiple mandatory flags when all provided when parsePrimaryWithSecondaries then succeeds`() {
        // Given
        val secondaryOption1 = FlagOption("--beta", "-b")
        val secondaryOption2 = FlagOption("--gamma", "-g")
        val givenArguments = arrayOf("--alpha", secondaryOption1.long, "x", secondaryOption2.long, "y")
        val expectedParsedArguments = listOf(mapOf(secondaryOption1 to "x", secondaryOption2 to "y"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = "--alpha",
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(
                                    secondaryOption1,
                                    isMandatory = true,
                                    missingErrorMessage = "Beta required"
                                ),
                                SecondaryFlag(
                                    secondaryOption2,
                                    isMandatory = true,
                                    missingErrorMessage = "Gamma required"
                                )
                            )
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
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
                            linkedSetOf(
                                SecondaryFlag(
                                    FlagOption(long = "--beta", short = "-b"),
                                    isMandatory = true,
                                    missingErrorMessage = "Beta required"
                                ),
                                SecondaryFlag(
                                    FlagOption(long = "--gamma", short = "-g"),
                                    isMandatory = true,
                                    missingErrorMessage = expectedFirstErrorMessage
                                ),
                                SecondaryFlag(
                                    FlagOption(long = "--delta", short = "-d"),
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
        val secondaryOption = FlagOption(long = "--beta", short = "-b")
        val primaryLong = "--alpha"
        val givenArguments = arrayOf(primaryLong, secondaryOption.long)
        val expectedParsedArguments = listOf(mapOf(secondaryOption to ""))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(
                                    secondaryOption,
                                    isMandatory = true,
                                    isBoolean = true,
                                    missingErrorMessage = "Beta required"
                                )
                            )
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
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
                            linkedSetOf(
                                SecondaryFlag(
                                    FlagOption(long = "--beta", short = "-b"),
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
        val secondaryOption1 = FlagOption("--beta", "-b")
        val secondaryOption2 = FlagOption("--gamma", "-g")
        val secondaryOption3 = FlagOption("--delta", "-d")
        val primaryLong = "--alpha"
        val givenArguments =
            arrayOf(
                primaryLong,
                "${secondaryOption1.long}=inline",
                secondaryOption2.long,
                "separate",
                secondaryOption3.long,
                primaryLong,
                secondaryOption1.long,
                "separate",
                "${secondaryOption2.long}=inline"
            )
        val expectedParsedArguments =
            listOf(
                mapOf(secondaryOption1 to "inline", secondaryOption2 to "separate", secondaryOption3 to ""),
                mapOf(secondaryOption1 to "separate", secondaryOption2 to "inline")
            )

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags =
                            linkedSetOf(
                                SecondaryFlag(secondaryOption1),
                                SecondaryFlag(secondaryOption2),
                                SecondaryFlag(secondaryOption3, isBoolean = true)
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
                            linkedSetOf(
                                SecondaryFlag(
                                    FlagOption(long = "--beta", short = "-b"),
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
                            linkedSetOf(
                                SecondaryFlag(
                                    FlagOption(long = "--beta", short = "-b"),
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
        val secondaryOption = FlagOption("--beta", "-b")
        val primaryLong = "--alpha"
        val givenArguments = arrayOf(primaryLong, "--invalid", secondaryOption.long, "value", "-unknown", "test")
        val expectedParsedArguments = listOf(mapOf(secondaryOption to "value"))

        // When
        val result =
            classUnderTest.parsePrimaryWithSecondaries(
                arguments = givenArguments,
                primaryFlag =
                    createPrimaryFlag(
                        long = primaryLong,
                        short = "-a",
                        secondaryFlags = linkedSetOf(SecondaryFlag(secondaryOption))
                    )
            )

        // Then
        assertEquals(expectedParsedArguments, result)
    }
}
