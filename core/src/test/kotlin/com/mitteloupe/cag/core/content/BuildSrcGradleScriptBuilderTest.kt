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
        val expectedContent = """plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
"""

        // When
        val result = buildBuildSrcGradleScript()

        // Then
        assertEquals("Should have exact content", expectedContent, result)
    }

    @Test
    fun `Given buildSrc gradle script when buildBuildSrcGradleScript then includes mavenCentral repository`() {
        // Given
        val expectedContent = """plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
"""

        // When
        val result = buildBuildSrcGradleScript()

        // Then
        assertEquals("Should have exact content", expectedContent, result)
    }

    @Test
    fun `Given buildSrc gradle script when buildBuildSrcGradleScript then includes repositories block`() {
        // Given
        val expectedContent = """plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
"""

        // When
        val result = buildBuildSrcGradleScript()

        // Then
        assertEquals("Should have exact content", expectedContent, result)
    }
}
