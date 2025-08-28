package com.mitteloupe.cag.core.generation

import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class SettingsFileUpdaterTest {
    private lateinit var classUnderTest: SettingsFileUpdater

    @Before
    fun setUp() {
        classUnderTest = SettingsFileUpdater()
    }

    @Test
    fun `Given no settings files up the tree when updateProjectSettingsIfPresent then returns null`() {
        // Given
        val startDirectory = createTempDirectory(prefix = "noSettings").toFile()

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = "sample"
            )

        // Then
        Assert.assertNull(result)
    }

    @Test
    fun `Given kotlin settings file with newline when updateProjectSettingsIfPresent then appends includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithKotlinSettings(
                initialContent = "rootProject.name = \"app\"\n"
            )
        val givenFeatureNameLowerCase = "feature"
        val expectedTail =
            """
            include(":features:$givenFeatureNameLowerCase:ui")
            include(":features:$givenFeatureNameLowerCase:presentation")
            include(":features:$givenFeatureNameLowerCase:domain")
            include(":features:$givenFeatureNameLowerCase:data")
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
        Assert.assertNull(result)
        MatcherAssert.assertThat(content, CoreMatchers.endsWith("\n$expectedTail\n"))
    }

    @Test
    fun `Given kotlin settings file without newline when updateProjectSettingsIfPresent then inserts newline before includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithKotlinSettings(
                initialContent = "rootProject.name = \"app\""
            )
        val givenFeatureNameLowerCase = "feat"
        val expectedTail =
            """
            include(":features:$givenFeatureNameLowerCase:ui")
            include(":features:$givenFeatureNameLowerCase:presentation")
            include(":features:$givenFeatureNameLowerCase:domain")
            include(":features:$givenFeatureNameLowerCase:data")
            """.trimIndent()
        val expectedNewlineBeforeIncludes = "rootProject.name = \"app\"\n$expectedTail\n"

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
        Assert.assertNull(result)
        Assert.assertEquals(expectedNewlineBeforeIncludes, content)
    }

    @Test
    fun `Given kotlin settings file with all includes when updateProjectSettingsIfPresent then does nothing`() {
        // Given
        val givenFeatureNameLowerCase = "ready"
        val initial =
            """
            rootProject.name = "app"
            include(":features:$givenFeatureNameLowerCase:ui")
            include(":features:$givenFeatureNameLowerCase:presentation")
            include(":features:$givenFeatureNameLowerCase:domain")
            include(":features:$givenFeatureNameLowerCase:data")
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithKotlinSettings(initialContent = initial)

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
        Assert.assertNull(result)
        Assert.assertEquals(initial, content)
    }

    @Test
    fun `Given groovy settings file when updateProjectSettingsIfPresent then appends includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithGroovySettings(
                initialContent = "rootProject.name = 'app'\n"
            )
        val givenFeatureNameLowerCase = "testfeature"
        val expectedTail =
            """
            include(":features:$givenFeatureNameLowerCase:ui")
            include(":features:$givenFeatureNameLowerCase:presentation")
            include(":features:$givenFeatureNameLowerCase:domain")
            include(":features:$givenFeatureNameLowerCase:data")
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle").readText()

        // Then
        Assert.assertNull(result)
        MatcherAssert.assertThat(content, CoreMatchers.endsWith("\n$expectedTail\n"))
    }

    private fun createProjectWithKotlinSettings(initialContent: String): Pair<File, File> {
        val projectRoot = createTempDirectory(prefix = "projectKts").toFile()
        File(projectRoot, "settings.gradle.kts").writeText(initialContent)
        val startDirectory = createNestedDirectory(projectRoot)
        return projectRoot to startDirectory
    }

    private fun createProjectWithGroovySettings(initialContent: String): Pair<File, File> {
        val projectRoot = createTempDirectory(prefix = "projectGroovy").toFile()
        File(projectRoot, "settings.gradle").writeText(initialContent)
        val startDirectory = createNestedDirectory(projectRoot)
        return projectRoot to startDirectory
    }

    private fun createNestedDirectory(root: File): File {
        val nested = File(root, "a/b/c")
        nested.mkdirs()
        return nested
    }
}
