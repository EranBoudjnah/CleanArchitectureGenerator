package com.mitteloupe.cag.core

import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.BuildSrcContentCreator
import com.mitteloupe.cag.core.generation.ConfigurationFileCreator
import com.mitteloupe.cag.core.generation.DataLayerContentGenerator
import com.mitteloupe.cag.core.generation.DataSourceImplementationCreator
import com.mitteloupe.cag.core.generation.DataSourceInterfaceCreator
import com.mitteloupe.cag.core.generation.DataSourceModuleCreator
import com.mitteloupe.cag.core.generation.DomainLayerContentGenerator
import com.mitteloupe.cag.core.generation.GradleFileCreator
import com.mitteloupe.cag.core.generation.GradlePropertiesFileCreator
import com.mitteloupe.cag.core.generation.GradleWrapperCreator
import com.mitteloupe.cag.core.generation.KotlinFileCreator
import com.mitteloupe.cag.core.generation.PresentationLayerContentGenerator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.UiLayerContentGenerator
import com.mitteloupe.cag.core.generation.architecture.ArchitectureModulesContentGenerator
import com.mitteloupe.cag.core.generation.architecture.CoroutineModuleContentGenerator
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.request.GenerateArchitectureRequest
import com.mitteloupe.cag.core.request.GenerateFeatureRequest
import com.mitteloupe.cag.core.request.GenerateUseCaseRequest
import com.mitteloupe.cag.core.request.GenerateViewModelRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class GeneratorTest {
    private lateinit var classUnderTest: Generator

    private lateinit var temporaryDirectory: File

    @Before
    fun setUp() {
        temporaryDirectory = createTempDirectory(prefix = "test").toFile()
        classUnderTest = produceGenerator()
    }

    @Test(expected = GenerationException::class)
    fun `Given empty feature package name when generateFeature then throws exception`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )

        // When
        classUnderTest.generateFeature(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given null feature package name when generateFeature then throws exception`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = null,
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )

        // When
        classUnderTest.generateFeature(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given invalid feature package name when generateFeature then throws exception`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "...",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )

        // When
        classUnderTest.generateFeature(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given existing feature directory when generateFeature then throws exception`() {
        // Given
        val existingFeatureDir = File(temporaryDirectory, "features/testfeature")
        existingFeatureDir.mkdirs()
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.feature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )

        // When
        classUnderTest.generateFeature(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given existing file with feature name when generateFeature then throws exception`() {
        // Given
        val featuresDirectory = File(temporaryDirectory, "features")
        featuresDirectory.mkdirs()
        val existingFeatureFile = File(featuresDirectory, "testfeature")
        existingFeatureFile.createNewFile()
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.feature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )

        // When
        classUnderTest.generateFeature(request)

        // Then throws GenerationException
    }

    @Test
    fun `Given valid request when generateFeature then creates feature structure and returns success`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.feature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )

        // When
        classUnderTest.generateFeature(request)

        // Then
        val featureRoot = File(temporaryDirectory, "features/testfeature")
        assertTrue(featureRoot.exists())
        assertTrue(featureRoot.isDirectory)

        val layers = listOf("ui", "presentation", "domain", "data")
        layers.forEach { layer ->
            val layerDir = File(featureRoot, "$layer/src/main/java")
            assertTrue("Layer $layer should exist", layerDir.exists())
        }
    }

    @Test
    fun `Given valid use case request when generateUseCase then returns success`() {
        // Given
        val useCaseDir = File(temporaryDirectory, "src/main/java/com/example/feature/domain/usecase")
        useCaseDir.mkdirs()
        val request =
            GenerateUseCaseRequest(
                useCaseName = "TestUseCase",
                destinationDirectory = useCaseDir,
                inputDataType = "String",
                outputDataType = "Int"
            )

        // When
        classUnderTest.generateUseCase(request)

        // Then
    }

    @Test
    fun `Given existing directory when generateViewModel then returns success`() {
        // Given
        val viewModelDirectory = File(temporaryDirectory, "src/main/java/com/example/featurepresentation/viewmodel")
        viewModelDirectory.mkdirs()
        val request =
            GenerateViewModelRequest(
                viewModelName = "TestViewModel",
                destinationDirectory = viewModelDirectory,
                featurePackageName = "com.example.feature",
                viewModelPackageName = "com.example.feature.presentation.viewmodel",
                projectNamespace = "com.example"
            )
        val expectedPath = "TestViewModel.kt"

        // When
        classUnderTest.generateViewModel(request)
        val actualViewModelFile = File(viewModelDirectory, expectedPath)

        // Then
        assertTrue("ViewModel file should be created", actualViewModelFile.exists())
    }

    @Test
    fun `Given new directory when generateViewModel then creates ViewModel in correct presentation layer structure`() {
        // Given
        val targetDirectory = File(temporaryDirectory, "features/samplefeature")
        val request =
            GenerateViewModelRequest(
                viewModelName = "SampleViewModel",
                destinationDirectory = targetDirectory,
                featurePackageName = "com.example.app",
                viewModelPackageName = "com.example.app.examplefeature",
                projectNamespace = "com.example.app"
            )
        val expectedViewModelFile = File(targetDirectory, "SampleViewModel.kt")

        // When
        classUnderTest.generateViewModel(request)

        // Then
        assertTrue("ViewModel should be a file", expectedViewModelFile.isFile)
    }

    @Test
    fun `Given valid feature request when generateFeature then creates presentation model in correct directory structure`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.app.testfeature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example.app",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )
        val featureRoot = File(temporaryDirectory, "features/testfeature")
        val expectedPresentationModelFile =
            File(
                featureRoot,
                "presentation/src/main/java/com/example/app/testfeature/presentation/model/StubPresentationModel.kt"
            )

        // When
        classUnderTest.generateFeature(request)

        // Then
        assertTrue("Presentation model should be a file", expectedPresentationModelFile.isFile)
    }

    @Test
    fun `Given valid feature request when generateFeature then creates presentation mapper in correct directory structure`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.app.testfeature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example.app",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )
        val featureRoot = File(temporaryDirectory, "features/testfeature")
        val expectedPresentationMapperFile =
            File(
                featureRoot,
                "presentation/src/main/java/com/example/app/testfeature/presentation/mapper/StubPresentationMapper.kt"
            )

        // When
        classUnderTest.generateFeature(request)

        // Then
        assertTrue("Presentation mapper should be a file", expectedPresentationMapperFile.isFile)
    }

    @Test(expected = GenerationException::class)
    fun `Given empty architecture package name when generateArchitecture then throws exception`() {
        // Given
        val request =
            GenerateArchitectureRequest(
                architecturePackageName = "",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true
            )

        // When
        classUnderTest.generateArchitecture(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given invalid architecture package name when generateArchitecture then throws exception`() {
        // Given
        val request =
            GenerateArchitectureRequest(
                architecturePackageName = "...",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true
            )

        // When
        classUnderTest.generateArchitecture(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given existing architecture directory when generateArchitecture then throws exception`() {
        // Given
        val existingArchitectureDir = File(temporaryDirectory, "architecture")
        existingArchitectureDir.mkdirs()
        val request =
            GenerateArchitectureRequest(
                architecturePackageName = "com.example.architecture",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true
            )

        // When
        classUnderTest.generateArchitecture(request)

        // Then throws GenerationException
    }

    @Test
    fun `Given valid architecture request when generateArchitecture then creates architecture structure and returns success`() {
        // Given
        val request =
            GenerateArchitectureRequest(
                architecturePackageName = "com.example.architecture",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true
            )

        // When
        classUnderTest.generateArchitecture(request)

        // Then
        val architectureRoot = File(temporaryDirectory, "architecture")
        assertTrue(architectureRoot.isDirectory)

        val layers = listOf("domain", "presentation", "ui")
        layers.forEach { layer ->
            val layerDirectory = File(architectureRoot, "$layer/src/main/java")
            assertTrue("Layer $layer should exist", layerDirectory.isDirectory)
        }
    }

    @Test
    fun `Given request with detekt, ktlint when generateArchitecture then creates architecture structure`() {
        // Given
        val request =
            GenerateArchitectureRequest(
                architecturePackageName = "com.example.architecture",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true,
                enableKtlint = true,
                enableDetekt = true
            )
        val architectureRoot = File(temporaryDirectory, "architecture")

        // When
        classUnderTest.generateArchitecture(request)

        // Then
        assertTrue(architectureRoot.isDirectory)

        val layers = listOf("domain", "presentation", "ui")
        layers.forEach { layer ->
            val layerDirectory = File(architectureRoot, "$layer/src/main/java")
            assertTrue("Layer $layer should exist", layerDirectory.isDirectory)
        }
    }

    @Test
    fun `Given valid data source request when generateDataSource then returns success`() {
        // Given
        val dataSourceName = "TestDataSource"
        val projectNamespace = "com.example"
        val useKtor = true
        val useRetrofit = false

        // When
        classUnderTest.generateDataSource(
            destinationRootDirectory = temporaryDirectory,
            dataSourceName = dataSourceName,
            projectNamespace = projectNamespace,
            useKtor = useKtor,
            useRetrofit = useRetrofit
        )

        // Then

        val datasourceRoot = File(temporaryDirectory, "datasource")
        assertTrue(datasourceRoot.exists())

        val sourceModule = File(datasourceRoot, "source")
        val implementationModule = File(datasourceRoot, "implementation")
        assertTrue(sourceModule.exists())
        assertTrue(implementationModule.exists())
    }

    @Test
    fun `Given existing version catalog without compose versions when generateFeature with compose enabled then adds compose versions`() {
        val gradleDirectory = File(temporaryDirectory, "gradle")
        gradleDirectory.mkdirs()
        val catalogFile = File(gradleDirectory, "libs.versions.toml")
        val existingVersions = """kotlin = "2.2.10"
            compileSdk = "35"
            minSdk = "24"
            junit4 = "4.13.2"
            ksp = "2.2.10-2.0.2""""
        val existingLibraries = """androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.12.0" }"""
        catalogFile.writeText(
            """
            [versions]
            $existingVersions

            [libraries]
            $existingLibraries
            """.trimIndent()
        )

        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.feature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )
        val expectedContent =
            """
            [versions]
            $existingVersions
            targetSdk = "35"
            androidGradlePlugin = "8.12.2"
            composeBom = "2025.08.01"
            composeNavigation = "2.9.3"
            androidxActivityCompose = "1.8.2"

            [libraries]
            $existingLibraries
            compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
            compose-ui = { module = "androidx.compose.ui:ui" }
            compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
            compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
            compose-material3 = { module = "androidx.compose.material3:material3" }
            compose-navigation = { module = "androidx.navigation:navigation-compose", version.ref = "composeNavigation" }
            compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
            compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
            androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidxActivityCompose" }

            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

            """.trimIndent()

        // When
        classUnderTest.generateFeature(request)
        val catalogContent = catalogFile.readText()

        // Then
        assertEquals("Generated catalog should match expected content", expectedContent, catalogContent)
    }

    @Test
    fun `Given compose disabled feature request when generateFeature then does not update version catalog with compose versions`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.feature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = false,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )
        val catalogFile = File(temporaryDirectory, "gradle/libs.versions.toml")

        // When
        classUnderTest.generateFeature(request)

        // Then
        val catalogContent = catalogFile.readText()
        val expectedContent =
            """
            [versions]
            compileSdk = "35"
            minSdk = "24"
            targetSdk = "35"
            androidGradlePlugin = "8.12.2"
            kotlin = "2.2.10"
            ksp = "2.2.10-2.0.2"
            
            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }

            """.trimIndent()

        assertEquals(
            "Generated catalog should match expected content (no compose)",
            expectedContent,
            catalogContent
        )
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given existing version catalog, no compose versions, compose disabled when generateFeature then does not add compose versions`() {
        // Given
        val gradleDirectory = File(temporaryDirectory, "gradle")
        gradleDirectory.mkdirs()
        val catalogFile = File(gradleDirectory, "libs.versions.toml")
        val existingVersions = """kotlin = "2.2.10"
            compileSdk = "35"
            minSdk = "24"
            junit4 = "4.13.2"
            ksp = "2.2.10-2.0.2""""
        val existingLibraries = """androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.12.0" }"""
        catalogFile.writeText(
            """
            [versions]
            $existingVersions

            [libraries]
            $existingLibraries
            """.trimIndent()
        )

        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.feature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example",
                enableCompose = false,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = null
            )
        val expectedContent =
            """
            [versions]
            $existingVersions
            targetSdk = "35"
            androidGradlePlugin = "8.12.2"

            [libraries]
            $existingLibraries

            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }

            """.trimIndent()

        // When
        classUnderTest.generateFeature(request)
        val catalogContent = catalogFile.readText()

        // Then
        assertEquals("Generated catalog should match expected content (no compose)", expectedContent, catalogContent)
    }

    @Test
    fun `Given explicit app module directory when generateFeature then updates only specified module`() {
        // Given
        val appModuleA = File(temporaryDirectory, "appA").apply { mkdirs() }
        File(appModuleA, "build.gradle.kts").writeText("")
        val appModuleB = File(temporaryDirectory, "appB").apply { mkdirs() }
        File(appModuleB, "build.gradle.kts").writeText("")

        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.app.testfeature",
                destinationRootDirectory = temporaryDirectory,
                projectNamespace = "com.example.app",
                enableCompose = true,
                enableKtlint = false,
                enableDetekt = false,
                appModuleDirectory = appModuleB
            )

        // When
        classUnderTest.generateFeature(request)

        // Then
        val expectedDiFileInModuleB =
            File(appModuleB, "src/main/java/com/example/app/di/TestFeatureModule.kt")
        assertTrue("DI module should be created in the selected app module", expectedDiFileInModuleB.isFile)

        val unexpectedDiFileInModuleA =
            File(appModuleA, "src/main/java/com/example/app/di/TestFeatureModule.kt")
        assertFalse("DI module should not be created in non-selected module", unexpectedDiFileInModuleA.exists())
    }

    @Test
    fun `Given ktlint, detekt enabled when generateFeature then generates domain Gradle file`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "Sample",
                featurePackageName = "com.example.sample",
                projectNamespace = "com.example",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true,
                enableKtlint = true,
                enableDetekt = true,
                appModuleDirectory = null
            )
        val featureRoot = File(temporaryDirectory, "features/sample")
        val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.architecture.domain)
}
"""

        // When
        classUnderTest.generateFeature(request)
        val domainGradle = File(featureRoot, "domain/build.gradle.kts").readText()

        // Then
        assertEquals(expectedContent, domainGradle)
    }

    @Test
    fun `Given ktlint, detekt enabled when generateFeature then generates presentation Gradle file`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "Sample",
                featurePackageName = "com.example.sample",
                projectNamespace = "com.example",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true,
                enableKtlint = true,
                enableDetekt = true,
                appModuleDirectory = null
            )
        val featureRoot = File(temporaryDirectory, "features/sample")
        val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.features.sample.domain)
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)
}
"""

        // When
        classUnderTest.generateFeature(request)
        val presentationGradle = File(featureRoot, "presentation/build.gradle.kts").readText()

        // Then
        assertEquals(expectedContent, presentationGradle)
    }

    @Test
    fun `Given ktlint, detekt enabled when generateFeature then generates data Gradle file`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "Sample",
                featurePackageName = "com.example.sample",
                projectNamespace = "com.example",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true,
                enableKtlint = true,
                enableDetekt = true,
                appModuleDirectory = null
            )
        val featureRoot = File(temporaryDirectory, "features/sample")
        val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.features.sample.domain)
    implementation(projects.architecture.domain)

    implementation(projects.datasource.source)
}
"""

        // When
        classUnderTest.generateFeature(request)
        val dataGradle = File(featureRoot, "data/build.gradle.kts").readText()

        // Then
        assertEquals(expectedContent, dataGradle)
    }

    @Test
    fun `Given ktlint, detekt enabled when generateFeature then generates UI Gradle file`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "Sample",
                featurePackageName = "com.example.sample",
                projectNamespace = "com.example",
                destinationRootDirectory = temporaryDirectory,
                enableCompose = true,
                enableKtlint = true,
                enableDetekt = true,
                appModuleDirectory = null
            )
        val featureRoot = File(temporaryDirectory, "features/sample")
        val expectedContent = """plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.example.sample.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.features.sample.presentation)
    implementation(projects.architecture.ui)
    implementation(projects.architecture.presentation)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.navigation)
    implementation(libs.compose.ui.tooling.preview)
}
"""

        // When
        classUnderTest.generateFeature(request)
        val uiGradle = File(featureRoot, "ui/build.gradle.kts").readText()

        // Then
        assertEquals(expectedContent, uiGradle)
    }

    private fun produceGenerator(): Generator {
        val fileCreator = FileCreator(FakeFileSystemBridge())
        val directoryFinder = DirectoryFinder()
        val kotlinFileCreator = KotlinFileCreator(fileCreator)
        val gradleFileCreator = GradleFileCreator(fileCreator)
        val catalogUpdater = VersionCatalogUpdater(fileCreator)
        return Generator(
            GradleFileCreator(fileCreator),
            GradleWrapperCreator(fileCreator),
            AppModuleContentGenerator(fileCreator, directoryFinder),
            BuildSrcContentCreator(fileCreator),
            ConfigurationFileCreator(fileCreator),
            UiLayerContentGenerator(kotlinFileCreator),
            PresentationLayerContentGenerator(kotlinFileCreator, fileCreator),
            DomainLayerContentGenerator(kotlinFileCreator),
            DataLayerContentGenerator(kotlinFileCreator),
            DataSourceModuleCreator(fileCreator),
            DataSourceInterfaceCreator(fileCreator),
            DataSourceImplementationCreator(fileCreator),
            GradlePropertiesFileCreator(fileCreator),
            ArchitectureModulesContentGenerator(gradleFileCreator, catalogUpdater),
            CoroutineModuleContentGenerator(gradleFileCreator, catalogUpdater),
            VersionCatalogUpdater(fileCreator),
            SettingsFileUpdater(fileCreator)
        )
    }
}
