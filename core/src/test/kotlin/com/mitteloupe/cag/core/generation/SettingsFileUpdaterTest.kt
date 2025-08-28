package com.mitteloupe.cag.core.generation

import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        assertNull(result)
    }

    @Test
    fun `Given kotlin settings file with newline when updateProjectSettingsIfPresent then appends grouped includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithKotlinSettings(
                initialContent = "rootProject.name = \"app\"\n"
            )
        val givenFeatureNameLowerCase = "feature"
        val expectedTail =
            """
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$givenFeatureNameLowerCase:${'$'}module")
            }
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
        assertNull(result)
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given kotlin settings file without newline when updateProjectSettingsIfPresent then inserts newline before grouped includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithKotlinSettings(
                initialContent = "rootProject.name = \"app\""
            )
        val givenFeatureNameLowerCase = "feat"
        val expectedTail =
            """
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$givenFeatureNameLowerCase:${'$'}module")
            }
            """.trimIndent()
        val expectedNewlineBeforeIncludes = "rootProject.name = \"app\"\n$expectedTail"

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
        assertNull(result)
        assertThat(content, endsWith(expectedNewlineBeforeIncludes))
    }

    @Test
    fun `Given kotlin settings file with all includes when updateProjectSettingsIfPresent then does nothing`() {
        // Given
        val givenFeatureNameLowerCase = "ready"
        val initial =
            """
            rootProject.name = "app"
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$givenFeatureNameLowerCase:${'$'}module")
            }
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
        assertNull(result)
        assertEquals(initial, content)
    }

    @Test
    fun `Given groovy settings file when updateProjectSettingsIfPresent then appends grouped includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithGroovySettings(
                initialContent = "rootProject.name = 'app'\n"
            )
        val givenFeatureNameLowerCase = "testfeature"
        val expectedTail =
            """
            [
                'ui',
                'presentation',
                'domain',
                'data'
            ].each { module ->
                include ":features:$givenFeatureNameLowerCase:${'$'}module"
            }
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle").readText()

        // Then
        assertNull(result)
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given kotlin settings file with partial includes when updateProjectSettingsIfPresent then replaces with grouped includes`() {
        // Given
        val givenFeatureNameLowerCase = "feat"
        val initial =
            """
            rootProject.name = "app"
            include(":features:$givenFeatureNameLowerCase:ui")
            include(":features:$givenFeatureNameLowerCase:domain")
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithKotlinSettings(initialContent = initial)

        val expectedTail =
            """
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$givenFeatureNameLowerCase:${'$'}module")
            }
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
        assertNull(result)
        assertThat(content, endsWith(expectedTail))
        assertFalse(content.lines().any { it.contains("include(\":features:$givenFeatureNameLowerCase:ui\")") })
        assertFalse(content.lines().any { it.contains("include(\":features:$givenFeatureNameLowerCase:domain\")") })
    }

    @Test
    fun `Given groovy settings file with partial includes when updateProjectSettingsIfPresent then replaces with grouped includes`() {
        // Given
        val givenFeatureNameLowerCase = "feat"
        val initial =
            """
            rootProject.name = 'app'
            include ":features:$givenFeatureNameLowerCase:ui"
            include ":features:$givenFeatureNameLowerCase:domain"
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithGroovySettings(initialContent = initial)

        val expectedTail =
            """
            [
                'ui',
                'presentation',
                'domain',
                'data'
            ].each { module ->
                include ":features:$givenFeatureNameLowerCase:${'$'}module"
            }
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle").readText()

        // Then
        assertNull(result)
        assertThat(content, endsWith(expectedTail))
        assertFalse(content.lines().any { it.contains("include \":features:$givenFeatureNameLowerCase:ui\"") })
        assertFalse(content.lines().any { it.contains("include \":features:$givenFeatureNameLowerCase:domain\"") })
    }

    @Test
    fun `Given grouped includes already present in Kotlin when updateProjectSettingsIfPresent then does nothing`() {
        // Given
        val givenFeatureNameLowerCase = "feat"
        val initial =
            """
            rootProject.name = "app"
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$givenFeatureNameLowerCase:${'$'}module")
            }
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
        assertNull(result)
        assertEquals(initial, content)
    }

    @Test
    fun `Given grouped includes already present in Groovy when updateProjectSettingsIfPresent then does nothing`() {
        // Given
        val givenFeatureNameLowerCase = "feat"
        val initial =
            """
            rootProject.name = 'app'
            [
                'ui',
                'presentation',
                'domain',
                'data'
            ].each { module ->
                include ":features:$givenFeatureNameLowerCase:${'$'}module"
            }
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithGroovySettings(initialContent = initial)

        // When
        val result =
            classUnderTest.updateProjectSettingsIfPresent(
                startDirectory = startDirectory,
                featureNameLowerCase = givenFeatureNameLowerCase
            )
        val content = File(projectRoot, "settings.gradle").readText()

        // Then
        assertNull(result)
        assertEquals(initial, content)
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
