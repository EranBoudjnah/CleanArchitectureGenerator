package com.mitteloupe.cag.core

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        val result =
            classUnderTest.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = givenLayer,
                content = givenContent
            )
        val targetFile = File(moduleDir, "build.gradle.kts")

        // Then
        assertNull(result)
        assertEquals(givenContent, targetFile.readText())
    }

    @Test
    fun `Given module directory missing when writeGradleFileIfMissing then returns error and does not create file`() {
        // Given
        val featureRoot = createTempDirectory(prefix = "featureRoot2").toFile()
        val givenLayer = "presentation"
        val moduleDir = File(featureRoot, givenLayer)
        val givenContent = "// gradle script\n"

        // When
        val result =
            classUnderTest.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = givenLayer,
                content = givenContent
            )
        val targetFile = File(moduleDir, "build.gradle.kts")

        // Then
        assertNotNull(result)
        checkNotNull(result)
        assertThat(result, startsWith(ERROR_PREFIX))
        assertThat(result, containsString("$givenLayer/build.gradle.kts"))
        assertFalse(targetFile.exists())
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
        val result =
            classUnderTest.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = givenLayer,
                content = newContent
            )

        // Then
        assertNull(result)
        assertEquals(initialContent, targetFile.readText())
    }
}
