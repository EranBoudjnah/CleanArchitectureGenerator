package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class BuildSrcContentCreatorTest {
    private lateinit var classUnderTest: BuildSrcContentCreator

    @Before
    fun setUp() {
        classUnderTest = BuildSrcContentCreator(FileCreator(FakeFileSystemBridge()))
    }

    @Test
    fun `Given project root when writeBuildSrcGradleFile then creates buildSrc build gradle file with correct content`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()
        val buildSrcDir = File(projectRoot, "buildSrc")
        buildSrcDir.mkdirs()
        val expectedContent = """plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
"""

        // When
        classUnderTest.writeGradleFile(projectRoot)
        val buildSrcGradleFile = File(buildSrcDir, "build.gradle.kts")

        // Then
        assertEquals(expectedContent, buildSrcGradleFile.readText())
    }

    @Test
    fun `Given buildSrc file already exists when writeBuildSrcGradleFile then does nothing`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot2").toFile()
        val buildSrcDir = File(projectRoot, "buildSrc")
        buildSrcDir.mkdirs()
        val buildSrcGradleFile = File(buildSrcDir, "build.gradle.kts")
        val initialContent = "// existing buildSrc content\n"
        buildSrcGradleFile.writeText(initialContent)

        // When
        classUnderTest.writeGradleFile(projectRoot)

        // Then
        assertEquals(initialContent, buildSrcGradleFile.readText())
    }

    @Test
    fun `Given project root when writeBuildSrcSettingsGradleFile then creates buildSrc settings gradle file with correct content`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot3").toFile()
        val buildSrcDir = File(projectRoot, "buildSrc")
        buildSrcDir.mkdirs()
        val expectedContent = """rootProject.name = "buildSrc"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
"""

        // When
        classUnderTest.writeSettingsGradleFile(projectRoot)
        val buildSrcSettingsFile = File(buildSrcDir, "settings.gradle.kts")

        // Then
        assertEquals(expectedContent, buildSrcSettingsFile.readText())
    }

    @Test
    fun `Given buildSrc settings file already exists when writeBuildSrcSettingsGradleFile then does nothing`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot4").toFile()
        val buildSrcDir = File(projectRoot, "buildSrc")
        buildSrcDir.mkdirs()
        val buildSrcSettingsFile = File(buildSrcDir, "settings.gradle.kts")
        val initialContent = "// existing buildSrc settings content\n"
        buildSrcSettingsFile.writeText(initialContent)

        // When
        classUnderTest.writeSettingsGradleFile(projectRoot)

        // Then
        assertEquals(initialContent, buildSrcSettingsFile.readText())
    }
}
