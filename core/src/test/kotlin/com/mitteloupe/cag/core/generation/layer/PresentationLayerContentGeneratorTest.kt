package com.mitteloupe.cag.core.generation.layer

import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.KotlinFileCreator
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class PresentationLayerContentGeneratorTest {
    private lateinit var classUnderTest: PresentationLayerContentGenerator
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        val fileCreator = FileCreator(FakeFileSystemBridge())
        classUnderTest = PresentationLayerContentGenerator(KotlinFileCreator(fileCreator), fileCreator)
        tempDirectory = createTempDirectory(prefix = "PresentationLayerContentGeneratorTest").toFile()
    }

    @Test
    fun `Given valid parameters when generatePresentationLayer then creates model with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        val featureName = "test"
        featureRoot.mkdirs()
        val expectedModelContent = """package com.example.feature.presentation.model

data class StubPresentationModel(
    val id: String
)
"""
        val modelFile =
            File(featureRoot, "presentation/src/main/java/com/example/feature/presentation/model/StubPresentationModel.kt")

        // When
        classUnderTest.generatePresentationLayer(featureRoot, projectNamespace, featurePackageName, featureName)

        // Then
        assertEquals("Model file should have exact content", expectedModelContent, modelFile.readText())
    }

    @Test
    fun `Given valid parameters when generatePresentationLayer then creates view state with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        val featureName = "test"
        featureRoot.mkdirs()
        val expectedViewStateContent = """package com.example.feature.presentation.model

sealed interface TestViewState {
    data object Loading : TestViewState

    data class Idle(val value: StubPresentationModel) : TestViewState

    data object Error : TestViewState
}
"""
        val viewStateFile =
            File(featureRoot, "presentation/src/main/java/com/example/feature/presentation/model/TestViewState.kt")

        // When
        classUnderTest.generatePresentationLayer(featureRoot, projectNamespace, featurePackageName, featureName)

        // Then
        assertEquals("View state file should have exact content", expectedViewStateContent, viewStateFile.readText())
    }

    @Test
    fun `Given valid parameters when generatePresentationLayer then creates domain to presentation mapper with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        val featureName = "test"
        featureRoot.mkdirs()
        val expectedDomainToPresentationMapperContent = """package com.example.feature.presentation.mapper

import com.example.feature.domain.model.StubDomainModel
import com.example.feature.presentation.model.StubPresentationModel

class StubPresentationMapper {
    fun toPresentation(stub: StubDomainModel): StubPresentationModel =
        StubPresentationModel(id = stub.id)
}
"""
        val domainToPresentationMapperFile =
            File(featureRoot, "presentation/src/main/java/com/example/feature/presentation/mapper/StubPresentationMapper.kt")

        // When
        classUnderTest.generatePresentationLayer(featureRoot, projectNamespace, featurePackageName, featureName)

        // Then
        assertEquals(
            "Domain to presentation mapper file should have exact content",
            expectedDomainToPresentationMapperContent,
            domainToPresentationMapperFile.readText()
        )
    }

    @Test
    fun `Given valid parameters when generatePresentationLayer then creates presentation to domain mapper with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        val featureName = "test"
        featureRoot.mkdirs()
        val expectedPresentationToDomainMapperContent = """package com.example.feature.presentation.mapper

import com.example.feature.domain.model.StubDomainModel
import com.example.feature.presentation.model.StubPresentationModel

class StubDomainMapper {
    fun toDomain(stub: StubPresentationModel): StubDomainModel =
        StubDomainModel(id = stub.id)
}
"""
        val presentationToDomainMapperFile =
            File(featureRoot, "presentation/src/main/java/com/example/feature/presentation/mapper/StubDomainMapper.kt")

        // When
        classUnderTest.generatePresentationLayer(featureRoot, projectNamespace, featurePackageName, featureName)

        // Then
        assertEquals(
            "Presentation to domain mapper file should have exact content",
            expectedPresentationToDomainMapperContent,
            presentationToDomainMapperFile.readText()
        )
    }

    @Test
    fun `Given valid parameters when generatePresentationLayer then creates view model with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        val featureName = "test"
        featureRoot.mkdirs()
        val expectedViewModelContent = """package com.example.feature.presentation.viewmodel

import com.example.architecture.domain.UseCaseExecutor
import com.example.architecture.presentation.notification.PresentationNotification
import com.example.architecture.presentation.viewmodel.BaseViewModel
import com.example.feature.domain.model.StubDomainModel
import com.example.feature.domain.usecase.PerformActionUseCase
import com.example.feature.presentation.mapper.StubDomainMapper
import com.example.feature.presentation.mapper.StubPresentationMapper
import com.example.feature.presentation.model.StubPresentationModel
import com.example.feature.presentation.model.TestViewState
import com.example.feature.presentation.model.TestViewState.Idle
import com.example.feature.presentation.model.TestViewState.Loading

class TestViewModel(
    private val performActionUseCase: PerformActionUseCase,
    private val stubDomainMapper: StubDomainMapper,
    private val stubPresentationMapper: StubPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<TestViewState, PresentationNotification>(useCaseExecutor) {
    fun onEnter(stub: StubPresentationModel) {
        updateViewState(Loading)
        performAction(stub)
    }

    private fun performAction(stub: StubPresentationModel) {
        val domainStub = stubDomainMapper.toDomain(stub)
        performActionUseCase(
            value = domainStub,
            onResult = { result ->
                presentStub(result)
            },
            onException = { exception ->
                presentError()
            }
        )
    }

    private fun presentStub(stub: StubDomainModel) {
        val presentationStub = stubPresentationMapper.toPresentation(stub)
        updateViewState(Idle(presentationStub))
    }

    private fun presentError() {
        updateViewState(TestViewState.Error)
    }
}
"""
        val viewModelFile = File(featureRoot, "presentation/src/main/java/com/example/feature/presentation/viewmodel/TestViewModel.kt")

        // When
        classUnderTest.generatePresentationLayer(
            featureRoot = featureRoot,
            projectNamespace = projectNamespace,
            featurePackageName = featurePackageName,
            featureName = featureName
        )

        // Then
        assertEquals("View model file should have exact content", expectedViewModelContent, viewModelFile.readText())
    }

    @Test
    fun `Given valid parameters when generatePresentationLayer then creates navigation event with correct subpath`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        val featureName = "test"
        featureRoot.mkdirs()
        val expectedNavigationEventContent = """package com.example.feature.presentation.navigation

import com.example.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface TestPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnEvent : TestPresentationNavigationEvent
}
"""
        val navigationEventFile =
            File(featureRoot, "presentation/src/main/java/com/example/feature/presentation/navigation/TestPresentationNavigationEvent.kt")

        // When
        classUnderTest.generatePresentationLayer(featureRoot, projectNamespace, featurePackageName, featureName)

        // Then
        assertEquals("Navigation event file should have exact content", expectedNavigationEventContent, navigationEventFile.readText())
    }

    @Test
    fun `Given valid parameters when generateViewModel then creates view model file with correct content`() {
        // Given
        val destinationDirectory = File(tempDirectory, "presentation")
        destinationDirectory.mkdirs()
        val viewModelName = "UserProfileViewModel"
        val projectNamespace = "com.example"
        val featurePackageName = "$projectNamespace.feature"
        val viewModelPackageName = "$featurePackageName.presentation.viewmodel"
        val expectedContent = """package com.example.feature.presentation.viewmodel

import com.example.architecture.domain.UseCaseExecutor
import com.example.architecture.presentation.notification.PresentationNotification
import com.example.architecture.presentation.viewmodel.BaseViewModel
import com.example.feature.domain.model.StubDomainModel
import com.example.feature.domain.usecase.PerformActionUseCase
import com.example.feature.presentation.mapper.StubDomainMapper
import com.example.feature.presentation.mapper.StubPresentationMapper
import com.example.feature.presentation.model.StubPresentationModel
import com.example.feature.presentation.model.UserProfileViewState
import com.example.feature.presentation.model.UserProfileViewState.Idle
import com.example.feature.presentation.model.UserProfileViewState.Loading

class UserProfileViewModel(
    private val performActionUseCase: PerformActionUseCase,
    private val stubDomainMapper: StubDomainMapper,
    private val stubPresentationMapper: StubPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<UserProfileViewState, PresentationNotification>(useCaseExecutor) {
    fun onEnter(stub: StubPresentationModel) {
        updateViewState(Loading)
        performAction(stub)
    }

    private fun performAction(stub: StubPresentationModel) {
        val domainStub = stubDomainMapper.toDomain(stub)
        performActionUseCase(
            value = domainStub,
            onResult = { result ->
                presentStub(result)
            },
            onException = { exception ->
                presentError()
            }
        )
    }

    private fun presentStub(stub: StubDomainModel) {
        val presentationStub = stubPresentationMapper.toPresentation(stub)
        updateViewState(Idle(presentationStub))
    }

    private fun presentError() {
        updateViewState(UserProfileViewState.Error)
    }
}
"""
        val viewModelFile = File(destinationDirectory, "UserProfileViewModel.kt")

        // When
        classUnderTest.generateViewModel(
            destinationDirectory = destinationDirectory,
            viewModelName = viewModelName,
            viewModelPackageName = viewModelPackageName,
            featurePackageName = featurePackageName,
            projectNamespace = projectNamespace
        )

        // Then
        assertEquals("View model file should have exact content", expectedContent, viewModelFile.readText())
    }

    @Test
    fun `Given existing files when generatePresentationLayer then does not overwrite existing files`() {
        // Given
        val featureRoot = File(tempDirectory, "feature")
        val projectNamespace = "com.example"
        val featurePackageName = "com.example.feature"
        val featureName = "test"
        featureRoot.mkdirs()

        val presentationRoot = File(featureRoot, "presentation")
        val modelDirectory = File(presentationRoot, "src/main/java/com/example/feature/model")
        modelDirectory.mkdirs()
        val existingModelFile = File(modelDirectory, "StubPresentationModel.kt")
        val originalContent = "original content"
        existingModelFile.writeText(originalContent)

        // When
        classUnderTest.generatePresentationLayer(featureRoot, projectNamespace, featurePackageName, featureName)

        // Then
        assertEquals("Existing model file should not be overwritten", originalContent, existingModelFile.readText())
    }

    @Test
    fun `Given existing files when generateViewModel then does not overwrite existing files`() {
        // Given
        val destinationDirectory = File(tempDirectory, "presentation")
        destinationDirectory.mkdirs()
        val viewModelName = "UserProfileViewModel"
        val projectNamespace = "com.example"
        val featurePackageName = "$projectNamespace.feature"
        val viewModelPackageName = "$featurePackageName.presentation.viewmodel"
        val existingFile = File(destinationDirectory, "UserProfileViewModel.kt")
        val originalContent = "original content"
        existingFile.writeText(originalContent)

        // When
        classUnderTest.generateViewModel(
            destinationDirectory = destinationDirectory,
            viewModelName = viewModelName,
            viewModelPackageName = viewModelPackageName,
            featurePackageName = projectNamespace,
            projectNamespace = projectNamespace
        )

        // Then
        assertEquals("Existing view model file should not be overwritten", originalContent, existingFile.readText())
    }
}
