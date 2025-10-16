package com.mitteloupe.cag.core.generation.structure

import org.junit.Assert
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.junit.runners.Suite.SuiteClasses
import java.io.File

@RunWith(Enclosed::class)
@SuiteClasses(
    PackageNameDeriverTest.BasicTests::class,
    PackageNameDeriverTest.JavaSourceTests::class,
    PackageNameDeriverTest.KotlinSourceTests::class,
    PackageNameDeriverTest.EdgeCaseTests::class
)
class PackageNameDeriverTest {
    class BasicTests {
        @Test
        fun `Given non-existent directory when derivePackageNameForDirectory then returns null`() {
            // Given
            val directory = File("/src/non/existent/path")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertNull(result)
        }

        @Test
        fun `Given directory without src marker when derivePackageNameForDirectory then returns null`() {
            // Given
            val directory = File("/some/random/path")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertNull(result)
        }
    }

    @RunWith(Parameterized::class)
    class JavaSourceTests(
        private val directoryPath: String,
        private val expectedPackageName: String?,
        @Suppress("unused") private val description: String
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "{2}: Given ''{0}'' then returns {1}")
            fun parameters(): Collection<Array<Any>> =
                listOf(
                    testCase(
                        directoryPath = "/project/src/main/java/com/example/feature",
                        expectedPackageName = "com.example.feature",
                        description = "simple java package"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/java/com/example/feature/domain",
                        expectedPackageName = "com.example.feature.domain",
                        description = "nested java package"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/java/com/example/feature/presentation/viewmodel",
                        expectedPackageName = "com.example.feature.presentation.viewmodel",
                        description = "deeply nested java package"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/java/com/example",
                        expectedPackageName = "com.example",
                        description = "root java package"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/java/com/example/feature/domain/usecase",
                        expectedPackageName = "com.example.feature.domain.usecase",
                        description = "java package with usecase"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/java/com/example/feature/presentation",
                        expectedPackageName = "com.example.feature.presentation",
                        description = "java package with presentation"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/java/com/example/feature/data",
                        expectedPackageName = "com.example.feature.data",
                        description = "java package with data"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/java/com/example/feature/ui",
                        expectedPackageName = "com.example.feature.ui",
                        description = "java package with ui"
                    )
                )

            private fun testCase(
                directoryPath: String,
                expectedPackageName: String,
                description: String
            ): Array<Any> = arrayOf(directoryPath, expectedPackageName, description)
        }

        @Test
        fun `When derivePackageNameForDirectory`() {
            // Given
            val directory = File(directoryPath)

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("Unexpected result for '$directoryPath'", expectedPackageName, result)
        }
    }

    @RunWith(Parameterized::class)
    class KotlinSourceTests(
        private val directoryPath: String,
        private val expectedPackageName: String?,
        @Suppress("unused") private val description: String
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "{2}: Given ''{0}'' then returns {1}")
            fun parameters(): Collection<Array<Any>> =
                listOf(
                    testCase(
                        directoryPath = "/project/src/main/kotlin/com/example/feature",
                        expectedPackageName = "com.example.feature",
                        description = "simple kotlin package"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/kotlin/com/example/feature/domain",
                        expectedPackageName = "com.example.feature.domain",
                        description = "nested kotlin package"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/kotlin/com/example/feature/presentation/viewmodel",
                        expectedPackageName = "com.example.feature.presentation.viewmodel",
                        description = "deeply nested kotlin package"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/kotlin/com/example",
                        expectedPackageName = "com.example",
                        description = "root kotlin package"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/kotlin/com/example/feature/domain/usecase",
                        expectedPackageName = "com.example.feature.domain.usecase",
                        description = "kotlin package with usecase"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/kotlin/com/example/feature/presentation",
                        expectedPackageName = "com.example.feature.presentation",
                        description = "kotlin package with presentation"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/kotlin/com/example/feature/data",
                        expectedPackageName = "com.example.feature.data",
                        description = "kotlin package with data"
                    ),
                    testCase(
                        directoryPath = "/project/src/main/kotlin/com/example/feature/ui",
                        expectedPackageName = "com.example.feature.ui",
                        description = "kotlin package with ui"
                    )
                )

            private fun testCase(
                directoryPath: String,
                expectedPackageName: String,
                description: String
            ): Array<Any> = arrayOf(directoryPath, expectedPackageName, description)
        }

        @Test
        fun `When derivePackageNameForDirectory`() {
            // Given
            val directory = File(directoryPath)

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("Unexpected result for '$directoryPath'", expectedPackageName, result)
        }
    }

    class EdgeCaseTests {
        @Test
        fun `Given directory ending at src main java when derivePackageNameForDirectory then returns empty string`() {
            // Given
            val directory = File("/project/src/main/java")
            val expectedPackageName = ""

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals(expectedPackageName, result)
        }

        @Test
        fun `Given directory ending at src main kotlin when derivePackageNameForDirectory then returns empty string`() {
            // Given
            val directory = File("/project/src/main/kotlin")
            val expectedPackageName = ""

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals(expectedPackageName, result)
        }

        @Test
        fun `Given directory with trailing slash when derivePackageNameForDirectory then returns expected package name`() {
            // Given
            val directory = File("/project/src/main/java/com/example/feature/")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.feature", result)
        }

        @Test
        fun `Given directory with multiple separators when derivePackageNameForDirectory then returns expected package name`() {
            // Given
            val directory = File("/project/src/main/java//com//example//feature")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.feature", result)
        }

        @Test
        fun `Given directory with empty segments when derivePackageNameForDirectory then returns expected package name`() {
            // Given
            val directory = File("/project/src/main/java/com/example//feature")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.feature", result)
        }

        @Test
        fun `Given directory with src main java in middle when derivePackageNameForDirectory then returns package after marker`() {
            // Given
            val directory = File("/some/path/src/main/java/com/example/feature/extra/path")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.feature.extra.path", result)
        }

        @Test
        fun `Given directory with src main kotlin in middle when derivePackageNameForDirectory then returns package after marker`() {
            // Given
            val directory = File("/some/path/src/main/kotlin/com/example/feature/extra/path")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.feature.extra.path", result)
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given directory with both java and kotlin markers when derivePackageNameForDirectory then returns package after first marker`() {
            // Given
            val directory = File("/project/src/main/java/com/example/feature/src/main/kotlin/other")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.feature.src.main.kotlin.other", result)
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given directory with single character package segments when derivePackageNameForDirectory then returns expected package name`() {
            // Given
            val directory = File("/project/src/main/java/a/b/c")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("a.b.c", result)
        }

        @Test
        fun `Given directory with underscore in package name when derivePackageNameForDirectory then returns expected package name`() {
            // Given
            val directory = File("/project/src/main/java/com/example/my_feature")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.my_feature", result)
        }

        @Test
        fun `Given directory with numbers in package name when derivePackageNameForDirectory then returns expected package name`() {
            // Given
            val directory = File("/project/src/main/java/com/example/feature2")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.feature2", result)
        }

        @Test
        fun `Given directory with mixed case package name when derivePackageNameForDirectory then returns expected package name`() {
            // Given
            val directory = File("/project/src/main/java/com/example/MyFeature")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.MyFeature", result)
        }

        @Test
        fun `Given directory with Windows separators when derivePackageNameForDirectory then returns null`() {
            // Given
            val directory = File("C:\\project\\src\\main\\java\\com\\example\\feature")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertNull(result)
        }

        @Test
        fun `Given directory with relative path when derivePackageNameForDirectory then returns expected package name`() {
            // Given
            val directory = File("src/main/java/com/example/feature")

            // When
            val result = PackageNameDeriver.derivePackageNameForDirectory(directory)

            // Then
            Assert.assertEquals("com.example.feature", result)
        }
    }
}
