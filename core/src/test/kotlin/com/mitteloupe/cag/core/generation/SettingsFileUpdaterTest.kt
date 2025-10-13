package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class SettingsFileUpdaterTest {
    private lateinit var classUnderTest: SettingsFileUpdater

    @Before
    fun setUp() {
        classUnderTest = SettingsFileUpdater(FileCreator(FakeFileSystemBridge()))
    }

    @Test
    fun `Given no settings files up the tree when updateProjectSettingsIfPresent then does nothing`() {
        // Given
        val startDirectory = createTempDirectory(prefix = "noSettings").toFile()

        // When
        classUnderTest.updateProjectSettingsIfPresent(startDirectory = startDirectory, featureNameLowerCase = "sample")

        // Then does nothing
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
            $$"""
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$$givenFeatureNameLowerCase:$module")
            }
            """.trimIndent()

        // When
        classUnderTest.updateProjectSettingsIfPresent(
            startDirectory = startDirectory,
            featureNameLowerCase = givenFeatureNameLowerCase
        )
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
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
            $$"""
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$$givenFeatureNameLowerCase:$module")
            }
            """.trimIndent()
        val expectedNewlineBeforeIncludes = "rootProject.name = \"app\"\n$expectedTail"

        // When
        classUnderTest.updateProjectSettingsIfPresent(
            startDirectory = startDirectory,
            featureNameLowerCase = givenFeatureNameLowerCase
        )

        // Then
        val content = File(projectRoot, "settings.gradle.kts").readText()
        assertThat(content, endsWith(expectedNewlineBeforeIncludes))
    }

    @Test
    fun `Given kotlin settings file with all includes when updateProjectSettingsIfPresent then does nothing`() {
        // Given
        val givenFeatureNameLowerCase = "ready"
        val initial =
            $$"""
            rootProject.name = "app"
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$$givenFeatureNameLowerCase:$module")
            }
            
            """.trimIndent()
        val (projectRoot, startDirectory) = createProjectWithKotlinSettings(initialContent = initial)

        // When
        classUnderTest.updateProjectSettingsIfPresent(
            startDirectory = startDirectory,
            featureNameLowerCase = givenFeatureNameLowerCase
        )

        // Then
        val content = File(projectRoot, "settings.gradle.kts").readText()
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
            $$"""
            [
                'ui',
                'presentation',
                'domain',
                'data'
            ].each { module ->
                include ":features:$$givenFeatureNameLowerCase:$module"
            }
            """.trimIndent()

        // When
        classUnderTest.updateProjectSettingsIfPresent(
            startDirectory = startDirectory,
            featureNameLowerCase = givenFeatureNameLowerCase
        )
        val content = File(projectRoot, "settings.gradle").readText()

        // Then
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
            
            """.trimIndent()
        val (projectRoot, startDirectory) = createProjectWithKotlinSettings(initialContent = initial)

        val expectedTail =
            $$"""
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$$givenFeatureNameLowerCase:$module")
            }
            """.trimIndent()

        // When
        classUnderTest.updateProjectSettingsIfPresent(
            startDirectory = startDirectory,
            featureNameLowerCase = givenFeatureNameLowerCase
        )

        // Then
        val content = File(projectRoot, "settings.gradle.kts").readText()
        assertThat(content, endsWith(expectedTail))
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
            $$"""
            [
                'ui',
                'presentation',
                'domain',
                'data'
            ].each { module ->
                include ":features:$$givenFeatureNameLowerCase:$module"
            }
            """.trimIndent()

        // When
        classUnderTest.updateProjectSettingsIfPresent(
            startDirectory = startDirectory,
            featureNameLowerCase = givenFeatureNameLowerCase
        )
        val content = File(projectRoot, "settings.gradle").readText()

        // Then
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given grouped includes already present in Kotlin when updateProjectSettingsIfPresent then does nothing`() {
        // Given
        val givenFeatureNameLowerCase = "feat"
        val initial =
            $$"""
            rootProject.name = "app"
            setOf(
                "ui",
                "presentation",
                "domain",
                "data"
            ).forEach { module ->
                include(":features:$$givenFeatureNameLowerCase:$module")
            }
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithKotlinSettings(initialContent = initial)

        // When
        classUnderTest.updateProjectSettingsIfPresent(
            startDirectory = startDirectory,
            featureNameLowerCase = givenFeatureNameLowerCase
        )
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
        assertEquals(initial, content)
    }

    @Test
    fun `Given grouped includes already present in Groovy when updateProjectSettingsIfPresent then does nothing`() {
        // Given
        val givenFeatureNameLowerCase = "feat"
        val initial =
            $$"""
            rootProject.name = 'app'
            [
                'ui',
                'presentation',
                'domain',
                'data'
            ].each { module ->
                include ":features:$$givenFeatureNameLowerCase:$module"
            }
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithGroovySettings(initialContent = initial)

        // When
        classUnderTest.updateProjectSettingsIfPresent(
            startDirectory = startDirectory,
            featureNameLowerCase = givenFeatureNameLowerCase
        )
        val content = File(projectRoot, "settings.gradle").readText()

        // Then
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

    @Test
    fun `Given no settings files up the tree when updateDataSourceSettingsIfPresent then does nothing`() {
        // Given
        val startDirectory = createTempDirectory(prefix = "noSettings").toFile()

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(
            startDirectory = startDirectory
        )

        // Then does nothing
    }

    @Test
    fun `Given kotlin settings file with newline when updateDataSourceSettingsIfPresent then appends grouped includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithKotlinSettings(
                initialContent = "rootProject.name = \"app\"\n"
            )
        val expectedTail =
            $$"""
            setOf(
                "source",
                "implementation"
            ).forEach { module ->
                include(":datasource:$module")
            }
            """.trimIndent()

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(startDirectory = startDirectory)
        val content = File(projectRoot, "settings.gradle.kts").readText()

        // Then
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given kotlin settings file without newline when updateDataSourceSettingsIfPresent then inserts newline before grouped includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithKotlinSettings(
                initialContent = "rootProject.name = \"app\""
            )
        val expectedTail =
            $$"""
            setOf(
                "source",
                "implementation"
            ).forEach { module ->
                include(":datasource:$module")
            }
            """.trimIndent()
        val expectedNewlineBeforeIncludes = "rootProject.name = \"app\"\n$expectedTail"

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(
            startDirectory = startDirectory
        )

        // Then
        val content = File(projectRoot, "settings.gradle.kts").readText()
        assertThat(content, endsWith(expectedNewlineBeforeIncludes))
    }

    @Test
    fun `Given kotlin settings file with partial includes when updateDataSourceSettingsIfPresent then replaces with grouped includes`() {
        // Given
        val initial =
            """
            rootProject.name = "app"
            include(":datasource:source")
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithKotlinSettings(initialContent = initial)

        val expectedTail =
            $$"""
            setOf(
                "source",
                "implementation"
            ).forEach { module ->
                include(":datasource:$module")
            }
            """.trimIndent()

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(startDirectory = startDirectory)

        // Then
        val content = File(projectRoot, "settings.gradle.kts").readText()
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given kotlin settings file, non-rooted partial include when updateDataSourceSettingsIfPresent then replaces with grouped includes`() {
        // Given
        val initial =
            """
            rootProject.name = "app"
            include("datasource:implementation")
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithKotlinSettings(initialContent = initial)

        val expectedTail =
            $$"""
            setOf(
                "source",
                "implementation"
            ).forEach { module ->
                include(":datasource:$module")
            }
            """.trimIndent()

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(startDirectory = startDirectory)

        // Then
        val content = File(projectRoot, "settings.gradle.kts").readText()
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given groovy settings file when updateDataSourceSettingsIfPresent then appends grouped includes`() {
        // Given
        val (projectRoot, startDirectory) =
            createProjectWithGroovySettings(
                initialContent = "rootProject.name = 'app'\n"
            )
        val expectedTail =
            $$"""
            [
                'source',
                'implementation'
            ].each { module ->
                include ":datasource:$module"
            }
            """.trimIndent()

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(startDirectory = startDirectory)

        // Then
        val content = File(projectRoot, "settings.gradle").readText()
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given groovy settings file,partial includes when updateDataSourceSettingsIfPresent then replaces with grouped includes`() {
        // Given
        val initial =
            """
            rootProject.name = 'app'
            include ":datasource:source"
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithGroovySettings(initialContent = initial)

        val expectedTail =
            $$"""
            [
                'source',
                'implementation'
            ].each { module ->
                include ":datasource:$module"
            }
            """.trimIndent()

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(startDirectory = startDirectory)

        // Then
        val content = File(projectRoot, "settings.gradle").readText()
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given grouped includes already present in Kotlin when updateDataSourceSettingsIfPresent then does nothing`() {
        // Given
        val initial =
            $$"""
            rootProject.name = "app"
            setOf(
                "source",
                "implementation"
            ).forEach { module ->
                include(":datasource:$module")
            }
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithKotlinSettings(initialContent = initial)

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(startDirectory = startDirectory)

        // Then
        val content = File(projectRoot, "settings.gradle.kts").readText()
        assertEquals(initial, content)
    }

    @Test
    fun `Given grouped includes already present in Groovy when updateDataSourceSettingsIfPresent then does nothing`() {
        // Given
        val initial =
            $$"""
            rootProject.name = 'app'
            [
                'source',
                'implementation'
            ].each { module ->
                include ":datasource:$module"
            }
            """.trimIndent() + "\n"
        val (projectRoot, startDirectory) = createProjectWithGroovySettings(initialContent = initial)

        // When
        classUnderTest.updateDataSourceSettingsIfPresent(
            startDirectory = startDirectory
        )

        // Then
        val content = File(projectRoot, "settings.gradle").readText()
        assertEquals(initial, content)
    }

    @Test
    fun `Given kotlin settings file when updateArchitectureSettingsIfPresent then appends grouped includes`() {
        // Given
        val (projectRoot, _) =
            createProjectWithKotlinSettings(
                initialContent = "rootProject.name = \"app\"\n"
            )
        val expectedTail =
            $$"""
            setOf(
                "ui",
                "instrumentation-test",
                "presentation",
                "presentation-test",
                "domain"
            ).forEach { module ->
                include(":architecture:$module")
            }
            """.trimIndent()

        // When
        classUnderTest.updateArchitectureSettingsIfPresent(projectRoot)

        // Then
        val content = File(projectRoot, "settings.gradle.kts").readText()
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given groovy settings file when updateArchitectureSettingsIfPresent then appends grouped includes`() {
        // Given
        val (projectRoot, _) =
            createProjectWithGroovySettings(
                initialContent = "rootProject.name = 'app'\n"
            )
        val expectedTail =
            $$"""
            [
                'ui',
                'instrumentation-test',
                'presentation',
                'presentation-test',
                'domain'
            ].each { module ->
                include ":architecture:$module"
            }
            """.trimIndent()

        // When
        classUnderTest.updateArchitectureSettingsIfPresent(projectRoot)

        // Then
        val content = File(projectRoot, "settings.gradle").readText()
        assertThat(content, endsWith(expectedTail))
    }

    @Test
    fun `Given no settings files when updateArchitectureSettingsIfPresent then does nothing`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "noSettings").toFile()

        // When
        classUnderTest.updateArchitectureSettingsIfPresent(projectRoot)

        // Then does nothing
    }
}
