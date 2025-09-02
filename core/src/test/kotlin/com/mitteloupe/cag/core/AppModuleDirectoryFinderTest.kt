
package com.mitteloupe.cag.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class AppModuleDirectoryFinderTest {
    private lateinit var classUnderTest: AppModuleDirectoryFinder
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        classUnderTest = AppModuleDirectoryFinder()
        tempDirectory = createTempDirectory(prefix = "test").toFile()
    }

    @Test
    fun `Given project with android app plugin in kts format when findAndroidAppModuleDirectories then returns app module`() {
        // Given
        val appModule =
            createAppModuleWithBuildFile(
                """
                plugins {
                    id("com.android.application")
                }
                """.trimIndent()
            )

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertSingleAppModuleFound(result, appModule)
    }

    @Test
    fun `Given project with android app plugin in groovy format when findAndroidAppModuleDirectories then returns app module`() {
        // Given
        val appModule =
            createAppModuleWithGroovyBuildFile(
                """
                plugins {
                    id 'com.android.application'
                }
                """.trimIndent()
            )

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertSingleAppModuleFound(result, appModule)
    }

    @Test
    fun `Given project with android app plugin via apply when findAndroidAppModuleDirectories then returns app module`() {
        // Given
        val appModule =
            createAppModuleWithBuildFile(
                """
                apply(plugin = "com.android.application")
                """.trimIndent()
            )

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertSingleAppModuleFound(result, appModule)
    }

    @Test
    fun `Given project with android app plugin via version catalog when findAndroidAppModuleDirectories then returns app module`() {
        // Given
        val appModule = createVersionCatalogTestSetup()

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertSingleAppModuleFound(result, appModule)
    }

    @Test
    fun `Given project with android app plugin via version catalog with dash returns app module`() {
        // Given
        val appModule = createVersionCatalogTestSetup()

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertSingleAppModuleFound(result, appModule)
    }

    @Test
    fun `Given project with library module when findAndroidAppModuleDirectories then returns empty list`() {
        // Given
        createLibraryModuleWithBuildFile(
            """
            plugins {
                id("com.android.library")
            }
            """.trimIndent()
        )

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given project with apply false plugin when findAndroidAppModuleDirectories then returns empty list`() {
        // Given
        createAppModuleWithBuildFile(
            """
            plugins {
                id("com.android.application") apply false
            }
            """.trimIndent()
        )

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given project with no gradle files when findAndroidAppModuleDirectories then returns empty list`() {
        // Given
        File(tempDirectory, "regular").apply { mkdirs() }

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given nested project structure when findAndroidAppModuleDirectories then finds all app modules`() {
        // Given
        val appModule1 =
            createAppModuleWithBuildFile(
                """
                plugins {
                    id("com.android.application")
                }
                """.trimIndent()
            )

        val nestedModule = File(tempDirectory, "feature").apply { mkdirs() }
        val appModule2 =
            createAppModuleWithBuildFile(
                File(nestedModule, "app"),
                """
                plugins {
                    id("com.android.application")
                }
                """.trimIndent()
            )

        // When
        val result = classUnderTest.findAndroidAppModuleDirectories(tempDirectory)

        // Then
        assertAppModulesFound(result, listOf(appModule1, appModule2))
    }

    private fun createAppModuleWithBuildFile(buildFileContent: String): File {
        val appModule = File(tempDirectory, "app").apply { mkdirs() }
        createBuildGradleKtsFile(appModule, buildFileContent)
        return appModule
    }

    private fun createAppModuleWithBuildFile(
        moduleDirectory: File,
        buildFileContent: String
    ): File {
        moduleDirectory.mkdirs()
        createBuildGradleKtsFile(moduleDirectory, buildFileContent)
        return moduleDirectory
    }

    private fun createAppModuleWithGroovyBuildFile(buildFileContent: String): File {
        val appModule = File(tempDirectory, "app").apply { mkdirs() }
        createBuildGradleFile(appModule, buildFileContent)
        return appModule
    }

    private fun createLibraryModuleWithBuildFile(buildFileContent: String) {
        val libraryModule = File(tempDirectory, "library").apply { mkdirs() }
        createBuildGradleKtsFile(libraryModule, buildFileContent)
    }

    private fun createVersionCatalogTestSetup(): File {
        val appModule = File(tempDirectory, "app").apply { mkdirs() }
        createBuildGradleKtsFile(
            appModule,
            """
            plugins {
                alias(libs.plugins.android.application)
            }
            """.trimIndent()
        )

        createVersionCatalogFile(
            tempDirectory,
            """
            [plugins]
            android-application = { 
                id = "com.android.application", 
                version = "8.0.0" 
            }
            """.trimIndent()
        )
        return appModule
    }

    private fun assertSingleAppModuleFound(
        result: List<File>,
        expectedAppModule: File
    ) {
        assertEquals(1, result.size)
        assertEquals(expectedAppModule.canonicalFile, result.first().canonicalFile)
    }

    private fun assertAppModulesFound(
        result: List<File>,
        expectedAppModules: List<File>
    ) {
        assertEquals(expectedAppModules.size, result.size)
        val canonicalPaths = result.map { it.canonicalFile }.toSet()
        val expectedCanonicalPaths = expectedAppModules.map { it.canonicalFile }.toSet()
        assertEquals(expectedCanonicalPaths, canonicalPaths)
    }

    private fun createBuildGradleKtsFile(
        directory: File,
        content: String
    ) {
        File(directory, "build.gradle.kts").writeText(content)
    }

    private fun createBuildGradleFile(
        directory: File,
        content: String
    ) {
        File(directory, "build.gradle").writeText(content)
    }

    private fun createVersionCatalogFile(
        projectRoot: File,
        content: String
    ) {
        val gradleDir = File(projectRoot, "gradle").apply { mkdirs() }
        File(gradleDir, "libs.versions.toml").writeText(content)
    }
}
