package com.mitteloupe.cag.cleanarchitecturegenerator.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import java.io.File

@RunWith(Suite::class)
@SuiteClasses(
    SymbolValidatorTest.BasicTests::class,
    SymbolValidatorTest.IsValidSymbolSyntaxParameterizedTests::class,
    SymbolValidatorTest.IsValidSymbolInContextParameterizedTests::class
)
class SymbolValidatorTest {
    class BasicTests {
        private lateinit var classUnderTest: SymbolValidator

        @Before
        fun setUp() {
            classUnderTest = SymbolValidator()
        }

        @Test
        fun `Given empty string when isValidSymbolSyntax then returns false`() {
            // Given
            val givenSymbol = ""

            // When
            val actualResult = classUnderTest.isValidSymbolSyntax(givenSymbol)

            // Then
            assertFalse(actualResult)
        }

        @Test
        fun `Given nested generic types when isValidSymbolSyntax then handles correctly`() {
            // Given
            val nestedGenericType = "List<Map<String, Int>>"

            // When
            val actualResult = classUnderTest.isValidSymbolSyntax(nestedGenericType)

            // Then
            assertTrue("Expected '$nestedGenericType' to be valid", actualResult)
        }
    }

    @RunWith(Parameterized::class)
    class IsValidSymbolSyntaxParameterizedTests(
        private val input: String,
        private val expectedResult: Boolean,
        @Suppress("unused") private val description: String
    ) {
        private lateinit var classUnderTest: SymbolValidator

        @Before
        fun setUp() {
            classUnderTest = SymbolValidator()
        }

        @Test
        fun `Given symbol when isValidSymbolSyntax then returns expected result`() {
            // Given
            val symbol = input

            // When
            val actualResult = classUnderTest.isValidSymbolSyntax(symbol)

            // Then
            assertEquals("Unexpected result for '$symbol'", expectedResult, actualResult)
        }

        companion object {
            private const val DESCRIPTION_INVALID_GENERIC_TYPE = "invalid generic type"
            private const val DESCRIPTION_VALID_GENERIC_TYPE = "valid generic type"
            private const val DESCRIPTION_INVALID_IDENTIFIER = "invalid identifier"
            private const val DESCRIPTION_VALID_IDENTIFIER = "valid identifier"

            @JvmStatic
            @Parameters(name = "{2}: ''{0}'' -> {1}")
            fun parameters(): Collection<Array<Any>> {
                return listOf(
                    arrayOf("String", true, DESCRIPTION_VALID_IDENTIFIER),
                    arrayOf("MyClass", true, DESCRIPTION_VALID_IDENTIFIER),
                    arrayOf("_private", true, DESCRIPTION_VALID_IDENTIFIER),
                    arrayOf("test123", true, DESCRIPTION_VALID_IDENTIFIER),
                    arrayOf("123test", false, DESCRIPTION_INVALID_IDENTIFIER),
                    arrayOf("test-class", false, DESCRIPTION_INVALID_IDENTIFIER),
                    arrayOf("test class", false, DESCRIPTION_INVALID_IDENTIFIER),
                    arrayOf("List<String>", true, DESCRIPTION_VALID_GENERIC_TYPE),
                    arrayOf("Map<String, Int>", true, DESCRIPTION_VALID_GENERIC_TYPE),
                    arrayOf("List<String", false, DESCRIPTION_INVALID_GENERIC_TYPE),
                    arrayOf("List<>", false, DESCRIPTION_INVALID_GENERIC_TYPE),
                    arrayOf("List<<String>>", false, DESCRIPTION_INVALID_GENERIC_TYPE)
                )
            }
        }
    }

    @RunWith(Parameterized::class)
    class IsValidSymbolInContextParameterizedTests(
        private val input: String,
        private val expectedResult: Boolean
    ) {
        private lateinit var classUnderTest: SymbolValidator

        @Before
        fun setUp() {
            classUnderTest = SymbolValidator()
        }

        @Test
        fun `Given symbol when isValidSymbolInContext then returns expected result`() {
            // Given
            val symbol = input
            val contextDirectory = File(".")

            // When
            val actualResult = classUnderTest.isValidSymbolInContext(symbol, contextDirectory)

            // Then
            if (expectedResult) {
                assertTrue("Expected '$symbol' to be valid in context", actualResult)
            } else {
                assertFalse("Expected '$symbol' to be invalid in context", actualResult)
            }
        }

        companion object {
            @JvmStatic
            @Parameters(name = "primitive type in context: ''{0}'' -> {1}")
            fun parameters(): Collection<Array<Any>> {
                return listOf(
                    arrayOf("String", true),
                    arrayOf("Int", true),
                    arrayOf("Boolean", true),
                    arrayOf("Unit", true)
                )
            }
        }
    }
}
