package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.DependencyConfiguration
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.LibraryRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.PluginRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.VersionRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class VersionCatalogUpdaterTest {
    private lateinit var classUnderTest: VersionCatalogUpdater

    @Before
    fun setUp() {
        classUnderTest = VersionCatalogUpdater(FileCreator(FakeFileSystemBridge()))
    }

    @Test
    fun `Given no catalog file when createOrUpdateVersionCatalog then does nothing`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "noCatalog").toFile()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = emptyList(),
                libraries = emptyList(),
                plugins = emptyList()
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then does nothing
    }

    @Test
    fun `Given no versions section when createOrUpdateVersionCatalog then adds desired plugins and versions`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [plugins]
                    android-application = { id = "com.android.application", version = "1.0.0" }
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            ksp = "2.2.10-2.0.2"
            androidGradlePlugin = "8.12.2"

            [plugins]
            android-application = { id = "com.android.application", version = "1.0.0" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = emptyList(),
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given versions section with trailing blanks when createOrUpdateVersionCatalog then trims gaps and adds plugins`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.0.0"
                    compileSdk = "34"
                    minSdk = "23"
                    
                    
                    
                    [plugins]
                    android-application = { id = "com.android.application", version = "1.0.0" }
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.0.0"
            compileSdk = "34"
            minSdk = "23"
            targetSdk = "36"
            androidGradlePlugin = "8.12.2"
            ksp = "2.2.10-2.0.2"

            [plugins]
            android-application = { id = "com.android.application", version = "1.0.0" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given no plugins section, when createOrUpdateVersionCatalog then appends desired plugins and versions`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.2.10"
                    compileSdk = "36"
                    minSdk = "24"
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "36"
            minSdk = "24"
            targetSdk = "36"
            androidGradlePlugin = "8.12.2"
            ksp = "2.2.10-2.0.2"

            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given no plugins section, trailing newline when createOrUpdateVersionCatalog then appends with single separator`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.2.10"
                    compileSdk = "36"
                    minSdk = "24"
                    
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "36"
            minSdk = "24"
            ksp = "2.2.10-2.0.2"
            androidGradlePlugin = "8.12.2"

            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = emptyList(),
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given plugins section with one desired entry missing when createOrUpdateVersionCatalog then appends missing entries`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    agp = "35"

                    [plugins]
                    android-library = { id = "com.android.library", version.ref = "agp" }
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            agp = "35"
            androidGradlePlugin = "8.12.2"

            [plugins]
            android-library = { id = "com.android.library", version.ref = "agp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = emptyList(),
                libraries = emptyList(),
                plugins = PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given compose when createOrUpdateVersionCatalog then adds compose dependencies`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.2.10"
                    compileSdk = "36"
                    minSdk = "24"
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "36"
            minSdk = "24"
            composeBom = "2025.08.01"
            composeNavigation = "2.9.3"
            androidxActivityCompose = "1.8.2"

            [plugins]
            compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

            [libraries]
            compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
            compose-ui = { module = "androidx.compose.ui:ui" }
            compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
            compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
            compose-material3 = { module = "androidx.compose.material3:material3" }
            compose-navigation = { module = "androidx.navigation:navigation-compose", version.ref = "composeNavigation" }
            compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
            compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
            androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidxActivityCompose" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = emptyList(),
                libraries = LibraryConstants.COMPOSE_LIBRARIES,
                plugins = listOf(PluginConstants.COMPOSE_COMPILER)
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given coroutines when createOrUpdateVersionCatalog then adds coroutine dependencies`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.2.10"
                    compileSdk = "36"
                    minSdk = "24"
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "36"
            minSdk = "24"
            kotlinxCoroutines = "1.7.3"

            [libraries]
            kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = listOf(VersionCatalogConstants.KOTLINX_COROUTINES_VERSION),
                libraries = listOf(LibraryConstants.KOTLINX_COROUTINES_CORE),
                plugins = emptyList()
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given new catalog when createOrUpdateVersionCatalog then resolved mappings are populated`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "newCatalog").toFile()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = emptyList(),
                libraries = LibraryConstants.CORE_ANDROID_LIBRARIES.take(2),
                plugins = PluginConstants.KOTLIN_PLUGINS.take(2)
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals("kotlin-jvm", classUnderTest.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM))
        assertEquals("kotlin-android", classUnderTest.getResolvedPluginAliasFor(PluginConstants.KOTLIN_ANDROID))
        assertEquals("androidx-core-ktx", classUnderTest.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_CORE_KTX))
        assertEquals(
            "androidx-lifecycle-runtime-ktx",
            classUnderTest.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_LIFECYCLE_RUNTIME_KTX)
        )
    }

    @Test
    fun `Given existing catalog when createOrUpdateVersionCatalog then resolved mappings are updated`() {
        // Given
        val (projectRoot, _) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.1.0"

                    [plugins]
                    existing-plugin = { id = "com.example.existing", version = "1.0.0" }

                    [libraries]
                    existing-library = { module = "com.example:existing", version = "1.0.0" }
                    """.trimIndent()
            )
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = emptyList(),
                libraries = LibraryConstants.CORE_ANDROID_LIBRARIES.take(1),
                plugins = PluginConstants.KOTLIN_PLUGINS.take(1)
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(
            "existing-plugin",
            classUnderTest.getResolvedPluginAliasFor(
                PluginRequirement("existing-plugin", "com.example.existing", VersionRequirement("kotlin", "1.1.0"))
            )
        )
        assertEquals("kotlin-jvm", classUnderTest.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM))
        assertEquals(
            "existing-library",
            classUnderTest.getResolvedLibraryAliasForModule(
                LibraryRequirement("existing-library", "com.example:existing")
            )
        )
        assertEquals("androidx-core-ktx", classUnderTest.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_CORE_KTX))
    }

    @Test
    fun `Given no catalog file when createOrUpdateVersionCatalog then resolved mappings remain empty`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "noCatalog").toFile()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = emptyList(),
                libraries = emptyList(),
                plugins = emptyList()
            )

        // When
        classUnderTest.createOrUpdateVersionCatalog(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals("kotlin-jvm", classUnderTest.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM))
        assertEquals(false, classUnderTest.isPluginAvailable(PluginConstants.KOTLIN_JVM))
        assertEquals("androidx-core-ktx", classUnderTest.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_CORE_KTX))
    }

    private fun createProjectWithCatalog(initialContent: String): Pair<File, File> {
        val projectRoot = createTempDirectory(prefix = "projectWithCatalog").toFile()
        val gradleDir = File(projectRoot, "gradle")
        gradleDir.mkdirs()
        val catalogFile = File(gradleDir, "libs.versions.toml")
        catalogFile.writeText(initialContent)
        return projectRoot to catalogFile
    }
}
