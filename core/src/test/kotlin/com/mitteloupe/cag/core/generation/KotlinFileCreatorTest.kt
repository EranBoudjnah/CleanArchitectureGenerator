package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class KotlinFileCreatorTest {
    private lateinit var classUnderTest: KotlinFileCreator
    private lateinit var temporaryDirectory: File

    @Before
    fun setUp() {
        temporaryDirectory = createTempDirectory(prefix = "KotlinFileCreatorTest").toFile()
        classUnderTest = KotlinFileCreator()
    }

    @Test
    fun `Given valid parameters when writeKotlinFileInLayer then creates file with content`() {
        // Given
        val featureRoot = File(temporaryDirectory, "feature")
        val layer = "domain"
        val featurePackageName = "com.example.feature"
        val relativePackageSubPath = "usecase"
        val fileName = "GetUserUseCase.kt"
        val content = "class GetUserUseCase"
        featureRoot.mkdirs()
        val expectedFile = File(featureRoot, "$layer/src/main/java/com/example/feature/usecase/$fileName")

        // When
        val result =
            classUnderTest.writeKotlinFileInLayer(
                featureRoot = featureRoot,
                layer = layer,
                featurePackageName = featurePackageName,
                relativePackageSubPath = relativePackageSubPath,
                fileName = fileName,
                content = content
            )

        // Then
        assertNull(result)
        assertTrue(expectedFile.exists())
        assertEquals(content, expectedFile.readText())
    }

    @Test
    fun `Given existing file when writeKotlinFileInLayer then does not overwrite`() {
        // Given
        val featureRoot = File(temporaryDirectory, "feature")
        val layer = "domain"
        val featurePackageName = "com.example.feature"
        val relativePackageSubPath = "usecase"
        val fileName = "GetUserUseCase.kt"
        val originalContent = "class OriginalUseCase"
        val newContent = "class NewUseCase"
        featureRoot.mkdirs()

        val targetDirectory = File(featureRoot, "$layer/src/main/java/com/example/feature/usecase")
        targetDirectory.mkdirs()
        val targetFile = File(targetDirectory, fileName)
        targetFile.writeText(originalContent)

        // When
        val result =
            classUnderTest.writeKotlinFileInLayer(
                featureRoot = featureRoot,
                layer = layer,
                featurePackageName = featurePackageName,
                relativePackageSubPath = relativePackageSubPath,
                fileName = fileName,
                content = newContent
            )

        // Then
        assertNull(result)
        assertEquals(originalContent, targetFile.readText())
    }

    @Test
    fun `Given directory creation fails when writeKotlinFileInLayer then returns error`() {
        // Given
        val featureRoot = File(temporaryDirectory, "feature")
        val layer = "domain"
        val featurePackageName = "com.example.feature"
        val relativePackageSubPath = "usecase"
        val fileName = "GetUserUseCase.kt"
        val content = "class GetUserUseCase"
        featureRoot.mkdirs()

        val conflictingFile = File(featureRoot, "$layer/src/main/java/com/example/feature/usecase")
        conflictingFile.parentFile?.mkdirs()
        conflictingFile.writeText("conflicting file")

        // When
        val result =
            classUnderTest.writeKotlinFileInLayer(
                featureRoot = featureRoot,
                layer = layer,
                featurePackageName = featurePackageName,
                relativePackageSubPath = relativePackageSubPath,
                fileName = fileName,
                content = content
            )

        // Then
        assertNotNull(result)
        checkNotNull(result)
        assertThat(result, startsWith(ERROR_PREFIX))
        assertThat(result, containsString("Failed to create directory"))
    }

    @Test
    fun `Given file write fails when writeKotlinFileInLayer then returns error`() {
        // Given
        val featureRoot = File(temporaryDirectory, "feature")
        val layer = "domain"
        val featurePackageName = "com.example.feature"
        val relativePackageSubPath = "usecase"
        val fileName = "GetUserUseCase.kt"
        val content = "class GetUserUseCase"
        featureRoot.mkdirs()

        val targetDirectory = File(featureRoot, "$layer/src/main/java/com/example/feature/usecase")
        targetDirectory.mkdirs()
        val conflictingDirectory = File(targetDirectory, fileName)
        conflictingDirectory.mkdirs()

        // When
        val result =
            classUnderTest.writeKotlinFileInLayer(
                featureRoot = featureRoot,
                layer = layer,
                featurePackageName = featurePackageName,
                relativePackageSubPath = relativePackageSubPath,
                fileName = fileName,
                content = content
            )

        // Then
        assertNotNull(result)
        checkNotNull(result)
        assertThat(result, startsWith(ERROR_PREFIX))
        assertThat(result, containsString("Failed to create file"))
    }

    @Test
    fun `Given valid parameters when writeKotlinFileInLayer with target directory then creates file`() {
        // Given
        val targetDirectory = File(temporaryDirectory, "target")
        targetDirectory.mkdirs()
        val fileName = "TestClass.kt"
        val content = "class TestClass"
        val expectedFile = File(targetDirectory, fileName)

        // When
        val result =
            classUnderTest.writeKotlinFileInLayer(
                targetDirectory = targetDirectory,
                fileName = fileName,
                content = content
            )

        // Then
        assertNull(result)
        assertTrue(expectedFile.exists())
        assertEquals(content, expectedFile.readText())
    }

    @Test
    fun `Given existing file when writeKotlinFileInLayer with target directory then does not overwrite`() {
        // Given
        val targetDirectory = File(temporaryDirectory, "target")
        targetDirectory.mkdirs()
        val fileName = "TestClass.kt"
        val originalContent = "class OriginalClass"
        val newContent = "class NewClass"
        val targetFile = File(targetDirectory, fileName)
        targetFile.writeText(originalContent)

        // When
        val result =
            classUnderTest.writeKotlinFileInLayer(
                targetDirectory = targetDirectory,
                fileName = fileName,
                content = newContent
            )

        // Then
        assertNull(result)
        assertEquals(originalContent, targetFile.readText())
    }

    @Test
    fun `Given file write fails when writeKotlinFileInLayer with target directory then returns error`() {
        // Given
        val targetDirectory = File(temporaryDirectory, "target")
        targetDirectory.mkdirs()
        val fileName = "TestClass.kt"
        val content = "class TestClass"

        val conflictingDirectory = File(targetDirectory, fileName)
        conflictingDirectory.mkdirs()

        // When
        val result =
            classUnderTest.writeKotlinFileInLayer(
                targetDirectory = targetDirectory,
                fileName = fileName,
                content = content
            )

        // Then
        assertNotNull(result)
        checkNotNull(result)
        assertThat(result, startsWith(ERROR_PREFIX))
        assertThat(result, containsString("Failed to create file"))
    }
}
