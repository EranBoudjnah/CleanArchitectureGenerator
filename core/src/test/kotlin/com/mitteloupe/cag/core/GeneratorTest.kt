package com.mitteloupe.cag.core

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
        val expectedPath = "presentation/src/main/java/com/example/feature/viewmodel/TestViewModelViewModel.kt"

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
        val expectedPath = "presentation/src/main/java/com/example/feature/viewmodel/TestViewModelViewModel.kt"

        // When
        classUnderTest.generateViewModel(request)
        val actualViewModelFile = File(invalidDirectory, expectedPath)

        // Then
        assertTrue("ViewModel file should be created", actualViewModelFile.exists())
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
        assertTrue(architectureRoot.exists())
        assertTrue(architectureRoot.isDirectory)

        val layers = listOf("domain", "presentation", "ui")
        layers.forEach { layer ->
            val layerDir = File(architectureRoot, "$layer/src/main/java")
            assertTrue("Layer $layer should exist", layerDir.exists())
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

        // When
        classUnderTest.generateArchitecture(request)

        // Then
        val architectureRoot = File(tempDirectory, "architecture")
        assertTrue(architectureRoot.exists())
        assertTrue(architectureRoot.isDirectory)

        val layers = listOf("domain", "presentation", "ui")
        layers.forEach { layer ->
            val layerDir = File(architectureRoot, "$layer/src/main/java")
            assertTrue("Layer $layer should exist", layerDir.exists())
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
}
