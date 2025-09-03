package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class GradleFileCreatorTest {
    private lateinit var classUnderTest: GradleFileCreator

    @Before
    fun setUp() {
        classUnderTest = GradleFileCreator()
    }

    @Test
    fun `Given module directory exists and file missing when writeGradleFileIfMissing then creates file with content`() {
        // Given
        val featureRoot = createTempDirectory(prefix = "featureRoot").toFile()
        val givenLayer = "data"
        val moduleDir = File(featureRoot, givenLayer)
        moduleDir.mkdirs()
        val givenContent = "plugins { kotlin(\"jvm\") }\n"

        // When
        classUnderTest.writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = givenLayer,
            content = givenContent
        )

        // Then
        val targetFile = File(moduleDir, "build.gradle.kts")
        assertEquals(givenContent, targetFile.readText())
    }

    @Test(expected = GenerationException::class)
    fun `Given module directory missing when writeGradleFileIfMissing then throws exception and does not create file`() {
        // Given
        val featureRoot = createTempDirectory(prefix = "featureRoot2").toFile()
        val givenLayer = "presentation"
        val givenContent = "// gradle script\n"

        // When
        classUnderTest.writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = givenLayer,
            content = givenContent
        )

        // Then throws GenerationException
    }

    @Test
    fun `Given file already exists when writeGradleFileIfMissing then does nothing`() {
        // Given
        val featureRoot = createTempDirectory(prefix = "featureRoot3").toFile()
        val givenLayer = "ui"
        val moduleDir = File(featureRoot, givenLayer)
        moduleDir.mkdirs()
        val targetFile = File(moduleDir, "build.gradle.kts")
        val initialContent = "// existing content\n"
        targetFile.writeText(initialContent)
        val newContent = "// new content that should be ignored\n"

        // When
        classUnderTest.writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = givenLayer,
            content = newContent
        )

        // Then
        assertEquals(initialContent, targetFile.readText())
    }
}
