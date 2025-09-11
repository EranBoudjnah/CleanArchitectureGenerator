package com.mitteloupe.cag.core.generation

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class DomainLayerContentGeneratorTest {
    private lateinit var classUnderTest: DomainLayerContentGenerator
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        classUnderTest = DomainLayerContentGenerator()
        tempDirectory = createTempDirectory(prefix = "DomainLayerContentGeneratorTest").toFile()
    }

    @Test
    fun `Given valid parameters when generateDomainLayer then creates use case with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        featureRoot.mkdirs()
        val expectedUseCaseContent = """package $featurePackageName.domain.usecase

import com.example.architecture.domain.usecase.UseCase
import com.example.feature.domain.model.StubDomainModel
import com.example.feature.domain.repository.PerformActionRepository

class PerformActionUseCase(private val performExampleRepository: PerformActionRepository) : UseCase<StubDomainModel, StubDomainModel> {
    override fun execute(input: StubDomainModel, onResult: (StubDomainModel) -> Unit) {
        onResult(performExampleRepository.perform(input))
    }
}
"""
        val useCaseFile = File(featureRoot, "domain/src/main/java/com/example/feature/domain/usecase/PerformActionUseCase.kt")

        // When
        classUnderTest.generateDomainLayer(featureRoot, projectNamespace, featurePackageName)

        // Then
        assertEquals("Use case file should have exact content", expectedUseCaseContent, useCaseFile.readText())
    }

    @Test
    fun `Given valid parameters when generateDomainLayer then creates repository interface with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        featureRoot.mkdirs()
        val expectedRepositoryContent = """package $featurePackageName.domain.repository

import com.example.feature.domain.model.StubDomainModel

interface PerformActionRepository {
    fun perform(input: StubDomainModel): StubDomainModel
}
"""
        val repositoryFile = File(featureRoot, "domain/src/main/java/com/example/feature/domain/repository/PerformActionRepository.kt")

        // When
        classUnderTest.generateDomainLayer(featureRoot, projectNamespace, featurePackageName)

        // Then
        assertEquals("Repository file should have exact content", expectedRepositoryContent, repositoryFile.readText())
    }

    @Test
    fun `Given valid parameters when generateDomainLayer then creates mpdel with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        featureRoot.mkdirs()
        val expectedModelContent = """package $featurePackageName.domain.model

data class StubDomainModel(
    val id: String
)
"""
        val modelFile = File(featureRoot, "domain/src/main/java/com/example/feature/domain/model/StubDomainModel.kt")

        // When
        classUnderTest.generateDomainLayer(featureRoot, projectNamespace, featurePackageName)

        // Then
        assertEquals("Model file should have exact content", expectedModelContent, modelFile.readText())
    }

    @Test
    fun `Given valid parameters when generateUseCase then creates use case file with correct content`() {
        // Given
        val destinationDirectory = File(tempDirectory, "src/main/java/com/example/feature/domain/usecase")
        destinationDirectory.mkdirs()
        val useCaseName = "GetUserUseCase"
        val inputDataType = "String"
        val outputDataType = "User"
        val expectedContent = """package com.example.feature.domain.usecase

import com.example.feature.architecture.domain.usecase.UseCase

class GetUserUseCase() : UseCase<String, User> {
    override fun execute(input: String, onResult: (User) -> Unit) {
        onResult(TODO("Evaluate result"))
    }
}
"""
        val useCaseFile = File(destinationDirectory, "GetUserUseCase.kt")

        // When
        classUnderTest.generateUseCase(destinationDirectory, useCaseName, inputDataType, outputDataType)

        // Then
        assertEquals("Use case file should have exact content", expectedContent, useCaseFile.readText())
    }

    @Test
    fun `Given use case without input and output types when generateUseCase then creates use case file with correct content`() {
        // Given
        val destinationDirectory = File(tempDirectory, "src/main/java/com/example/feature/domain/usecase")
        destinationDirectory.mkdirs()
        val useCaseName = "PerformActionUseCase"
        val expectedContent = """package com.example.feature.domain.usecase

import com.example.feature.architecture.domain.usecase.UseCase
import com.example.feature.domain.model.StubDomainModel

class PerformActionUseCase() : UseCase<StubDomainModel, StubDomainModel> {
    override fun execute(input: StubDomainModel, onResult: (StubDomainModel) -> Unit) {
        onResult(TODO("Evaluate result"))
    }
}
"""
        val useCaseFile = File(destinationDirectory, "PerformActionUseCase.kt")

        // When
        classUnderTest.generateUseCase(destinationDirectory, useCaseName)

        // Then
        assertEquals("Use case file should have exact content", expectedContent, useCaseFile.readText())
    }

    @Test
    fun `Given existing files when generateDomainLayer then does not overwrite existing files`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        featureRoot.mkdirs()

        val domainRoot = File(featureRoot, "domain")
        val useCaseDirectory = File(domainRoot, "src/main/java/com/example/feature/usecase")
        useCaseDirectory.mkdirs()
        val existingUseCaseFile = File(useCaseDirectory, "PerformActionUseCase.kt")
        val originalContent = "original content"
        existingUseCaseFile.writeText(originalContent)

        // When
        classUnderTest.generateDomainLayer(featureRoot, projectNamespace, featurePackageName)

        // Then
        assertEquals("Existing use case file should not be overwritten", originalContent, existingUseCaseFile.readText())
    }

    @Test
    fun `Given existing files when generateUseCase then does not overwrite existing files`() {
        // Given
        val destinationDirectory = File(tempDirectory, "src/main/java/com/example/feature/domain/usecase")
        destinationDirectory.mkdirs()
        val useCaseName = "GetUserUseCase"
        val existingFile = File(destinationDirectory, "GetUserUseCase.kt")
        val originalContent = "original content"
        existingFile.writeText(originalContent)

        // When
        classUnderTest.generateUseCase(destinationDirectory, useCaseName)

        // Then
        assertEquals("Existing use case file should not be overwritten", originalContent, existingFile.readText())
    }
}
