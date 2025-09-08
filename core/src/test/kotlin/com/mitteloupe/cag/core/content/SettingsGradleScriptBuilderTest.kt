package com.mitteloupe.cag.core.content

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsGradleScriptBuilderTest {
    @Test
    fun `Given project name when buildSettingsGradleScript then includes coroutine module`() {
        // Given
        val projectName = "TestProject"
        val featureNames = listOf("SampleFeature")
        val expectedContent = """enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "TestProject"
include(":app")
include(":coroutine")

setOf(
    "ui",
    "instrumentation-test", 
    "presentation",
    "presentation-test",
    "domain"
).forEach { module ->
    include(":architecture:${'$'}module")
}

setOf("ui", "presentation", "domain", "data").forEach { layer ->
   include("features:samplefeature:${'$'}layer")
}

setOf(
    "source",
    "implementation"
).forEach { module ->
    include(":datasource:${'$'}module")
}"""

        // When
        val result = buildSettingsGradleScript(projectName, featureNames)

        // Then
        assertEquals("Settings should have exact content", expectedContent, result)
    }

    @Test
    fun `Given project name when buildSettingsGradleScript then includes app module`() {
        // Given
        val projectName = "TestProject"
        val featureNames = listOf("SampleFeature")
        val expectedContent = """enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "TestProject"
include(":app")
include(":coroutine")

setOf(
    "ui",
    "instrumentation-test", 
    "presentation",
    "presentation-test",
    "domain"
).forEach { module ->
    include(":architecture:${'$'}module")
}

setOf("ui", "presentation", "domain", "data").forEach { layer ->
   include("features:samplefeature:${'$'}layer")
}

setOf(
    "source",
    "implementation"
).forEach { module ->
    include(":datasource:${'$'}module")
}"""

        // When
        val result = buildSettingsGradleScript(projectName, featureNames)

        // Then
        assertEquals("Settings should have exact content", expectedContent, result)
    }

    @Test
    fun `Given project name when buildSettingsGradleScript then includes architecture modules`() {
        // Given
        val projectName = "TestProject"
        val featureNames = listOf("SampleFeature")
        val expectedContent = """enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "TestProject"
include(":app")
include(":coroutine")

setOf(
    "ui",
    "instrumentation-test", 
    "presentation",
    "presentation-test",
    "domain"
).forEach { module ->
    include(":architecture:${'$'}module")
}

setOf("ui", "presentation", "domain", "data").forEach { layer ->
   include("features:samplefeature:${'$'}layer")
}

setOf(
    "source",
    "implementation"
).forEach { module ->
    include(":datasource:${'$'}module")
}"""

        // When
        val result = buildSettingsGradleScript(projectName, featureNames)

        // Then
        assertEquals("Settings should have exact content", expectedContent, result)
    }
}
