package com.mitteloupe.cag.core.generation.gradle

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.PluginRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

private const val ALIAS_HILT = "alias(libs.plugins.hilt)"
private const val DEFAULT_PLUGINS = """alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    $ALIAS_HILT apply false
    alias(libs.plugins.ksp) apply false"""

class GradleFileCreatorTest {
    private lateinit var classUnderTest: GradleFileCreator

    private lateinit var fileCreator: FileCreator

    @Before
    fun setUp() {
        fileCreator = FileCreator(FakeFileSystemBridge())
        classUnderTest = GradleFileCreator(fileCreator)
    }

    @Test
    fun `Given module directory exists and file missing when writeGradleFileIfMissing then creates file with content`() {
        // Given
        val featureRoot = createTempDirectory(prefix = "featureRoot").toFile()
        val givenLayer = "data"
        val moduleDirectory = File(featureRoot, givenLayer)
        moduleDirectory.mkdirs()
        val givenContent = "plugins { kotlin(\"jvm\") }\n"

        // When
        classUnderTest.writeGradleFileIfMissing(featureRoot = featureRoot, layer = givenLayer) { givenContent }

        // Then
        val targetFile = File(moduleDirectory, "build.gradle.kts")
        assertEquals(givenContent, targetFile.readText())
    }

    @Test(expected = GenerationException::class)
    fun `Given module directory missing when writeGradleFileIfMissing then throws exception and does not create file`() {
        // Given
        val featureRoot = createTempDirectory(prefix = "featureRoot2").toFile()
        val givenLayer = "presentation"
        val givenContent = "// gradle script\n"

        // When
        classUnderTest.writeGradleFileIfMissing(featureRoot = featureRoot, layer = givenLayer) { givenContent }

        // Then throws GenerationException
    }

    @Test
    fun `Given file already exists when writeGradleFileIfMissing then does nothing`() {
        // Given
        val featureRoot = createTempDirectory(prefix = "featureRoot3").toFile()
        val givenLayer = "ui"
        val moduleDirectory = File(featureRoot, givenLayer)
        moduleDirectory.mkdirs()
        val targetFile = File(moduleDirectory, "build.gradle.kts")
        val initialContent = "// existing content\n"
        targetFile.writeText(initialContent)
        val newContent = "// new content that should be ignored\n"

        // When
        classUnderTest.writeGradleFileIfMissing(featureRoot = featureRoot, layer = givenLayer) { newContent }

        // Then
        assertEquals(initialContent, targetFile.readText())
    }

    @Test
    fun `Given no compose when writeAppGradleFile then generates app gradle file without compose`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "featureRoot3").toFile()
        val packageName = "com.brand.app"
        val moduleDirectory = File(projectRoot, "app")
        moduleDirectory.mkdirs()
        val targetFile = File(moduleDirectory, "build.gradle.kts")
        val catalog = VersionCatalogUpdater(fileCreator)
        val expectedContent =
            """
            import org.jetbrains.kotlin.gradle.dsl.JvmTarget

            plugins {
                alias(libs.plugins.android.application)
                alias(libs.plugins.kotlin.android)
                alias(libs.plugins.ksp)
                $ALIAS_HILT
            }

            kotlin {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }

            android {
                namespace = "com.brand.app"
                compileSdk = libs.versions.compileSdk.get().toInt()

                defaultConfig {
                    applicationId = "com.brand.app"
                    minSdk = libs.versions.minSdk.get().toInt()
                    targetSdk = libs.versions.targetSdk.get().toInt()
                    versionCode = 1
                    versionName = "1.0"

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                packaging {
                    resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
                }
            }
                
            dependencies {
                implementation(libs.material)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.hilt.android)
                ksp(libs.hilt.android.compiler)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.constraintlayout)
                implementation(libs.material)

                implementation(projects.architecture.ui)
                implementation(projects.architecture.presentation)
                implementation(projects.architecture.domain)
                implementation(projects.features.samplefeature.ui)
                implementation(projects.features.samplefeature.presentation)
                implementation(projects.features.samplefeature.domain)
                implementation(projects.features.samplefeature.data)
                implementation(projects.datasource.source)
                implementation(projects.datasource.implementation)
                    
                testImplementation(libs.test.junit)
                androidTestImplementation(libs.test.androidx.junit)
                androidTestImplementation(libs.test.androidx.espresso.core)
            }
            """.trimIndent()

        // When
        classUnderTest.writeAppGradleFile(
            projectRoot = projectRoot,
            packageName = packageName,
            enableHilt = true,
            enableCompose = false,
            catalog = catalog
        )

        // Then
        assertEquals(expectedContent, targetFile.readText())
    }

    @Test
    fun `Given no hilt when writeAppGradleFile then generates app gradle file without hilt`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "featureRoot3").toFile()
        val packageName = "com.brand.app"
        val moduleDirectory = File(projectRoot, "app")
        moduleDirectory.mkdirs()
        val targetFile = File(moduleDirectory, "build.gradle.kts")
        val catalog = VersionCatalogUpdater(fileCreator)
        val expectedContent =
            """
            import org.jetbrains.kotlin.gradle.dsl.JvmTarget

            plugins {
                alias(libs.plugins.android.application)
                alias(libs.plugins.kotlin.android)
                alias(libs.plugins.ksp)
            }

            kotlin {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }

            android {
                namespace = "com.brand.app"
                compileSdk = libs.versions.compileSdk.get().toInt()

                defaultConfig {
                    applicationId = "com.brand.app"
                    minSdk = libs.versions.minSdk.get().toInt()
                    targetSdk = libs.versions.targetSdk.get().toInt()
                    versionCode = 1
                    versionName = "1.0"

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                packaging {
                    resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
                }
            }
                
            dependencies {
                implementation(libs.material)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.hilt.android)
                ksp(libs.hilt.android.compiler)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.constraintlayout)
                implementation(libs.material)

                implementation(projects.architecture.ui)
                implementation(projects.architecture.presentation)
                implementation(projects.architecture.domain)
                implementation(projects.features.samplefeature.ui)
                implementation(projects.features.samplefeature.presentation)
                implementation(projects.features.samplefeature.domain)
                implementation(projects.features.samplefeature.data)
                implementation(projects.datasource.source)
                implementation(projects.datasource.implementation)
                    
                testImplementation(libs.test.junit)
                androidTestImplementation(libs.test.androidx.junit)
                androidTestImplementation(libs.test.androidx.espresso.core)
            }
            """.trimIndent()

        // When
        classUnderTest.writeAppGradleFile(
            projectRoot = projectRoot,
            packageName = packageName,
            enableHilt = false,
            enableCompose = false,
            catalog = catalog
        )

        // Then
        assertEquals(expectedContent, targetFile.readText())
    }

    @Test
    fun `Given compose when writeAppGradleFile then generates app gradle file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "featureRoot3").toFile()
        val packageName = "com.awesome.app"
        val moduleDirectory = File(projectRoot, "app")
        moduleDirectory.mkdirs()
        val targetFile = File(moduleDirectory, "build.gradle.kts")
        val catalog = VersionCatalogUpdater(fileCreator)
        val expectedContent =
            """
            import org.jetbrains.kotlin.gradle.dsl.JvmTarget

            plugins {
                alias(libs.plugins.android.application)
                alias(libs.plugins.kotlin.android)
                alias(libs.plugins.ksp)
                $ALIAS_HILT
                alias(libs.plugins.compose.compiler)
            }

            kotlin {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }

            android {
                namespace = "com.awesome.app"
                compileSdk = libs.versions.compileSdk.get().toInt()

                defaultConfig {
                    applicationId = "com.awesome.app"
                    minSdk = libs.versions.minSdk.get().toInt()
                    targetSdk = libs.versions.targetSdk.get().toInt()
                    versionCode = 1
                    versionName = "1.0"

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                buildFeatures {
                    compose = true
                }
                packaging {
                    resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
                }
            }
                
            dependencies {
                implementation(libs.material)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.hilt.android)
                ksp(libs.hilt.android.compiler)
                implementation(libs.androidx.activity.compose)
                implementation(platform(libs.compose.bom))
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.material3)
                debugImplementation(libs.compose.ui.tooling)
                debugImplementation(libs.compose.ui.test.manifest)

                implementation(projects.architecture.ui)
                implementation(projects.architecture.presentation)
                implementation(projects.architecture.domain)
                implementation(projects.features.samplefeature.ui)
                implementation(projects.features.samplefeature.presentation)
                implementation(projects.features.samplefeature.domain)
                implementation(projects.features.samplefeature.data)
                implementation(projects.datasource.source)
                implementation(projects.datasource.implementation)
                    
                testImplementation(libs.test.junit)
                androidTestImplementation(libs.test.androidx.junit)
                androidTestImplementation(libs.test.androidx.espresso.core)
            }
            """.trimIndent()

        // When
        classUnderTest.writeAppGradleFile(
            projectRoot = projectRoot,
            packageName = packageName,
            enableHilt = true,
            enableCompose = true,
            catalog = catalog
        )

        // Then
        assertEquals(expectedContent, targetFile.readText())
    }

    @Test
    fun `Given no ktlint, no detekt when writeProjectGradleFile then generates app gradle file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "featureRoot").toFile()
        val targetFile = File(projectRoot, "build.gradle.kts")
        val catalog = VersionCatalogUpdater(fileCreator)
        val expectedContent =
            """// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    $DEFAULT_PLUGINS
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xskip-prerelease-check")
        }
    }
}
"""

        // When
        classUnderTest.writeProjectGradleFile(
            projectRoot = projectRoot,
            enableHilt = true,
            enableKtlint = false,
            enableDetekt = false,
            catalog = catalog
        )

        // Then
        assertEquals(expectedContent, targetFile.readText())
    }

    @Test
    fun `Given no hilt when writeProjectGradleFile then generates app gradle file without hilt`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "featureRoot").toFile()
        val targetFile = File(projectRoot, "build.gradle.kts")
        val catalog = VersionCatalogUpdater(fileCreator)
        val expectedContent =
            """// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xskip-prerelease-check")
        }
    }
}
"""

        // When
        classUnderTest.writeProjectGradleFile(
            projectRoot = projectRoot,
            enableHilt = false,
            enableKtlint = false,
            enableDetekt = false,
            catalog = catalog
        )

        // Then
        assertEquals(expectedContent, targetFile.readText())
    }

    @Test
    fun `Given ktlint, no detekt when writeProjectGradleFile then generates app gradle file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "featureRoot").toFile()
        val targetFile = File(projectRoot, "build.gradle.kts")
        val catalog = givenVersionCatalog()
        val expectedContent =
            """// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    $DEFAULT_PLUGINS
    alias(libs.plugins.ktlint) apply false
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xskip-prerelease-check")
        }
    }
}
"""

        // When
        classUnderTest.writeProjectGradleFile(
            projectRoot = projectRoot,
            enableHilt = true,
            enableKtlint = true,
            enableDetekt = false,
            catalog = catalog
        )

        // Then
        assertEquals(expectedContent, targetFile.readText())
    }

    @Test
    fun `Given detekt, no ktlint when writeProjectGradleFile then generates app gradle file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "featureRoot").toFile()
        val targetFile = File(projectRoot, "build.gradle.kts")
        val catalog = givenVersionCatalog()
        val expectedContent =
            """// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    $DEFAULT_PLUGINS
    alias(libs.plugins.detekt) apply false
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xskip-prerelease-check")
        }
    }
}
"""

        // When
        classUnderTest.writeProjectGradleFile(
            projectRoot = projectRoot,
            enableHilt = true,
            enableKtlint = false,
            enableDetekt = true,
            catalog = catalog
        )

        // Then
        assertEquals(expectedContent, targetFile.readText())
    }

    @Test
    fun `Given ktlint, detekt when writeProjectGradleFile then generates app gradle file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "featureRoot").toFile()
        val targetFile = File(projectRoot, "build.gradle.kts")
        val catalog = givenVersionCatalog()
        val expectedContent =
            """// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    $DEFAULT_PLUGINS
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xskip-prerelease-check")
        }
    }
}
"""

        // When
        classUnderTest.writeProjectGradleFile(
            projectRoot = projectRoot,
            enableHilt = true,
            enableKtlint = true,
            enableDetekt = true,
            catalog = catalog
        )

        // Then
        assertEquals(expectedContent, targetFile.readText())
    }

    private fun givenVersionCatalog(): VersionCatalogReader =
        mockk<VersionCatalogUpdater>().apply {
            givenVersionCatalogPlugin(PluginConstants.ANDROID_APPLICATION, "android.application")
            givenVersionCatalogPlugin(PluginConstants.ANDROID_LIBRARY, "android.library")
            givenVersionCatalogPlugin(PluginConstants.KOTLIN_ANDROID, "kotlin.android")
            givenVersionCatalogPlugin(PluginConstants.KOTLIN_JVM, "kotlin.jvm")
            givenVersionCatalogPlugin(PluginConstants.KTLINT, "ktlint")
            givenVersionCatalogPlugin(PluginConstants.DETEKT, "detekt")
            givenVersionCatalogPlugin(PluginConstants.HILT_ANDROID, "hilt")
            givenVersionCatalogPlugin(PluginConstants.KSP, "ksp")
        }

    private fun VersionCatalogUpdater.givenVersionCatalogPlugin(
        pluginRequirement: PluginRequirement,
        alias: String
    ) {
        every { isPluginAvailable(pluginRequirement) } returns true
        every { getResolvedPluginAliasFor(pluginRequirement) } returns alias
    }
}
