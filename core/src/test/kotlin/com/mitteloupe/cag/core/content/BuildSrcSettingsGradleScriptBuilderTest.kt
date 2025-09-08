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
        // When
        val result = buildBuildSrcSettingsGradleScript()

        // Then
        assertEquals("Should include rootProject name", true, result.contains("rootProject.name = \"buildSrc\""))
    }

    @Test
    fun `Given buildSrc settings gradle script when buildBuildSrcSettingsGradleScript then includes pluginManagement block`() {
        // Given
        // When
        val result = buildBuildSrcSettingsGradleScript()

        // Then
        assertEquals("Should include pluginManagement block", true, result.contains("pluginManagement {"))
    }

    @Test
    fun `Given buildSrc settings gradle script when buildBuildSrcSettingsGradleScript then includes all required repositories`() {
        // Given
        // When
        val result = buildBuildSrcSettingsGradleScript()

        // Then
        assertEquals("Should include google repository", true, result.contains("google()"))
        assertEquals("Should include mavenCentral repository", true, result.contains("mavenCentral()"))
        assertEquals("Should include gradlePluginPortal repository", true, result.contains("gradlePluginPortal()"))
    }
}
