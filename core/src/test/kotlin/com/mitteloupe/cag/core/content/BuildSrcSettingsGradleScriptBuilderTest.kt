package com.mitteloupe.cag.core.content

import org.junit.Assert.assertEquals
import org.junit.Test

class BuildSrcSettingsGradleScriptBuilderTest {
    @Test
    fun `Given buildSrc settings gradle script when buildBuildSrcSettingsGradleScript then returns correct content`() {
        // Given
        // When
        val result = buildBuildSrcSettingsGradleScript()

        // Then
        val expectedContent = """rootProject.name = "buildSrc"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
"""
        assertEquals(expectedContent, result)
    }

    @Test
    fun `Given buildSrc settings gradle script when buildBuildSrcSettingsGradleScript then includes rootProject name`() {
        // Given
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
        val result = buildBuildSrcSettingsGradleScript()

        // Then
        assertEquals("Should have exact content", expectedContent, result)
    }

    @Test
    fun `Given buildSrc settings gradle script when buildBuildSrcSettingsGradleScript then includes pluginManagement block`() {
        // Given
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
        val result = buildBuildSrcSettingsGradleScript()

        // Then
        assertEquals("Should have exact content", expectedContent, result)
    }

    @Test
    fun `Given buildSrc settings gradle script when buildBuildSrcSettingsGradleScript then includes all required repositories`() {
        // Given
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
        val result = buildBuildSrcSettingsGradleScript()

        // Then
        assertEquals("Should have exact content", expectedContent, result)
    }
}
