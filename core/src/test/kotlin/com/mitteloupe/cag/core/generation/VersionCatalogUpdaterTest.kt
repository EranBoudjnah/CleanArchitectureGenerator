package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.generation.versioncatalog.DependencyConfiguration
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.LibraryRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.PluginRequirement
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
        classUnderTest = VersionCatalogUpdater()
    }

    @Test
    fun `Given no catalog file when updateVersionCatalogIfPresent then does nothing`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "noCatalog").toFile()
        val dependencyConfiguration = DependencyConfiguration()

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then does nothing
    }

    @Test
    fun `Given no versions section when updateVersionCatalogIfPresent then adds desired plugins and versions`() {
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
            compileSdk = "35"
            minSdk = "24"
            junit4 = "4.13.2"
            ksp = "2.2.10-2.0.2"
            targetSdk = "35"
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
                versions = VersionCatalogConstants.BASIC_VERSIONS + VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given versions section with trailing blanks when updateVersionCatalogIfPresent then trims gaps and adds plugins`() {
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
            targetSdk = "35"
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
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given no plugins section, when updateVersionCatalogIfPresent then appends desired plugins and versions`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.2.10"
                    compileSdk = "35"
                    minSdk = "24"
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "35"
            minSdk = "24"
            targetSdk = "35"
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
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given no plugins section, trailing newline when updateVersionCatalogIfPresent then appends with single separator`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.2.10"
                    compileSdk = "35"
                    minSdk = "24"
                    
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "35"
            minSdk = "24"
            targetSdk = "35"
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
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given plugins section with one desired entry missing when updateVersionCatalogIfPresent then appends missing entries`() {
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
            kotlin = "2.2.10"
            compileSdk = "35"
            minSdk = "24"
            junit4 = "4.13.2"
            ksp = "2.2.10-2.0.2"
            targetSdk = "35"
            androidGradlePlugin = "8.12.2"

            [plugins]
            android-library = { id = "com.android.library", version.ref = "agp" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.BASIC_VERSIONS + VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given compose enabled when updateVersionCatalogIfPresent then adds compose dependencies`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.2.10"
                    compileSdk = "35"
                    minSdk = "24"
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "35"
            minSdk = "24"
            junit4 = "4.13.2"
            ksp = "2.2.10-2.0.2"
            targetSdk = "35"
            androidGradlePlugin = "8.12.2"
            composeBom = "2025.08.01"
            composeNavigation = "2.9.3"
            composeCompiler = "1.5.8"

            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
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
            androidx-activity-compose = { module = "androidx.activity:activity-compose", version = "1.8.2" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions =
                    VersionCatalogConstants.BASIC_VERSIONS +
                        VersionCatalogConstants.ANDROID_VERSIONS +
                        VersionCatalogConstants.COMPOSE_VERSIONS,
                libraries = LibraryConstants.COMPOSE_LIBRARIES,
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS + PluginConstants.COMPOSE_PLUGINS
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given coroutines enabled when updateVersionCatalogIfPresent then adds coroutine dependencies`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    kotlin = "2.2.10"
                    compileSdk = "35"
                    minSdk = "24"
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "35"
            minSdk = "24"
            junit4 = "4.13.2"
            ksp = "2.2.10-2.0.2"
            targetSdk = "35"
            androidGradlePlugin = "8.12.2"

            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }

            [libraries]
            androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.12.0" }
            androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version = "2.7.0" }
            androidx-appcompat = { module = "androidx.appcompat:appcompat", version = "1.6.1" }
            kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version = "1.7.3" }
            material = { module = "com.google.android.material:material", version = "1.11.0" }
            okhttp3 = { module = "com.squareup.okhttp3:okhttp", version = "4.12.0" }
            androidx-recyclerview = { module = "androidx.recyclerview:recyclerview", version = "1.3.2" }
            androidx-fragment-ktx = { module = "androidx.fragment:fragment-ktx", version = "1.6.2" }
            androidx-navigation-fragment-ktx = { module = "androidx.navigation:navigation-fragment-ktx", version = "2.7.6" }
            androidx-constraintlayout = { module = "androidx.constraintlayout:constraintlayout", version = "2.1.4" }
            """.trimIndent() + "\n"

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.BASIC_VERSIONS + VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = LibraryConstants.CORE_ANDROID_LIBRARIES + LibraryConstants.VIEW_LIBRARIES,
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    fun `Given new catalog when updateVersionCatalogIfPresent then resolved mappings are populated`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "newCatalog").toFile()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.BASIC_VERSIONS,
                libraries = LibraryConstants.CORE_ANDROID_LIBRARIES.take(2),
                plugins = PluginConstants.KOTLIN_PLUGINS.take(2)
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

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
    fun `Given existing catalog when updateVersionCatalogIfPresent then resolved mappings are updated`() {
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
                versions = VersionCatalogConstants.BASIC_VERSIONS,
                libraries = LibraryConstants.CORE_ANDROID_LIBRARIES.take(1),
                plugins = PluginConstants.KOTLIN_PLUGINS.take(1)
            )

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

        // Then
        assertEquals(
            "existing-plugin",
            classUnderTest.getResolvedPluginAliasFor(
                PluginRequirement("existing-plugin", "com.example.existing", "kotlin")
            )
        )
        assertEquals("kotlin-jvm", classUnderTest.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM))
        assertEquals(
            "existing-library",
            classUnderTest.getResolvedLibraryAliasForModule(
                LibraryRequirement("existing-library", "com.example:existing", versionLiteral = "1.0.0")
            )
        )
        assertEquals("androidx-core-ktx", classUnderTest.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_CORE_KTX))
    }

    @Test
    fun `Given no catalog file when updateVersionCatalogIfPresent then resolved mappings remain empty`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "noCatalog").toFile()
        val dependencyConfiguration = DependencyConfiguration()

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, dependencyConfiguration = dependencyConfiguration)

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
