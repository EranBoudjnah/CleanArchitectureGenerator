package com.mitteloupe.cag.core.content

import org.junit.Assert.assertEquals
import org.junit.Test

class BuildSrcGradleScriptBuilderTest {
    @Test
    fun `Given buildSrc gradle script when buildBuildSrcGradleScript then returns correct content`() {
        // Given
        // When
        val result = buildBuildSrcGradleScript()

        // Then
        val expectedContent = """plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
"""
        assertEquals(expectedContent, result)
    }

    @Test
    fun `Given buildSrc gradle script when buildBuildSrcGradleScript then includes kotlin-dsl plugin`() {
        // Given
        // When
        val result = buildBuildSrcGradleScript()

        // Then
        assertEquals("Should include kotlin-dsl plugin", true, result.contains("`kotlin-dsl`"))
    }

    @Test
    fun `Given buildSrc gradle script when buildBuildSrcGradleScript then includes mavenCentral repository`() {
        // Given
        // When
        val result = buildBuildSrcGradleScript()

        // Then
        assertEquals("Should include mavenCentral repository", true, result.contains("mavenCentral()"))
    }

    @Test
    fun `Given buildSrc gradle script when buildBuildSrcGradleScript then includes repositories block`() {
        // Given
        // When
        val result = buildBuildSrcGradleScript()

        // Then
        assertEquals(
            "Should include repositories block",
            true,
            result.contains("repositories {")
        )
    }
}
