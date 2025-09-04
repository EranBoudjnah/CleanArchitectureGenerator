package com.mitteloupe.cag.core.generation

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class AppModuleGradleUpdaterTest {
    private lateinit var classUnderTest: AppModuleGradleUpdater

    @Before
    fun setUp() {
        classUnderTest = AppModuleGradleUpdater()
    }

    @Test
    fun `Given KTS app, dependencies block, nested statements when update then inserts feature dependencies before closing brace`() {
        // Given
        val feature = "feature"
        val projectRoot = createTempDirectory(prefix = "projKtsNested").toFile()
        val givenGradleContent =
            """
            plugins {
                id("com.android.application")
                kotlin("android")
            }

            dependencies {
                implementation(libs.kotlin)
                androidTestImplementation(libs.test.mockito) {
                    exclude("net.bytebuddy")
                }
            }
            
            """.trimIndent()
        val buildFile = givenBuildFile(projectRoot, givenGradleContent)
        val startDirectory = File(projectRoot, "a/b").apply { mkdirs() }
        val expectedContent =
            """
            plugins {
                id("com.android.application")
                kotlin("android")
            }

            dependencies {
                implementation(libs.kotlin)
                androidTestImplementation(libs.test.mockito) {
                    exclude("net.bytebuddy")
                }
                implementation(projects.features.$feature.ui)
                implementation(projects.features.$feature.presentation)
                implementation(projects.features.$feature.domain)
                implementation(projects.features.$feature.data)
            }
            
            """.trimIndent()

        // When
        classUnderTest.updateAppModuleDependenciesIfPresent(startDirectory, feature)
        val content = buildFile.readText()

        // Then
        assertEquals(expectedContent, content)
    }

    @Test
    fun `Given KTS app without dependencies block when update then appends new block with feature dependencies`() {
        // Given
        val feature = "sample"
        val projectRoot = createTempDirectory(prefix = "projKtsNoBlock").toFile()
        val givenGradleContent =
            """
            plugins {
                id("com.android.application")
            }
            """.trimIndent()
        val buildFile = givenBuildFile(projectRoot, givenGradleContent)
        val startDirectory = File(projectRoot, "deep/nested").apply { mkdirs() }
        val expectedTail =
            """
            dependencies {
                implementation(projects.features.$feature.ui)
                implementation(projects.features.$feature.presentation)
                implementation(projects.features.$feature.domain)
                implementation(projects.features.$feature.data)
            }
            """.trimIndent()
        val expectedComplete = "$givenGradleContent\n$expectedTail\n"

        // When
        classUnderTest.updateAppModuleDependenciesIfPresent(startDirectory, feature)
        val content = buildFile.readText()

        // Then
        assertEquals(expectedComplete, content)
    }

    @Test
    fun `Given KTS app with some feature dependencies when update then only missing are added`() {
        // Given
        val feature = "feat"
        val projectRoot = createTempDirectory(prefix = "projKtsPartial").toFile()
        val givenGradleContent =
            """
            plugins { id("com.android.application") }
            dependencies {
                implementation(projects.features.$feature.ui)
            }
            
            """.trimIndent()
        val buildFile = givenBuildFile(projectRoot, givenGradleContent)
        val startDirectory = File(projectRoot, "work").apply { mkdirs() }
        val expectedContentPartial =
            """
            plugins { id("com.android.application") }
            dependencies {
                implementation(projects.features.$feature.ui)
                implementation(projects.features.$feature.presentation)
                implementation(projects.features.$feature.domain)
                implementation(projects.features.$feature.data)
            }
            
            """.trimIndent()

        // When
        classUnderTest.updateAppModuleDependenciesIfPresent(startDirectory, feature)
        val content = buildFile.readText()

        // Then
        assertEquals(expectedContentPartial, content)
    }

    @Test
    fun `Given Groovy app with dependencies block when update then inserts feature dependencies`() {
        // Given
        val feature = "demo"
        val projectRoot = createTempDirectory(prefix = "projGroovy").toFile()
        val givenGradleContent =
            """
            plugins {
                id 'com.android.application'
            }

            dependencies {
                implementation libs.kotlin
                androidTestImplementation libs.test.mockito
            }
            """.trimIndent() + "\n"
        val buildFile =
            givenBuildFile(
                projectRoot = projectRoot,
                givenGradleContent = givenGradleContent,
                gradleExtension = ""
            )
        val startDirectory = File(projectRoot, "start").apply { mkdirs() }
        val expectedContentGroovy =
            """
            plugins {
                id 'com.android.application'
            }

            dependencies {
                implementation libs.kotlin
                androidTestImplementation libs.test.mockito
                implementation(project(":features:$feature:ui"))
                implementation(project(":features:$feature:presentation"))
                implementation(project(":features:$feature:domain"))
                implementation(project(":features:$feature:data"))
            }
            """.trimIndent() + "\n"

        // When
        classUnderTest.updateAppModuleDependenciesIfPresent(startDirectory, feature)
        val content = buildFile.readText()

        // Then
        assertEquals(expectedContentGroovy, content)
    }

    private fun givenBuildFile(
        projectRoot: File,
        givenGradleContent: String,
        gradleExtension: String = ".kts"
    ): File {
        val appDir = File(projectRoot, "app").apply { mkdirs() }
        File(projectRoot, "settings.gradle$gradleExtension").writeText("rootProject.name = \"app\"\n")
        val buildFile = File(appDir, "build.gradle$gradleExtension").apply { writeText(givenGradleContent) }
        return buildFile
    }
}
