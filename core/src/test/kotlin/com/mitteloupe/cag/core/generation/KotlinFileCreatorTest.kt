package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import org.junit.Assert.assertEquals
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
        classUnderTest = KotlinFileCreator(FileCreator(FakeFileSystemBridge()))
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
        val expectedFile = File(featureRoot, "$layer/src/main/java/com/example/feature/domain/usecase/$fileName")

        // When
        classUnderTest.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = layer,
            featurePackageName = featurePackageName,
            relativePackageSubPath = relativePackageSubPath,
            fileName = fileName,
            content = content
        )

        // Then
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
        classUnderTest.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = layer,
            featurePackageName = featurePackageName,
            relativePackageSubPath = relativePackageSubPath,
            fileName = fileName,
            content = newContent
        )

        // Then
        assertEquals(originalContent, targetFile.readText())
    }

    @Test(expected = GenerationException::class)
    fun `Given directory creation fails when writeKotlinFileInLayer then throws exception`() {
        // Given
        val featureRoot = File(temporaryDirectory, "feature")
        val layer = "domain"
        val featurePackageName = "com.example.feature"
        val relativePackageSubPath = "usecase"
        val fileName = "GetUserUseCase.kt"
        val content = "class GetUserUseCase"
        featureRoot.mkdirs()

        val conflictingFile = File(featureRoot, "$layer/src/main/java/com/example/feature/domain/usecase")
        conflictingFile.parentFile?.mkdirs()
        conflictingFile.writeText("conflicting file")

        // When
        classUnderTest.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = layer,
            featurePackageName = featurePackageName,
            relativePackageSubPath = relativePackageSubPath,
            fileName = fileName,
            content = content
        )

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given file write fails when writeKotlinFileInLayer then throws exception`() {
        // Given
        val featureRoot = File(temporaryDirectory, "feature")
        val layer = "domain"
        val featurePackageName = "com.example.feature"
        val relativePackageSubPath = "usecase"
        val fileName = "GetUserUseCase.kt"
        val content = "class GetUserUseCase"
        featureRoot.mkdirs()

        val targetDirectory = File(featureRoot, "$layer/src/main/java/com/example/feature/$layer/usecase")
        targetDirectory.mkdirs()
        val conflictingDirectory = File(targetDirectory, fileName)
        conflictingDirectory.mkdirs()

        // When
        classUnderTest.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = layer,
            featurePackageName = featurePackageName,
            relativePackageSubPath = relativePackageSubPath,
            fileName = fileName,
            content = content
        )

        // Then throws GenerationException
    }

    @Test
    fun `Given valid parameters when writeKotlinFileInDirectory with target directory then creates file`() {
        // Given
        val targetDirectory = File(temporaryDirectory, "target")
        targetDirectory.mkdirs()
        val fileName = "TestClass.kt"
        val content = "class TestClass"
        val expectedFile = File(targetDirectory, fileName)

        // When
        classUnderTest.writeKotlinFileInDirectory(
            targetDirectory = targetDirectory,
            fileName = fileName,
            content = content
        )

        // Then
        assertTrue(expectedFile.exists())
        assertEquals(content, expectedFile.readText())
    }

    @Test
    fun `Given existing file when writeKotlinFileInDirectory with target directory then does not overwrite`() {
        // Given
        val targetDirectory = File(temporaryDirectory, "target")
        targetDirectory.mkdirs()
        val fileName = "TestClass.kt"
        val originalContent = "class OriginalClass"
        val newContent = "class NewClass"
        val targetFile = File(targetDirectory, fileName)
        targetFile.writeText(originalContent)

        // When
        classUnderTest.writeKotlinFileInDirectory(
            targetDirectory = targetDirectory,
            fileName = fileName,
            content = newContent
        )

        // Then
        assertEquals(originalContent, targetFile.readText())
    }

    @Test(expected = GenerationException::class)
    fun `Given file write fails when writeKotlinFileInDirectory with target directory then throws exception`() {
        // Given
        val targetDirectory = File(temporaryDirectory, "target")
        targetDirectory.mkdirs()
        val fileName = "TestClass.kt"
        val content = "class TestClass"

        val conflictingDirectory = File(targetDirectory, fileName)
        conflictingDirectory.mkdirs()

        // When
        classUnderTest.writeKotlinFileInDirectory(
            targetDirectory = targetDirectory,
            fileName = fileName,
            content = content
        )

        // Then throws GenerationException
    }
}
