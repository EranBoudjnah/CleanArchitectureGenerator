package com.mitteloupe.cag.core.content

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsGradleScriptBuilderTest {
    @Test
    fun `Given project name when buildSettingsGradleScript then includes coroutine module`() {
        // Given
        val projectName = "TestProject"
        val featureNames = listOf("SampleFeature")

        // When
        val result = buildSettingsGradleScript(projectName, featureNames)

        // Then
        assertEquals("Settings should include coroutine module", true, result.contains("include(\":coroutine\")"))
    }

    @Test
    fun `Given project name when buildSettingsGradleScript then includes app module`() {
        // Given
        val projectName = "TestProject"
        val featureNames = listOf("SampleFeature")

        // When
        val result = buildSettingsGradleScript(projectName, featureNames)

        // Then
        assertEquals("Settings should include app module", true, result.contains("include(\":app\")"))
    }

    @Test
    fun `Given project name when buildSettingsGradleScript then includes architecture modules`() {
        // Given
        val projectName = "TestProject"
        val featureNames = listOf("SampleFeature")

        // When
        val result = buildSettingsGradleScript(projectName, featureNames)

        // Then
        assertEquals("Settings should include architecture modules", true, result.contains("include(\":architecture:\$module\")"))
    }
}
