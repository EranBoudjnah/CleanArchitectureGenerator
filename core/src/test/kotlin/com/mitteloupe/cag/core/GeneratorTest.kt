package com.mitteloupe.cag.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class GeneratorTest {
    private lateinit var classUnderTest: Generator
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        tempDirectory = createTempDirectory(prefix = "test").toFile()
        classUnderTest = Generator()
    }

    @Test(expected = GenerationException::class)
    fun `Given empty feature package name when generateFeature then throws exception`() {
        // Given
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "",
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = true
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
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = true
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
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = true
            )

        // When
        classUnderTest.generateFeature(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given existing feature directory when generateFeature then throws exception`() {
        // Given
        val existingFeatureDir = File(tempDirectory, "features/testfeature")
        existingFeatureDir.mkdirs()
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.feature",
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = true
            )

        // When
        classUnderTest.generateFeature(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given existing file with feature name when generateFeature then throws exception`() {
        // Given
        val featuresDirectory = File(tempDirectory, "features")
        featuresDirectory.mkdirs()
        val existingFeatureFile = File(featuresDirectory, "testfeature")
        existingFeatureFile.createNewFile()
        val request =
            GenerateFeatureRequest(
                featureName = "TestFeature",
                featurePackageName = "com.example.feature",
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = true
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
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = true
            )

        // When
        classUnderTest.generateFeature(request)

        // Then
        val featureRoot = File(tempDirectory, "features/testfeature")
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
        val useCaseDir = File(tempDirectory, "src/main/java/com/example/feature/domain/usecase")
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
    fun `Given valid view model request when generateViewModel then returns success`() {
        // Given
        val featureRoot = File(tempDirectory, "src/main/java/com/example/feature")
        val viewModelDirectory = File(featureRoot, "presentation/viewmodel")
        viewModelDirectory.mkdirs()
        val request =
            GenerateViewModelRequest(
                viewModelName = "TestViewModel",
                destinationDirectory = viewModelDirectory,
                featurePackageName = "com.example.feature",
                projectNamespace = "com.example"
            )
        val expectedPath = "presentation/src/main/java/com/example/feature/presentation/viewmodel/TestViewModelViewModel.kt"

        // When
        classUnderTest.generateViewModel(request)
        val actualViewModelFile = File(viewModelDirectory, expectedPath)

        // Then
        assertTrue("ViewModel file should be created", actualViewModelFile.exists())
    }

    @Test
    fun `Given invalid directory when generateViewModel then creates directory and file`() {
        // Given
        val invalidDirectory = File(tempDirectory, "invalid/path")
        val request =
            GenerateViewModelRequest(
                viewModelName = "TestViewModel",
                destinationDirectory = invalidDirectory,
                featurePackageName = "com.example.feature",
                projectNamespace = "com.example"
            )
        val expectedPath = "presentation/src/main/java/com/example/feature/presentation/viewmodel/TestViewModelViewModel.kt"

        // When
        classUnderTest.generateViewModel(request)
        val actualViewModelFile = File(invalidDirectory, expectedPath)

        // Then
        assertTrue("ViewModel file should be created", actualViewModelFile.exists())
    }

    @Test
    fun `Given feature root directory when generateViewModel then creates ViewModel in correct presentation layer structure`() {
        // Given
        val featureRoot = File(tempDirectory, "features/samplefeature")
        val request =
            GenerateViewModelRequest(
                viewModelName = "SampleViewModel",
                destinationDirectory = featureRoot,
                featurePackageName = "com.example.app.examplefeature",
                projectNamespace = "com.example.app"
            )
        val expectedViewModelFile =
            File(
                featureRoot,
                "presentation/src/main/java/com/example/app/examplefeature/presentation/viewmodel/SampleViewModelViewModel.kt"
            )

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
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example.app",
                enableCompose = true
            )
        val featureRoot = File(tempDirectory, "features/testfeature")
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
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example.app",
                enableCompose = true
            )
        val featureRoot = File(tempDirectory, "features/testfeature")
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
                destinationRootDirectory = tempDirectory,
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
                destinationRootDirectory = tempDirectory,
                enableCompose = true
            )

        // When
        classUnderTest.generateArchitecture(request)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given existing architecture directory when generateArchitecture then throws exception`() {
        // Given
        val existingArchitectureDir = File(tempDirectory, "architecture")
        existingArchitectureDir.mkdirs()
        val request =
            GenerateArchitectureRequest(
                architecturePackageName = "com.example.architecture",
                destinationRootDirectory = tempDirectory,
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
                destinationRootDirectory = tempDirectory,
                enableCompose = true
            )

        // When
        classUnderTest.generateArchitecture(request)

        // Then
        val architectureRoot = File(tempDirectory, "architecture")
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
                destinationRootDirectory = tempDirectory,
                enableCompose = true,
                enableKtlint = true,
                enableDetekt = true
            )
        val architectureRoot = File(tempDirectory, "architecture")

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
            destinationRootDirectory = tempDirectory,
            dataSourceName = dataSourceName,
            projectNamespace = projectNamespace,
            useKtor = useKtor,
            useRetrofit = useRetrofit
        )

        // Then

        val datasourceRoot = File(tempDirectory, "datasource")
        assertTrue(datasourceRoot.exists())

        val sourceModule = File(datasourceRoot, "source")
        val implementationModule = File(datasourceRoot, "implementation")
        assertTrue(sourceModule.exists())
        assertTrue(implementationModule.exists())
    }

    @Test
    fun `Given existing version catalog without compose versions when generateFeature with compose enabled then adds compose versions`() {
        val gradleDirectory = File(tempDirectory, "gradle")
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
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = true
            )
        val expectedContent =
            """
            [versions]
            $existingVersions
            composeBom = "2025.08.01"
            composeNavigation = "2.9.3"
            composeCompiler = "1.5.8"

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
            androidx-activity-compose = { module = "androidx.activity:activity-compose", version = "1.8.2" }

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
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = false
            )
        val catalogFile = File(tempDirectory, "gradle/libs.versions.toml")

        // When
        classUnderTest.generateFeature(request)

        // Then
        val catalogContent = catalogFile.readText()
        val expectedContent =
            """
            [versions]
            kotlin = "2.2.10"
            compileSdk = "35"
            minSdk = "24"
            junit4 = "4.13.2"
            ksp = "2.2.10-2.0.2"
            
            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
            android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }

            """.trimIndent()

        println("Actual (compose disabled):")
        println(catalogContent)
        println("Expected (compose disabled):")
        println(expectedContent)

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
        val gradleDirectory = File(tempDirectory, "gradle")
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
                destinationRootDirectory = tempDirectory,
                projectNamespace = "com.example",
                enableCompose = false
            )
        val expectedContent =
            """
            [versions]
            $existingVersions

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
}
