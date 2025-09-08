package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.GenerationException
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import java.io.File
import kotlin.io.path.createTempDirectory

@RunWith(Enclosed::class)
@SuiteClasses(
    ArchitectureModulesContentGeneratorTest.Main::class,
    ArchitectureModulesContentGeneratorTest.DomainModule::class,
    ArchitectureModulesContentGeneratorTest.PresentationModule::class,
    ArchitectureModulesContentGeneratorTest.UiModule::class,
    ArchitectureModulesContentGeneratorTest.PresentationTestModule::class,
    ArchitectureModulesContentGeneratorTest.CoroutineModule::class,
    ArchitectureModulesContentGeneratorTest.InstrumentationTestModule::class,
    ArchitectureModulesContentGeneratorTest.GradleFileGeneration::class
)
class ArchitectureModulesContentGeneratorTest {
    @RunWith(Enclosed::class)
    class Main {
        private lateinit var classUnderTest: ArchitectureModulesContentGenerator
        private lateinit var temporaryDirectory: File

        @MockK
        private lateinit var domainModuleCreator: DomainModuleCreator

        @MockK
        private lateinit var instrumentationTestModuleCreator: InstrumentationTestModuleCreator

        @MockK
        private lateinit var presentationModuleCreator: PresentationModuleCreator

        @MockK
        private lateinit var presentationTestModuleCreator: PresentationTestModuleCreator

        @MockK
        private lateinit var uiModuleCreator: UiModuleCreator

        @Before
        fun setUp() {
            MockKAnnotations.init(this)
            temporaryDirectory = createTempDirectory(prefix = "test").toFile()
            classUnderTest =
                ArchitectureModulesContentGenerator(
                    domainModuleCreator = domainModuleCreator,
                    instrumentationTestModuleCreator = instrumentationTestModuleCreator,
                    presentationModuleCreator = presentationModuleCreator,
                    presentationTestModuleCreator = presentationTestModuleCreator,
                    uiModuleCreator = uiModuleCreator
                )
        }

        @Test(expected = GenerationException::class)
        fun `Given empty architecture package name when generate then throws exception`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = ""
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then throws GenerationException
        }

        @Test(expected = GenerationException::class)
        fun `Given invalid architecture package name when generate then throws exception`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "..."
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then throws GenerationException
        }

        @Test(expected = GenerationException::class)
        fun `Given architecture root that is a file when generate then throws exception`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { createNewFile() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then throws GenerationException
        }

        @Test
        fun `Given valid architecture package when generate then calls domainModuleCreator with correct arguments`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then
            verify {
                domainModuleCreator.generateDomainContent(
                    architectureRoot = File(architectureRoot, "domain"),
                    moduleNamespace = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "domain")
                )
            }
        }

        @Test
        fun `Given valid architecture package when generate then calls presentationModuleCreator with correct arguments`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then
            verify {
                presentationModuleCreator.generatePresentationContent(
                    architectureRoot = File(architectureRoot, "presentation"),
                    architecturePackageName = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "presentation")
                )
            }
        }

        @Test
        fun `Given valid architecture package when generate then calls uiModuleCreator with correct arguments`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then
            verify {
                uiModuleCreator.generateUiContent(
                    architectureRoot = File(architectureRoot, "ui"),
                    moduleNamespace = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "ui")
                )
            }
        }

        @Test
        fun `Given valid architecture package when generate then calls presentationTestModuleCreator with correct arguments`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then
            verify {
                presentationTestModuleCreator.generatePresentationTestContent(
                    architectureRoot = File(architectureRoot, "presentation-test"),
                    architecturePackageName = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "presentation")
                )
            }
        }

        @Test
        fun `Given valid architecture package when generate then calls instrumentationTestModuleCreator with correct arguments`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then
            verify {
                instrumentationTestModuleCreator.generateInstrumentationTestContent(
                    architectureRoot = File(architectureRoot, "instrumentation-test"),
                    architecturePackageName = "com.example.architecture",
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "test")
                )
            }
        }

        @Test
        fun `Given valid architecture package when generate then calls all module creators with correct arguments`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

            // Then
            verify {
                domainModuleCreator.generateDomainContent(
                    architectureRoot = File(architectureRoot, "domain"),
                    moduleNamespace = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "domain")
                )
            }
            verify {
                presentationModuleCreator.generatePresentationContent(
                    architectureRoot = File(architectureRoot, "presentation"),
                    architecturePackageName = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "presentation")
                )
            }
            verify {
                uiModuleCreator.generateUiContent(
                    architectureRoot = File(architectureRoot, "ui"),
                    moduleNamespace = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "ui")
                )
            }
            verify {
                presentationTestModuleCreator.generatePresentationTestContent(
                    architectureRoot = File(architectureRoot, "presentation-test"),
                    architecturePackageName = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "presentation")
                )
            }
            verify {
                instrumentationTestModuleCreator.generateInstrumentationTestContent(
                    architectureRoot = File(architectureRoot, "instrumentation-test"),
                    architecturePackageName = "com.example.architecture",
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "test")
                )
            }
        }

        @Test
        fun `Given valid architecture package with detekt enabled when generate then calls all module creators correctly`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = true
            val enableKtlint = true
            val enableDetekt = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            verify {
                domainModuleCreator.generateDomainContent(
                    architectureRoot = File(architectureRoot, "domain"),
                    moduleNamespace = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "domain")
                )
            }
            verify {
                presentationModuleCreator.generatePresentationContent(
                    architectureRoot = File(architectureRoot, "presentation"),
                    architecturePackageName = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "presentation")
                )
            }
            verify {
                uiModuleCreator.generateUiContent(
                    architectureRoot = File(architectureRoot, "ui"),
                    moduleNamespace = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "ui")
                )
            }
            verify {
                presentationTestModuleCreator.generatePresentationTestContent(
                    architectureRoot = File(architectureRoot, "presentation-test"),
                    architecturePackageName = architecturePackageName,
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "presentation")
                )
            }
            verify {
                instrumentationTestModuleCreator.generateInstrumentationTestContent(
                    architectureRoot = File(architectureRoot, "instrumentation-test"),
                    architecturePackageName = "com.example.architecture",
                    architecturePackageNameSegments = listOf("com", "example", "architecture", "test")
                )
            }
        }
    }

    class DomainModule {
        private lateinit var classUnderTest: DomainModuleCreator
        private lateinit var temporaryDirectory: File

        @Before
        fun setUp() {
            classUnderTest = DomainModuleCreator()
            temporaryDirectory = createTempDirectory(prefix = "test").toFile()
        }

        @Test
        fun `Given valid domain module when generateDomainContent then creates UseCase interface with exact content`() {
            // Given
            val domainRoot = File(temporaryDirectory, "domain").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "domain")

            // When
            classUnderTest.generateDomainContent(domainRoot, moduleNamespace, packageSegments)

            // Then
            val useCaseFile = File(domainRoot, "src/main/java/com/example/architecture/domain/usecase/UseCase.kt")
            val expectedContent = """package com.example.architecture.domain.usecase

interface UseCase<REQUEST, RESULT> {
    fun execute(input: REQUEST, onResult: (RESULT) -> Unit)
}
"""
            assertEquals("UseCase.kt should have exact content", expectedContent, useCaseFile.readText())
        }

        @Test
        fun `Given valid domain module when generateDomainContent then creates BackgroundExecutingUseCase class with exact content`() {
            // Given
            val domainRoot = File(temporaryDirectory, "domain").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "domain")

            // When
            classUnderTest.generateDomainContent(domainRoot, moduleNamespace, packageSegments)

            // Then
            val backgroundUseCaseFile =
                File(domainRoot, "src/main/java/com/example/architecture/domain/usecase/BackgroundExecutingUseCase.kt")
            val expectedContent = """package com.example.architecture.domain.usecase

import com.example.coroutine.CoroutineContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BackgroundExecutingUseCase<REQUEST, RESULT>(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : UseCase<REQUEST, RESULT> {
    final override fun execute(input: REQUEST, onResult: (RESULT) -> Unit) {
        coroutineScope.launch {
            val result = withContext(coroutineContextProvider.io) {
                executeInBackground(input)
            }
            onResult(result)
        }
    }

    abstract fun executeInBackground(request: REQUEST): RESULT
}
"""
            assertEquals("BackgroundExecutingUseCase.kt should have exact content", expectedContent, backgroundUseCaseFile.readText())
        }

        @Test
        fun `Given valid domain module when generateDomainContent then creates ContinuousExecutingUseCase class with exact content`() {
            // Given
            val domainRoot = File(temporaryDirectory, "domain").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "domain")

            // When
            classUnderTest.generateDomainContent(domainRoot, moduleNamespace, packageSegments)

            // Then
            val continuousUseCaseFile =
                File(domainRoot, "src/main/java/com/example/architecture/domain/usecase/ContinuousExecutingUseCase.kt")
            val expectedContent = """package com.example.architecture.domain.usecase

import com.example.coroutine.CoroutineContextProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ContinuousExecutingUseCase<REQUEST, RESULT>(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : UseCase<REQUEST, RESULT> {
    final override fun execute(input: REQUEST, onResult: (RESULT) -> Unit) {
        try {
            coroutineScope.launch {
                withContext(coroutineContextProvider.io) {
                    executeInBackground(input).collect { result ->
                        withContext(coroutineContextProvider.main) {
                            onResult(result)
                        }
                    }
                }
            }
        } catch (_: CancellationException) {
        }
    }

    abstract fun executeInBackground(request: REQUEST): Flow<RESULT>
}
"""
            assertEquals("ContinuousExecutingUseCase.kt should have exact content", expectedContent, continuousUseCaseFile.readText())
        }

        @Test
        fun `Given valid domain module when generateDomainContent then creates DomainException class with exact content`() {
            // Given
            val domainRoot = File(temporaryDirectory, "domain").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "domain")

            // When
            classUnderTest.generateDomainContent(domainRoot, moduleNamespace, packageSegments)

            // Then
            val domainExceptionFile = File(domainRoot, "src/main/java/com/example/architecture/domain/exception/DomainException.kt")
            val expectedContent = """package com.example.architecture.domain.exception

abstract class DomainException(cause: Throwable? = null) : Exception(cause)
"""
            assertEquals("DomainException.kt should have exact content", expectedContent, domainExceptionFile.readText())
        }

        @Test
        fun `Given valid domain module when generateDomainContent then creates UnknownDomainException class with exact content`() {
            // Given
            val domainRoot = File(temporaryDirectory, "domain").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "domain")

            // When
            classUnderTest.generateDomainContent(domainRoot, moduleNamespace, packageSegments)

            // Then
            val unknownDomainExceptionFile =
                File(domainRoot, "src/main/java/com/example/architecture/domain/exception/UnknownDomainException.kt")
            val expectedContent = """package com.example.architecture.domain.exception

class UnknownDomainException(cause: Throwable? = null) : DomainException(cause)
"""
            assertEquals("UnknownDomainException.kt should have exact content", expectedContent, unknownDomainExceptionFile.readText())
        }

        @Test
        fun `Given valid domain module when generateDomainContent then creates UseCaseExecutor class with exact content`() {
            // Given
            val domainRoot = File(temporaryDirectory, "domain").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "domain")

            // When
            classUnderTest.generateDomainContent(domainRoot, moduleNamespace, packageSegments)

            // Then
            val useCaseExecutorFile = File(domainRoot, "src/main/java/com/example/architecture/domain/UseCaseExecutor.kt")
            val expectedContent = """package com.example.architecture.domain

import com.example.architecture.domain.exception.DomainException
import com.example.architecture.domain.exception.UnknownDomainException
import com.example.architecture.domain.usecase.UseCase

class UseCaseExecutor {
    fun <OUTPUT> execute(
        useCase: UseCase<Unit, OUTPUT>,
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) = execute(useCase, Unit, onResult, onException)

    fun <INPUT, OUTPUT> execute(
        useCase: UseCase<INPUT, OUTPUT>,
        value: INPUT,
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) {
        try {
            useCase.execute(value, onResult)
        } catch (@Suppress("TooGenericExceptionCaught") throwable: Throwable) {
            val domainException =
                ((throwable as? DomainException) ?: UnknownDomainException(throwable))
            onException(domainException)
        }
    }
}
"""
            assertEquals("UseCaseExecutor.kt should have exact content", expectedContent, useCaseExecutorFile.readText())
        }

        @Test
        fun `Given valid domain module when generateDomainContent then creates UseCaseExecutorProvider typealias with exact content`() {
            // Given
            val domainRoot = File(temporaryDirectory, "domain").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "domain")

            // When
            classUnderTest.generateDomainContent(domainRoot, moduleNamespace, packageSegments)

            // Then
            val useCaseExecutorProviderFile = File(domainRoot, "src/main/java/com/example/architecture/domain/UseCaseExecutorProvider.kt")
            val expectedContent = """package com.example.architecture.domain

import kotlinx.coroutines.CoroutineScope

typealias UseCaseExecutorProvider =
    @JvmSuppressWildcards (coroutineScope: CoroutineScope) -> UseCaseExecutor
"""
            assertEquals("UseCaseExecutorProvider.kt should have exact content", expectedContent, useCaseExecutorProviderFile.readText())
        }
    }

    class PresentationModule {
        private lateinit var classUnderTest: PresentationModuleCreator
        private lateinit var temporaryDirectory: File

        @Before
        fun setUp() {
            classUnderTest = PresentationModuleCreator()
            temporaryDirectory = createTempDirectory(prefix = "test").toFile()
        }

        @Test
        fun `Given valid presentation module when generatePresentationContent then creates BaseViewModel class with exact content`() {
            // Given
            val presentationRoot = File(temporaryDirectory, "presentation").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "presentation")

            // When
            classUnderTest.generatePresentationContent(presentationRoot, architecturePackageName, packageSegments)

            // Then
            val viewModelFile = File(presentationRoot, "src/main/java/com/example/architecture/presentation/viewmodel/BaseViewModel.kt")
            val expectedContent = """package com.example.architecture.presentation.viewmodel

import com.example.architecture.domain.UseCaseExecutor
import com.example.architecture.domain.exception.DomainException
import com.example.architecture.domain.usecase.UseCase
import com.example.architecture.presentation.navigation.PresentationNavigationEvent
import com.example.architecture.presentation.notification.PresentationNotification
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<VIEW_STATE : Any, NOTIFICATION : PresentationNotification>(
    private val useCaseExecutor: UseCaseExecutor
) {
    val viewState: Flow<VIEW_STATE>
        field = MutableSharedFlow()

    val notification: Flow<NOTIFICATION>
        field = MutableSharedFlow()

    val navigationEvent: Flow<PresentationNavigationEvent>
        field = MutableSharedFlow()

    protected fun updateViewState(newState: VIEW_STATE) {
        MainScope().launch {
            viewState.emit(newState)
        }
    }

    protected fun notify(notification: NOTIFICATION) {
        MainScope().launch {
            this@BaseViewModel.notification.emit(notification)
        }
    }

    protected fun emitNavigationEvent(navigationEvent: PresentationNavigationEvent) {
        MainScope().launch {
            this@BaseViewModel.navigationEvent.emit(navigationEvent)
        }
    }

    protected operator fun <OUTPUT> UseCase<Unit, OUTPUT>.invoke(
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) {
        useCaseExecutor.execute(this, onResult, onException)
    }

    protected operator fun <INPUT, OUTPUT> UseCase<INPUT, OUTPUT>.invoke(
        value: INPUT,
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) {
        useCaseExecutor.execute(this, value, onResult, onException)
    }
}
"""
            assertEquals("BaseViewModel.kt should have exact content", expectedContent, viewModelFile.readText())
        }

        @Test
        fun `Given valid presentation module when generatePresentationContent then creates PresentationNavigationEvent`() {
            // Given
            val presentationRoot = File(temporaryDirectory, "presentation").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "presentation")

            // When
            classUnderTest.generatePresentationContent(presentationRoot, architecturePackageName, packageSegments)

            // Then
            val navigationEventFile =
                File(presentationRoot, "src/main/java/com/example/architecture/presentation/navigation/PresentationNavigationEvent.kt")
            val expectedContent = """package com.example.architecture.presentation.navigation

interface PresentationNavigationEvent {
    object Back : PresentationNavigationEvent
}
"""
            assertEquals("PresentationNavigationEvent.kt should have exact content", expectedContent, navigationEventFile.readText())
        }

        @Test
        fun `Given valid presentation module when generatePresentationContent then creates PresentationNotification with exact content`() {
            // Given
            val presentationRoot = File(temporaryDirectory, "presentation").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "presentation")

            // When
            classUnderTest.generatePresentationContent(presentationRoot, architecturePackageName, packageSegments)

            // Then
            val notificationFile =
                File(presentationRoot, "src/main/java/com/example/architecture/presentation/notification/PresentationNotification.kt")
            val expectedContent = """package com.example.architecture.presentation.notification

interface PresentationNotification
"""
            assertEquals("PresentationNotification.kt should have exact content", expectedContent, notificationFile.readText())
        }
    }

    class UiModule {
        private lateinit var classUnderTest: UiModuleCreator
        private lateinit var temporaryDirectory: File

        @Before
        fun setUp() {
            classUnderTest = UiModuleCreator()
            temporaryDirectory = createTempDirectory(prefix = "test").toFile()
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates ViewStateBinder interface with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val viewStateBinderFile = File(uiRoot, "src/main/java/com/example/architecture/ui/binder/ViewStateBinder.kt")
            val expectedContent = """package com.example.architecture.ui.binder

import com.example.architecture.ui.view.ViewsProvider

interface ViewStateBinder<in VIEW_STATE : Any, in VIEWS_PROVIDER : ViewsProvider> {
    fun VIEWS_PROVIDER.bindState(viewState: VIEW_STATE)
}
"""
            assertEquals("ViewStateBinder.kt should have exact content", expectedContent, viewStateBinderFile.readText())
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates UnhandledNavigationException class with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val unhandledNavigationExceptionFile =
                File(uiRoot, "src/main/java/com/example/architecture/ui/navigation/exception/UnhandledNavigationException.kt")
            val expectedContent = """package com.example.architecture.ui.navigation.exception

import com.example.architecture.presentation.navigation.PresentationNavigationEvent

class UnhandledNavigationException(event: PresentationNavigationEvent) :
    IllegalArgumentException(
        "Navigation event ${'$'}{event::class.simpleName} was not handled."
    )
"""
            assertEquals(
                "UnhandledNavigationException.kt should have exact content",
                expectedContent,
                unhandledNavigationExceptionFile.readText()
            )
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates NavigationEventDestinationMapper class with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val navigationEventDestinationMapperFile =
                File(uiRoot, "src/main/java/com/example/architecture/ui/navigation/mapper/NavigationEventDestinationMapper.kt")
            val expectedContent = """package com.example.architecture.ui.navigation.mapper

import com.example.architecture.presentation.navigation.PresentationNavigationEvent
import com.example.architecture.ui.navigation.exception.UnhandledNavigationException
import com.example.architecture.ui.navigation.model.UiDestination
import kotlin.reflect.KClass

abstract class NavigationEventDestinationMapper<in EVENT : PresentationNavigationEvent>(
    private val kotlinClass: KClass<EVENT>
) {
    fun toUi(navigationEvent: PresentationNavigationEvent): UiDestination = when {
        kotlinClass.isInstance(navigationEvent) -> {
            @Suppress("UNCHECKED_CAST")
            mapTypedEvent(navigationEvent as EVENT)
        }

        else -> {
            mapGenericEvent(navigationEvent) ?: throw UnhandledNavigationException(
                navigationEvent
            )
        }
    }

    protected abstract fun mapTypedEvent(navigationEvent: EVENT): UiDestination

    protected open fun mapGenericEvent(
        navigationEvent: PresentationNavigationEvent
    ): UiDestination? = null
}
"""
            assertEquals(
                "NavigationEventDestinationMapper.kt should have exact content",
                expectedContent,
                navigationEventDestinationMapperFile.readText()
            )
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates UiDestination interface with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val uiDestinationFile = File(uiRoot, "src/main/java/com/example/architecture/ui/navigation/model/UiDestination.kt")
            val expectedContent = """package com.example.architecture.ui.navigation.model

import androidx.navigation.NavController

fun interface UiDestination {
    fun navigate(navController: NavController)
}
"""
            assertEquals("UiDestination.kt should have exact content", expectedContent, uiDestinationFile.readText())
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates NotificationUiMapper interface with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val notificationUiMapperFile =
                File(uiRoot, "src/main/java/com/example/architecture/ui/notification/mapper/NotificationUiMapper.kt")
            val expectedContent = """package com.example.architecture.ui.notification.mapper

import com.example.architecture.presentation.notification.PresentationNotification
import com.example.architecture.ui.notification.model.UiNotification

interface NotificationUiMapper<in PRESENTATION_NOTIFICATION : PresentationNotification> {
    fun toUi(notification: PRESENTATION_NOTIFICATION): UiNotification
}
"""
            assertEquals("NotificationUiMapper.kt should have exact content", expectedContent, notificationUiMapperFile.readText())
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates UiNotification interface with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val uiNotificationFile = File(uiRoot, "src/main/java/com/example/architecture/ui/notification/model/UiNotification.kt")
            val expectedContent = """package com.example.architecture.ui.notification.model

fun interface UiNotification {
    fun present()
}
"""
            assertEquals("UiNotification.kt should have exact content", expectedContent, uiNotificationFile.readText())
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates BaseComposeHolder class with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val baseComposeHolderFile = File(uiRoot, "src/main/java/com/example/architecture/ui/view/BaseComposeHolder.kt")
            val expectedContent = """package com.example.architecture.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.example.architecture.presentation.navigation.PresentationNavigationEvent
import com.example.architecture.presentation.notification.PresentationNotification
import com.example.architecture.presentation.viewmodel.BaseViewModel
import com.example.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.example.architecture.ui.notification.mapper.NotificationUiMapper

abstract class BaseComposeHolder<VIEW_STATE : Any, NOTIFICATION : PresentationNotification>(
    private val viewModel: BaseViewModel<VIEW_STATE, NOTIFICATION>,
    private val navigationMapper: NavigationEventDestinationMapper<*>,
    private val notificationMapper: NotificationUiMapper<NOTIFICATION>
) {
    @Composable
    fun ViewModelObserver(navController: NavController) {
        viewModel.notification.collectAsState(initial = null)
            .value?.let { notificationValue ->
                Notifier(notification = notificationValue)
            }

        viewModel.navigationEvent.collectAsState(initial = null)
            .value?.let { navigationValue ->
                Navigator(navigationValue, navController)
            }
    }

    @Composable
    private fun Notifier(notification: NOTIFICATION) {
        LaunchedEffect(notification) {
            notificationMapper.toUi(notification).present()
        }
    }

    @Composable
    private fun Navigator(navigation: PresentationNavigationEvent, navController: NavController) {
        LaunchedEffect(navigation) {
            navigationMapper.toUi(navigation).navigate(navController)
        }
    }
}
"""
            assertEquals("BaseComposeHolder.kt should have exact content", expectedContent, baseComposeHolderFile.readText())
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates BaseFragment class with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val baseFragmentFile = File(uiRoot, "src/main/java/com/example/architecture/ui/view/BaseFragment.kt")
            val expectedContent = """package com.example.architecture.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.architecture.presentation.navigation.PresentationNavigationEvent
import com.example.architecture.presentation.notification.PresentationNotification
import com.example.architecture.presentation.viewmodel.BaseViewModel
import com.example.architecture.ui.binder.ViewStateBinder
import com.example.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private typealias NavigationMapper = NavigationEventDestinationMapper<PresentationNavigationEvent>

abstract class BaseFragment<VIEW_STATE : Any, NOTIFICATION : PresentationNotification> :
    Fragment,
    ViewsProvider {
    constructor() : super()
    constructor(@LayoutRes layoutResourceId: Int) : super(layoutResourceId)

    abstract val viewModel: BaseViewModel<VIEW_STATE, NOTIFICATION>

    abstract val viewStateBinder: ViewStateBinder<VIEW_STATE, ViewsProvider>

    abstract val navigationEventDestinationMapper: NavigationMapper

    open val navController: NavController
        get() = findNavController()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.bindViews()
        observeViewModel()
        return view
    }

    abstract fun View.bindViews()

    private fun observeViewModel() {
        with(viewModel) {
            performOnStartedLifecycleEvent {
                viewState.collect(::applyViewState)
            }
            performOnStartedLifecycleEvent {
                navigationEvent.collect(::navigate)
            }
        }
    }

    private fun performOnStartedLifecycleEvent(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    private fun applyViewState(viewState: VIEW_STATE) {
        with(viewStateBinder) {
            bindState(viewState)
        }
    }

    private fun navigate(destination: PresentationNavigationEvent) {
        val uiDestination = navigationEventDestinationMapper.toUi(destination)
        uiDestination.navigate(navController)
    }
}
"""
            assertEquals("BaseFragment.kt should have exact content", expectedContent, baseFragmentFile.readText())
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates ScreenEnterObserver function with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val screenEnterObserverFile = File(uiRoot, "src/main/java/com/example/architecture/ui/view/ScreenEnterObserver.kt")
            val expectedContent = """package com.example.architecture.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun ScreenEnterObserver(onEntered: () -> Unit) {
    var entered by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(entered) {
        if (!entered) {
            entered = true
            onEntered()
        }
    }
}
"""
            assertEquals("ScreenEnterObserver.kt should have exact content", expectedContent, screenEnterObserverFile.readText())
        }

        @Test
        fun `Given valid UI module when generateUiContent then creates ViewsProvider interface with exact content`() {
            // Given
            val uiRoot = File(temporaryDirectory, "ui").apply { mkdirs() }
            val moduleNamespace = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "ui")

            // When
            classUnderTest.generateUiContent(uiRoot, moduleNamespace, packageSegments)

            // Then
            val viewsProviderFile = File(uiRoot, "src/main/java/com/example/architecture/ui/view/ViewsProvider.kt")
            val expectedContent = """package com.example.architecture.ui.view

interface ViewsProvider
"""
            assertEquals("ViewsProvider.kt should have exact content", expectedContent, viewsProviderFile.readText())
        }
    }

    class PresentationTestModule {
        private lateinit var classUnderTest: PresentationTestModuleCreator
        private lateinit var temporaryDirectory: File

        @Before
        fun setUp() {
            classUnderTest = PresentationTestModuleCreator()
            temporaryDirectory = createTempDirectory(prefix = "test").toFile()
        }

        @Test
        fun `Given valid presentation test module when generatePresentationTestContent then creates BaseViewModelTest`() {
            // Given
            val presentationTestRoot = File(temporaryDirectory, "presentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "presentation")

            // When
            classUnderTest.generatePresentationTestContent(presentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val baseViewModelTestFile =
                File(
                    presentationTestRoot,
                    "src/main/java/com/example/architecture/presentation/viewmodel/BaseViewModelTest.kt"
                )
            val expectedContent = """package com.example.architecture.presentation.viewmodel

import com.example.architecture.domain.UseCaseExecutor
import com.example.architecture.domain.exception.DomainException
import com.example.architecture.domain.usecase.UseCase
import com.example.architecture.presentation.notification.PresentationNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.mockito.BDDMockito.willAnswer
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.stubbing.Answer

private const val NO_INPUT_ON_RESULT_ARGUMENT_INDEX = 1
private const val NO_INPUT_ON_EXCEPTION_ARGUMENT_INDEX = 2
private const val ON_RESULT_ARGUMENT_INDEX = 2
private const val ON_EXCEPTION_ARGUMENT_INDEX = 3

abstract class BaseViewModelTest<
    VIEW_STATE : Any,
    NOTIFICATION : PresentationNotification,
    VIEW_MODEL : BaseViewModel<VIEW_STATE, NOTIFICATION>
    > {
    private val testScheduler = TestCoroutineScheduler()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)

    protected lateinit var classUnderTest: VIEW_MODEL

    @Mock
    protected lateinit var useCaseExecutor: UseCaseExecutor

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun coroutineSetUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun coroutineTearDown() {
        Dispatchers.resetMain()
    }

    protected fun UseCase<Unit, *>.givenFailedExecution(domainException: DomainException) {
        givenExecutionWillAnswer { invocation ->
            val onException: (DomainException) -> Unit =
                invocation.getArgument(NO_INPUT_ON_EXCEPTION_ARGUMENT_INDEX)
            onException(domainException)
        }
    }

    protected fun <REQUEST> UseCase<REQUEST, *>.givenFailedExecution(
        input: REQUEST,
        domainException: DomainException
    ) {
        givenExecutionWillAnswer(input) { invocation ->
            val onException: (DomainException) -> Unit =
                invocation.getArgument(ON_EXCEPTION_ARGUMENT_INDEX)
            onException(domainException)
        }
    }

    protected fun <REQUEST, RESULT> UseCase<REQUEST, RESULT>.givenSuccessfulExecution(
        input: REQUEST,
        result: RESULT
    ) {
        givenExecutionWillAnswer(input) { invocation ->
            val onResult: (RESULT) -> Unit = invocation.getArgument(ON_RESULT_ARGUMENT_INDEX)
            onResult(result)
        }
    }

    protected fun <REQUEST> UseCase<REQUEST, Unit>.givenSuccessfulNoResultExecution(
        input: REQUEST
    ) {
        givenSuccessfulExecution(input, Unit)
    }

    protected fun <RESULT> UseCase<Unit, RESULT>.givenSuccessfulExecution(result: RESULT) {
        givenExecutionWillAnswer { invocationOnMock ->
            val onResult: (RESULT) -> Unit =
                invocationOnMock.getArgument(NO_INPUT_ON_RESULT_ARGUMENT_INDEX)
            onResult(result)
        }
    }

    protected fun UseCase<Unit, Unit>.givenSuccessfulNoArgumentNoResultExecution() {
        givenExecutionWillAnswer { invocationOnMock ->
            val onResult: (Unit) -> Unit = invocationOnMock.getArgument(ON_RESULT_ARGUMENT_INDEX)
            onResult(Unit)
        }
    }

    private fun <RESULT> UseCase<Unit, RESULT>.givenExecutionWillAnswer(answer: Answer<*>) {
        willAnswer(answer).given(useCaseExecutor).execute(
            useCase = eq(this@givenExecutionWillAnswer),
            onResult = any(),
            onException = any()
        )
    }

    private fun <REQUEST, RESULT> UseCase<REQUEST, RESULT>.givenExecutionWillAnswer(
        input: REQUEST,
        answer: Answer<*>
    ) {
        willAnswer(answer).given(useCaseExecutor).execute(
            useCase = eq(this@givenExecutionWillAnswer),
            value = eq(input),
            onResult = any(),
            onException = any()
        )
    }
}
"""
            assertEquals("BaseViewModelTest.kt should have exact content", expectedContent, baseViewModelTestFile.readText())
        }
    }

    class CoroutineModule {
        private lateinit var classUnderTest: CoroutineModuleCreator
        private lateinit var temporaryDirectory: File

        @Before
        fun setUp() {
            classUnderTest = CoroutineModuleCreator()
            temporaryDirectory = createTempDirectory(prefix = "test").toFile()
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid coroutine module when generateCoroutineContent then creates CoroutineContextProvider interface with exact content`() {
            // Given
            val coroutineRoot = File(temporaryDirectory, "coroutine").apply { mkdirs() }
            val moduleNamespace = "com.example.coroutine"
            val packageSegments = listOf("com", "example", "coroutine")

            // When
            classUnderTest.generateCoroutineContent(coroutineRoot, moduleNamespace, packageSegments)

            // Then
            val coroutineContextProviderFile = File(coroutineRoot, "src/main/java/com/example/coroutine/CoroutineContextProvider.kt")
            val expectedContent = """package com.example.coroutine

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

interface CoroutineContextProvider {
    val main: CoroutineContext
    val io: CoroutineContext

    object Default : CoroutineContextProvider {
        override val main: CoroutineContext = Dispatchers.Main
        override val io: CoroutineContext = Dispatchers.IO
    }
}
"""
            assertEquals("CoroutineContextProvider.kt should have exact content", expectedContent, coroutineContextProviderFile.readText())
        }
    }

    class InstrumentationTestModule {
        private lateinit var classUnderTest: InstrumentationTestModuleCreator
        private lateinit var temporaryDirectory: File

        @Before
        fun setUp() {
            classUnderTest = InstrumentationTestModuleCreator()
            temporaryDirectory = createTempDirectory(prefix = "test").toFile()
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates BaseTest class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val baseTestFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/test/BaseTest.kt")
            val expectedContent = """package com.example.architecture.test.test

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.annotation.CallSuper
import androidx.compose.ui.test.IdlingResource as ComposeIdlingResource
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource as EspressoIdlingResource
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.example.architecture.test.idlingresource.findAndCloseAppNotRespondingDialog
import com.example.architecture.test.idlingresource.registerAppNotRespondingWatcher
import com.example.architecture.test.launcher.AppLauncher
import com.example.architecture.test.localstore.KeyValueStore
import com.example.architecture.test.rule.HiltInjectorRule
import com.example.architecture.test.rule.LocalStoreRule
import com.example.architecture.test.rule.ScreenshotFailureRule
import com.example.architecture.test.rule.SdkAwareGrantPermissionRule
import com.example.architecture.test.rule.WebServerRule
import com.example.architecture.test.server.MockDispatcher
import com.example.architecture.test.server.MockWebServerProvider
import com.example.architecture.test.server.ResponseStore
import dagger.hilt.android.testing.HiltAndroidRule
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.RuleChain

typealias TypedAndroidComposeTestRule<ACTIVITY> =
    AndroidComposeTestRule<ActivityScenarioRule<ACTIVITY>, ACTIVITY>

abstract class BaseTest {
    private val hiltAndroidRule by lazy { HiltAndroidRule(this) }

    @Inject
    lateinit var mockDispatcher: MockDispatcher

    @Inject
    lateinit var responseStore: ResponseStore

    @Inject
    lateinit var mockWebServerProvider: MockWebServerProvider

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var keyValueStore: KeyValueStore

    @Inject
    lateinit var espressoIdlingResources: @JvmSuppressWildcards Collection<EspressoIdlingResource>

    @Inject
    lateinit var composeIdlingResources: @JvmSuppressWildcards Collection<ComposeIdlingResource>

    private val webServerRule = WebServerRule(
        lazy { mockDispatcher },
        lazy { responseStore }
    )

    private val localStoreRule = LocalStoreRule(
        lazy { sharedPreferences },
        lazy { keyValueStore }
    )

    private val idlingRegistry by lazy { IdlingRegistry.getInstance() }

    protected abstract val composeTestRule: ComposeContentTestRule

    @SuppressLint("UnsafeOptInUsageError")
    private val grantPermissionRule = SdkAwareGrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE)

    @get:Rule
    val testRules: RuleChain by lazy {
        RuleChain
            .outerRule(hiltAndroidRule)
            .around(HiltInjectorRule(hiltAndroidRule))
            .around(ScreenshotFailureRule())
            .around(webServerRule)
            .around(localStoreRule)
            .around(composeTestRule)
            .around(grantPermissionRule)
    }

    protected abstract val startActivityLauncher: AppLauncher

    @Before
    fun setUp() {
        val deviceUi = UiDevice.getInstance(getInstrumentation())
        deviceUi.findAndCloseAppNotRespondingDialog()
        registerIdlingResources()
        startActivityLauncher.launch()
    }

    @After
    fun cleanUp() {
        unregisterIdlingResources()
    }

    private fun registerIdlingResources() {
        idlingRegistry.register(*(espressoIdlingResources).toTypedArray())
        composeIdlingResources.forEach(composeTestRule::registerIdlingResource)
    }

    private fun unregisterIdlingResources() {
        idlingRegistry.unregister(*(espressoIdlingResources).toTypedArray())
        composeIdlingResources.forEach(composeTestRule::unregisterIdlingResource)
    }

    companion object {
        @BeforeClass
        @CallSuper
        @JvmStatic
        fun setUpGlobally() {
            val deviceUi = UiDevice.getInstance(getInstrumentation())
            deviceUi.registerAppNotRespondingWatcher()
        }
    }
}
"""
            assertEquals("BaseTest.kt should have exact content", expectedContent, baseTestFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates ClickChildView class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val clickChildViewFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/action/ClickChildView.kt")
            val expectedContent = """package com.example.architecture.test.action

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

fun clickChildView(matcher: Matcher<View>) = ClickChildView(matcher)

class ClickChildView(private val matcher: Matcher<View>) : ViewAction {
    override fun getConstraints(): Matcher<View> = allOf(isDisplayed(), matcher)

    override fun getDescription() = "Click on a matching view"

    override fun perform(uiController: UiController, view: View) {
        clickOnMatchingView(view)
    }

    private fun clickOnMatchingView(view: View): Boolean {
        if (matcher.matches(view)) {
            view.performClick()
            return true
        }

        return if (view is ViewGroup) {
            view.children.iterator().asSequence().firstOrNull { childView ->
                clickOnMatchingView(childView)
            } != null
        } else {
            false
        }
    }
}
"""
            assertEquals("ClickChildView.kt should have exact content", expectedContent, clickChildViewFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates ServerRequestResponse annotation with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val serverRequestResponseFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/annotation/ServerRequestResponse.kt")
            val expectedContent = """package com.example.architecture.test.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class ServerRequestResponse(val requestResponseIds: Array<String>)
"""
            assertEquals("ServerRequestResponse.kt should have exact content", expectedContent, serverRequestResponseFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates LocalStore annotation with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val localStoreFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/annotation/LocalStore.kt")
            val expectedContent = """package com.example.architecture.test.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class LocalStore(val localStoreDataIds: Array<String>)
"""
            assertEquals("LocalStore.kt should have exact content", expectedContent, localStoreFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates ItemAtPositionMatcher function with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val itemAtPositionMatcherFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/assertion/ItemAtPositionMatcher.kt")
            val expectedContent = """package com.example.architecture.test.assertion

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher

fun matchesItemAtPosition(matcher: Matcher<View?>?, position: Int) =
    ViewAssertion { view, noViewFoundException ->
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val recyclerView = view as RecyclerView
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            ?: throw AssertionFailedError("No view holder at position: ${'$'}position")
        assertThat(viewHolder.itemView, matcher)
    }
"""
            assertEquals("ItemAtPositionMatcher.kt should have exact content", expectedContent, itemAtPositionMatcherFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates AssetReader functions with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val assetReaderFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/asset/AssetReader.kt")
            val expectedContent = """package com.example.architecture.test.asset

import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream

fun getAssetAsString(name: String): String =
    processAssetStream(name) { stream -> stream.bufferedReader().readText() }

fun <OUTPUT> processAssetStream(
    filename: String,
    performOnStream: (inputStream: InputStream) -> OUTPUT
): OUTPUT = InstrumentationRegistry.getInstrumentation().context.assets.open(filename)
    .use { stream -> performOnStream(stream) }
"""
            assertEquals("AssetReader.kt should have exact content", expectedContent, assetReaderFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates AppNotRespondingHandler functions with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val appNotRespondingHandlerFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/idlingresource/AppNotRespondingHandler.kt")
            val expectedContent = """package com.example.architecture.test.idlingresource

import android.util.Log
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector

private const val APP_NOT_RESPONDING_TEXT = " isn't responding"
private const val APP_NOT_RESPONDING_TAG = "AppNotResponding"
fun UiDevice.registerAppNotRespondingWatcher() {
    registerWatcher("AppNotResponding") {
        findAndCloseAppNotRespondingDialog()
    }
    findAndCloseAppNotRespondingDialog()
}

private fun UiDevice.appNotRespondingDialog() = findObject(
    UiSelector()
        .packageName("android")
        .textContains(APP_NOT_RESPONDING_TEXT)
)

fun UiDevice.findAndCloseAppNotRespondingDialog() =
    appNotRespondingDialog().let { appNotRespondingDialog ->
        appNotRespondingDialog.exists()
            .also { dialogExists ->
                if (dialogExists) {
                    closeAnrWithWait(appNotRespondingDialog)
                }
            }
    }

private fun UiDevice.closeAnrWithWait(appNotRespondingDialog: UiObject) {
    Log.i(APP_NOT_RESPONDING_TAG, "App not responding (ANR) dialog detected.")
    try {
        findObject(
            UiSelector()
                .text("Wait")
                .className("android.widget.Button")
                .packageName("android")
        ).click()
        val dialogText = appNotRespondingDialog.text
        val appName = dialogText.take(dialogText.length - APP_NOT_RESPONDING_TEXT.length)
        Log.i(APP_NOT_RESPONDING_TAG, "App \"${'$'}appName\" is not responding. Pressed on wait.")
    } catch (uiObjectNotFoundException: UiObjectNotFoundException) {
        Log.i(APP_NOT_RESPONDING_TAG, "Detected app not responding dialog, but window disappeared.")
    }
}
"""
            assertEquals("AppNotRespondingHandler.kt should have exact content", expectedContent, appNotRespondingHandlerFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates DoesNot function with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val doesNotFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/test/DoesNot.kt")
            val expectedContent = """package com.example.architecture.test.test

import junit.framework.AssertionFailedError

fun doesNot(description: String, block: () -> Unit) {
    try {
        block()
        error("Unexpected: ${'$'}description")
    } catch (_: AssertionFailedError) {
    }
}
"""
            assertEquals("DoesNot.kt should have exact content", expectedContent, doesNotFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates Retry function with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val retryFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/test/Retry.kt")
            val expectedContent = """package com.example.architecture.test.test

import junit.framework.AssertionFailedError

fun retry(waitMilliseconds: Long = 200L, repeat: Int = 5, block: () -> Unit) {
    var lastExceptionMessage = ""
    repeat(repeat) {
        try {
            block()
            return
        } catch (exception: AssertionFailedError) {
            lastExceptionMessage = exception.message.orEmpty()
            Thread.sleep(waitMilliseconds)
        }
    }
    throw AssertionFailedError(lastExceptionMessage)
}
"""
            assertEquals("Retry.kt should have exact content", expectedContent, retryFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates ComposeOkHttp3IdlingResource class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val composeOkHttp3IdlingResourceFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/idlingresource/ComposeOkHttp3IdlingResource.kt")
            val expectedContent = """package com.example.architecture.test.idlingresource

import androidx.compose.ui.test.IdlingResource
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

class ComposeOkHttp3IdlingResource private constructor(dispatcher: Dispatcher) : IdlingResource {
    override val isIdleNow: Boolean = dispatcher.runningCallsCount() == 0

    companion object {
        fun create(client: OkHttpClient): ComposeOkHttp3IdlingResource =
            ComposeOkHttp3IdlingResource(client.dispatcher)
    }
}
"""
            assertEquals(
                "ComposeOkHttp3IdlingResource.kt should have exact content",
                expectedContent,
                composeOkHttp3IdlingResourceFile.readText()
            )
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates EspressoOkHttp3IdlingResource class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val espressoOkHttp3IdlingResourceFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/idlingresource/EspressoOkHttp3IdlingResource.kt")
            val expectedContent = """package com.example.architecture.test.idlingresource

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

class EspressoOkHttp3IdlingResource private constructor(
    private val name: String,
    private val dispatcher: Dispatcher
) : IdlingResource {
    @Volatile
    var callback: ResourceCallback? = null

    init {
        dispatcher.idleCallback = Runnable {
            val callback = callback
            callback?.onTransitionToIdle()
        }
    }

    override fun getName(): String = name

    override fun isIdleNow(): Boolean = dispatcher.runningCallsCount() == 0

    override fun registerIdleTransitionCallback(callback: ResourceCallback) {
        this.callback = callback
    }

    companion object {
        fun create(name: String, client: OkHttpClient): EspressoOkHttp3IdlingResource =
            EspressoOkHttp3IdlingResource(name, client.dispatcher)
    }
}
"""
            assertEquals(
                "EspressoOkHttp3IdlingResource.kt should have exact content",
                expectedContent,
                espressoOkHttp3IdlingResourceFile.readText()
            )
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates AppLauncher interface with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val appLauncherFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/launcher/AppLauncher.kt")
            val expectedContent = """package com.example.architecture.test.launcher

fun interface AppLauncher {
    fun launch()
}
"""
            assertEquals("AppLauncher.kt should have exact content", expectedContent, appLauncherFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates FromComposable function with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val fromComposableFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/launcher/FromComposable.kt")
            val expectedContent = """package com.example.architecture.test.launcher

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import com.example.architecture.test.test.TypedAndroidComposeTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun <ACTIVITY : ComponentActivity> fromComposable(
    composeContentTestRule: TypedAndroidComposeTestRule<ACTIVITY>,
    composable: @Composable (ACTIVITY) -> Unit
) = AppLauncher {
    val activity = composeContentTestRule.activity
    activity.findViewById<ViewGroup>(android.R.id.content)?.let { root ->
        runBlocking(Dispatchers.Main) {
            root.removeAllViews()
        }
    }
    composeContentTestRule.setContent { composable(activity) }
}
"""
            assertEquals("FromComposable.kt should have exact content", expectedContent, fromComposableFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates KeyValueStore class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val keyValueStoreFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/localstore/KeyValueStore.kt")
            val expectedContent = """package com.example.architecture.test.localstore

private typealias KeyValuePairList = List<Pair<String, Pair<String, Any>>>
private typealias KeyValueMap = Map<String, Pair<String, Any>>

abstract class KeyValueStore {
    val keyValues by lazy {
        internalKeyValues.toValidatedMap()
    }

    protected abstract val internalKeyValues: List<Pair<String, Pair<String, Any>>>

    private fun KeyValuePairList.toValidatedMap(): KeyValueMap {
        val responses = toMap()
        check(responses.size == size) {
            "Duplicate key/value key declared. Make sure all key/value keys are unique."
        }
        return responses
    }
}
"""
            assertEquals("KeyValueStore.kt should have exact content", expectedContent, keyValueStoreFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates WithBackgroundColorMatcher class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val withBackgroundColorMatcherFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/matcher/WithBackgroundColorMatcher.kt")
            val expectedContent = """package com.example.architecture.test.matcher

import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun withBackgroundColorMatcher(
    @ColorInt color: Int,
    matchCardViewBackgrounds: Boolean
): Matcher<View> = WithBackgroundColorMatcher(color, matchCardViewBackgrounds)

class WithBackgroundColorMatcher(
    @ColorInt private val expectedColor: Int,
    private val matchCardViewBackgrounds: Boolean = false
) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description?) {
        @OptIn(ExperimentalStdlibApi::class)
        description?.appendText("has background color: #${'$'}{expectedColor.toHexString()}")
    }

    override fun matchesSafely(item: View): Boolean {
        val textViewColor = if (matchCardViewBackgrounds) {
            (item as? CardView)?.cardBackgroundColor?.getColorForState(item.drawableState, -1)
                .also {
                    @OptIn(ExperimentalStdlibApi::class)
                    println("Background color: #${'$'}{it?.toHexString()}")
                }
        } else {
            (item.background as? ColorDrawable)?.color
        }

        return textViewColor == expectedColor
    }
}
"""
            assertEquals(
                "WithBackgroundColorMatcher.kt should have exact content",
                expectedContent,
                withBackgroundColorMatcherFile.readText()
            )
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates WithDrawableIdMatcher class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val withDrawableIdMatcherFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/matcher/WithDrawableIdMatcher.kt")
            val expectedContent = """package com.example.architecture.test.matcher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun withDrawableId(@DrawableRes id: Int): Matcher<View> = WithDrawableIdMatcher(id)

class WithDrawableIdMatcher(@param:DrawableRes private val expectedId: Int) :
    TypeSafeMatcher<View>(View::class.java) {

    override fun matchesSafely(target: View): Boolean {
        @Suppress("UNCHECKED_CAST")
        val drawable: Drawable = when {
            target::class.simpleName == "androidx.appcompat.view.menu.ActionMenuItemView" -> {
                (target::class as KClass<in View>).memberProperties
                    .first { it.name == "bar" }
                    .getter(target) as Drawable
            }

            target is ImageView -> {
                target.drawable?.extractStateIfStateful(target.drawableState)
            }

            else -> null
        } ?: return false

        val resources = target.resources
        val expectedDrawable =
            ResourcesCompat.getDrawable(resources, expectedId, target.context.theme)
        val constantStateIsSame =
            expectedDrawable?.constantState?.let { it == drawable.constantState } == true
        if (constantStateIsSame) return true

        val bitmapHolder = getBitmap(drawable)
        val expectedBitmapHolder = expectedDrawable?.let(::getBitmap)
        val result = expectedBitmapHolder?.bitmap?.sameAs(bitmapHolder?.bitmap) == true
        bitmapHolder?.recycleIfRecyclable()
        expectedBitmapHolder?.recycleIfRecyclable()
        return result
    }

    private fun getBitmap(drawable: Drawable): BitmapHolder? =
        (drawable as? BitmapDrawable)?.let { bitmapDrawable ->
            BitmapHolder(bitmap = bitmapDrawable.bitmap, recyclable = false)
        } ?: run {
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            if (width < 1 || height < 1) {
                return null
            }
            val result = createBitmap(width, height)
            val canvas = Canvas(result)

            with(drawable) {
                setBounds(0, 0, canvas.width, canvas.height)
                colorFilter = PorterDuffColorFilter(0, PorterDuff.Mode.DST)
                draw(canvas)
            }
            BitmapHolder(bitmap = result, recyclable = true)
        }

    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: ${'$'}expectedId")
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        targetContext.resources.getResourceEntryName(expectedId)
            ?.let { description.appendText("[${'$'}it]") }
    }

    private fun Drawable.extractStateIfStateful(currentState: IntArray): Drawable? {
        val stateListDrawable = this as? StateListDrawable ?: return this

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stateListDrawable.getStateDrawable(
                stateListDrawable.findStateDrawableIndex(currentState)
            )
        } else {
            Log.w("DrawableMatcher", "Android version ${'$'}{Build.VERSION.SDK_INT} unsupported.")
            null
        }
    }

    private class BitmapHolder(val bitmap: Bitmap, private val recyclable: Boolean) {
        fun recycleIfRecyclable() {
            if (recyclable) {
                bitmap.recycle()
            }
        }
    }
}
"""
            assertEquals("WithDrawableIdMatcher.kt should have exact content", expectedContent, withDrawableIdMatcherFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates HiltInjectorRule class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val hiltInjectorRuleFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/rule/HiltInjectorRule.kt")
            val expectedContent = """package com.example.architecture.test.rule

import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class HiltInjectorRule(private val hiltAndroidRule: HiltAndroidRule) : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                hiltAndroidRule.inject()
                base.evaluate()
            }
        }
}
"""
            assertEquals("HiltInjectorRule.kt should have exact content", expectedContent, hiltInjectorRuleFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates MockRequest class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val mockRequestFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/MockRequest.kt")
            val expectedContent = """package com.example.architecture.test.server

data class MockRequest(val url: String)
"""
            assertEquals("MockRequest.kt should have exact content", expectedContent, mockRequestFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates MockResponse class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val mockResponseFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/MockResponse.kt")
            val expectedContent = """package com.example.architecture.test.server

data class MockResponse(
    val upgradeToWebSocket: Boolean = false,
    val code: Int = 200,
    val headers: List<Pair<String, String>> = emptyList(),
    val body: String = ""
)
"""
            assertEquals("MockResponse.kt should have exact content", expectedContent, mockResponseFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates MockRequestResponseFactory class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val mockRequestResponseFactoryFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/MockRequestResponseFactory.kt")
            val expectedContent = """package com.example.architecture.test.server

import com.example.architecture.test.server.response.MockResponseFactory

data class MockRequestResponseFactory(
    val request: MockRequest,
    val responseFactory: MockResponseFactory
)
"""
            assertEquals(
                "MockRequestResponseFactory.kt should have exact content",
                expectedContent,
                mockRequestResponseFactoryFile.readText()
            )
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates MockResponseFactory interface with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val mockResponseFactoryFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/response/MockResponseFactory.kt")
            val expectedContent = """package com.example.architecture.test.server.response

import com.example.architecture.test.server.MockResponse

interface MockResponseFactory {
    fun mockResponse(): MockResponse
}
"""
            assertEquals("MockResponseFactory.kt should have exact content", expectedContent, mockResponseFactoryFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates ErrorResponseFactory class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val errorResponseFactoryFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/response/ErrorResponseFactory.kt")
            val expectedContent = """package com.example.architecture.test.server.response

import com.example.architecture.test.server.MockResponse

sealed class ErrorResponseFactory {
    object NotFound : MockResponseFactory {
        override fun mockResponse() = MockResponse(code = 404)
    }
}
"""
            assertEquals("ErrorResponseFactory.kt should have exact content", expectedContent, errorResponseFactoryFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates SimpleResponseFactory class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val simpleResponseFactoryFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/response/SimpleResponseFactory.kt")
            val expectedContent = """package com.example.architecture.test.server.response

import com.example.architecture.test.asset.getAssetAsString
import com.example.architecture.test.server.MockResponse

data class SimpleResponseFactory(
    private val code: Int = 200,
    private val headers: List<Pair<String, String>> = emptyList(),
    private val bodyFileName: String? = null
) : MockResponseFactory {
    private val body by lazy {
        if (bodyFileName == null) {
            ""
        } else {
            getAssetAsString(bodyFileName)
        }
    }

    override fun mockResponse() = MockResponse(code = code, headers = headers, body = body)
}
"""
            assertEquals("SimpleResponseFactory.kt should have exact content", expectedContent, simpleResponseFactoryFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates SequenceResponseFactory class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val sequenceResponseFactoryFile =
                File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/response/SequenceResponseFactory.kt")
            val expectedContent = """package com.example.architecture.test.server.response

import com.example.architecture.test.server.MockResponse

class SequenceResponseFactory(private vararg val responses: MockResponseFactory) :
    MockResponseFactory {
    private var responseIndex = 0
    override fun mockResponse(): MockResponse {
        val mockResponse = responses[responseIndex]
        responseIndex++
        if (responseIndex == responses.size) {
            responseIndex = 0
        }
        return mockResponse.mockResponse()
    }
}
"""
            assertEquals("SequenceResponseFactory.kt should have exact content", expectedContent, sequenceResponseFactoryFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates ResponseBinder interface with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val responseBinderFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/ResponseBinder.kt")
            val expectedContent = """package com.example.architecture.test.server

interface ResponseBinder {
    var testName: String

    fun bindResponse(requestResponseFactory: MockRequestResponseFactory)

    val usedEndpoints: Set<MockRequest>

    fun reset()

    var onWebSocketMessage: (String) -> Unit
}
"""
            assertEquals("ResponseBinder.kt should have exact content", expectedContent, responseBinderFile.readText())
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given valid instrumentation test module when generateInstrumentationTestContent then creates ResponseStore class with exact content`() {
            // Given
            val instrumentationTestRoot = File(temporaryDirectory, "instrumentation-test").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val packageSegments = listOf("com", "example", "architecture", "test")

            // When
            classUnderTest.generateInstrumentationTestContent(instrumentationTestRoot, architecturePackageName, packageSegments)

            // Then
            val responseStoreFile = File(instrumentationTestRoot, "src/main/java/com/example/architecture/test/server/ResponseStore.kt")
            val expectedContent = """package com.example.architecture.test.server

private typealias MockRequestResponsePairList = List<Pair<String, MockRequestResponseFactory>>
private typealias MockRequestResponseMap = Map<String, MockRequestResponseFactory>

abstract class ResponseStore {
    val responseFactories by lazy {
        internalResponseFactories.toValidatedMap()
    }

    protected abstract val internalResponseFactories: MockRequestResponsePairList

    private fun MockRequestResponsePairList.toValidatedMap(): MockRequestResponseMap {
        val responses = toMap()
        check(responses.size == size) {
            "Duplicate Request/Response key declared. " +
                "Make sure all Request/Response keys are unique."
        }
        return responses
    }
}
"""
            assertEquals("ResponseStore.kt should have exact content", expectedContent, responseStoreFile.readText())
        }
    }

    class GradleFileGeneration {
        private lateinit var classUnderTest: ArchitectureModulesContentGenerator
        private lateinit var temporaryDirectory: File

        @Before
        fun setUp() {
            classUnderTest = ArchitectureModulesContentGenerator()
            temporaryDirectory = createTempDirectory(prefix = "test").toFile()
        }

        @Test
        fun `Given ktlint and detekt enabled when generate then domain build gradle includes plugins and configurations`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = false
            val enableKtlint = true
            val enableDetekt = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            val buildGradleFile = File(architectureRoot, "domain/build.gradle.kts")
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
    config.setFrom("${'$'}projectDir/../../detekt.yml")
}

dependencies {
    implementation(projects.coroutine)
    implementation(libs.kotlinx.coroutines.core)
}
"""
            assertEquals("Domain build.gradle.kts should have exact content", expectedContent, buildGradleFile.readText())
        }

        @Test
        fun `Given ktlint and detekt disabled when generate then domain build gradle excludes plugins and configurations`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = false
            val enableKtlint = false
            val enableDetekt = false

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            val buildGradleFile = File(architectureRoot, "domain/build.gradle.kts")
            val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.coroutine)
    implementation(libs.kotlinx.coroutines.core)
}
"""
            assertEquals("Domain build.gradle.kts should have exact content", expectedContent, buildGradleFile.readText())
        }

        @Test
        fun `Given ktlint and detekt enabled when generate then presentation build gradle includes plugins and configurations`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = false
            val enableKtlint = true
            val enableDetekt = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            val buildGradleFile = File(architectureRoot, "presentation/build.gradle.kts")
            val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../detekt.yml")
}

dependencies {
    implementation(projects.architecture.domain)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.test.junit)
}
"""
            assertEquals("Presentation build.gradle.kts should have exact content", expectedContent, buildGradleFile.readText())
        }

        @Test
        fun `Given ktlint and detekt enabled when generate then ui build gradle includes plugins and configurations`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = false
            val enableKtlint = true
            val enableDetekt = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            val buildGradleFile = File(architectureRoot, "ui/build.gradle.kts")
            val expectedContent = """plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.example.architecture.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
    config.setFrom("${'$'}projectDir/../../detekt.yml")
}

dependencies {
    implementation(projects.architecture.presentation)

    implementation(projects.coroutine)

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
}
"""
            assertEquals("UI build.gradle.kts should have exact content", expectedContent, buildGradleFile.readText())
        }

        @Test
        fun `Given ktlint and detekt enabled when generate then presentation-test build gradle includes plugins and configurations`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = false
            val enableKtlint = true
            val enableDetekt = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            val buildGradleFile = File(architectureRoot, "presentation-test/build.gradle.kts")
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
    config.setFrom("${'$'}projectDir/../../detekt.yml")
}

dependencies {
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.test.junit)
    implementation(projects.coroutine)
}
"""
            assertEquals("Presentation-test build.gradle.kts should have exact content", expectedContent, buildGradleFile.readText())
        }

        @Test
        fun `Given ktlint and detekt enabled when generate then instrumentation-test build gradle includes plugins and configurations`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = false
            val enableKtlint = true
            val enableDetekt = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            val buildGradleFile = File(architectureRoot, "instrumentation-test/build.gradle.kts")
            val expectedContent = """plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.example.test"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../detekt.yml")
}

dependencies {
    implementation(libs.material)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    implementation(libs.test.junit)
    implementation(libs.test.androidx.junit)
    implementation(libs.test.androidx.espresso.core)
    implementation(libs.test.compose.ui.junit4)
    implementation(libs.test.android.hilt)
    implementation(libs.test.android.uiautomator)
    implementation(libs.test.androidx.espresso.core)
    implementation(libs.okhttp3)
    implementation(libs.test.android.mockwebserver)
    implementation(libs.androidx.appcompat)
    implementation(libs.test.androidx.rules)
    implementation(libs.androidx.recyclerview)
    implementation(kotlin("reflect"))
}
"""
            assertEquals("Instrumentation-test build.gradle.kts should have exact content", expectedContent, buildGradleFile.readText())
        }

        @Test
        fun `Given only ktlint enabled when generate then build gradle includes only ktlint plugin and configuration`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = false
            val enableKtlint = true
            val enableDetekt = false

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            val buildGradleFile = File(architectureRoot, "domain/build.gradle.kts")
            val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

dependencies {
    implementation(projects.coroutine)
    implementation(libs.kotlinx.coroutines.core)
}
"""
            assertEquals("Domain build.gradle.kts should have exact content", expectedContent, buildGradleFile.readText())
        }

        @Test
        fun `Given only detekt enabled when generate then build gradle includes only detekt plugin and configuration`() {
            // Given
            val architectureRoot = File(temporaryDirectory, "architecture").apply { mkdirs() }
            val architecturePackageName = "com.example.architecture"
            val enableCompose = false
            val enableKtlint = false
            val enableDetekt = true

            // When
            classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose, enableKtlint, enableDetekt)

            // Then
            val buildGradleFile = File(architectureRoot, "domain/build.gradle.kts")
            val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
}

detekt {
    config.setFrom("${'$'}projectDir/../../detekt.yml")
}

dependencies {
    implementation(projects.coroutine)
    implementation(libs.kotlinx.coroutines.core)
}
"""
            assertEquals("Domain build.gradle.kts should have exact content", expectedContent, buildGradleFile.readText())
        }
    }
}
