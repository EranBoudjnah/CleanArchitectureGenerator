package com.mitteloupe.cag.cleanarchitecturegenerator.validation

import com.mitteloupe.cag.cleanarchitecturegenerator.test.filesystem.FakeFileSystemWrapper
import org.jetbrains.kotlin.incremental.createDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.junit.runners.Suite.SuiteClasses
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempDirectory

@RunWith(Enclosed::class)
@SuiteClasses(
    SymbolValidatorTest.BasicTests::class,
    SymbolValidatorTest.IsValidSymbolSyntaxTests::class,
    SymbolValidatorTest.IsValidSymbolInContextTests::class,
    SymbolValidatorTest.IsValidSymbolInContextClassTests::class
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
    class IsValidSymbolSyntaxTests(
        private val input: String,
        private val expectedResult: Boolean,
        @Suppress("unused") private val description: String
    ) {
        companion object {
            private const val DESCRIPTION_INVALID_GENERIC_TYPE = "invalid generic type"
            private const val DESCRIPTION_VALID_GENERIC_TYPE = "valid generic type"
            private const val DESCRIPTION_INVALID_IDENTIFIER = "invalid identifier"
            private const val DESCRIPTION_VALID_IDENTIFIER = "valid identifier"

            @JvmStatic
            @Parameters(name = "{2}: ''{0}'' -> {1}")
            fun parameters(): Collection<Array<Any>> =
                listOf(
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
    }

    @RunWith(Parameterized::class)
    class IsValidSymbolInContextTests(
        private val symbol: String,
        private val expectedResult: Boolean
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "Given primitive type {0} then returns {1}")
            fun parameters(): Collection<Array<Any>> =
                listOf(
                    arrayOf("String", true),
                    arrayOf("Int", true),
                    arrayOf("Boolean", true),
                    arrayOf("Unit", true)
                )
        }

        private lateinit var classUnderTest: SymbolValidator

        @Before
        fun setUp() {
            classUnderTest = SymbolValidator()
        }

        @Test
        fun `When isValidSymbolInContext`() {
            // Given
            val contextDirectory = File(".")

            // When
            val actualResult = classUnderTest.isValidSymbolInContext(symbol, contextDirectory)

            // Then
            assertEquals(expectedResult, actualResult)
        }
    }

    @RunWith(Parameterized::class)
    class IsValidSymbolInContextClassTests(
        private val qualifiedSymbol: String,
        private val expectedResult: Boolean
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "Given custom type {0} then returns {1}")
            fun parameters(): Collection<Array<Any>> =
                listOf(
                    arrayOf("com.test.model.DemoModel", true),
                    arrayOf("com.Int", false),
                    arrayOf("com.Boolean", false),
                    arrayOf("com.Unit", false)
                )
        }

        private lateinit var classUnderTest: SymbolValidator

        @Before
        fun setUp() {
            val temporaryDirectory = Files.createTempDirectory("IsValidSymbolInContextClassTests_").toFile()

            classUnderTest = SymbolValidator(fileSystemWrapper = FakeFileSystemWrapper(temporaryDirectory))
        }

        @Test
        fun `When isValidSymbolInContext`() {
            // Given
            val contextDirectory = createTempDirectory(prefix = "isValidSymbolInContext_").toFile()
            val gradleFile = File(contextDirectory, "build.gradle.kts")
            gradleFile.createNewFile()
            val classesPath = "/src/main/java/main/com/test/model"
            val classesDirectory =
                File(contextDirectory, classesPath).apply {
                    createDirectory()
                }
            val classesFile = File(classesDirectory, "Classes.kt")
            classesFile.writeText(
                """
                package com.test.model

                class DemoModel {
                }
                """.trimIndent()
            )

            // When
            val actualResult = classUnderTest.isValidSymbolInContext(qualifiedSymbol, contextDirectory)

            // Then
            assertEquals(expectedResult, actualResult)
        }
    }
}
