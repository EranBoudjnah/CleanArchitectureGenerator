package com.mitteloupe.cag.cli

import org.junit.Assert.assertEquals
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
        assertEquals(emptyList<Pair<String, Map<String, String>>>(), result)
    }

    @Test
    fun `Given primaries, secondaries when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val givenArguments = arrayOf("--alpha=One", "--beta=x", "-g", "y", "-aTwo", "-b", "z")

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
                "One" to mapOf("--beta" to "x", "--gamma" to "y"),
                "Two" to mapOf("--beta" to "z")
            ),
            result
        )
    }

    @Test
    fun `Given unknown primary when parsePrimaryWithSecondaries then groups correctly`() {
        // Given
        val givenArguments = arrayOf("--alpha=One", "--beta=x", "-g", "y", "--delta", "--beta=x", "--alpha", "Two")

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
                "One" to mapOf("--beta" to "x", "--gamma" to "y"),
                "Two" to emptyMap()
            ),
            result
        )
    }
}
