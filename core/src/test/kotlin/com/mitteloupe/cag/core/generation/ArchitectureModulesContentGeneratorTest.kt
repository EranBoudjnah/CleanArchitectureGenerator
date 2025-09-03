package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class ArchitectureModulesContentGeneratorTest {
    private lateinit var classUnderTest: ArchitectureModulesContentGenerator
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        classUnderTest = ArchitectureModulesContentGenerator()
        tempDirectory = createTempDirectory(prefix = "test").toFile()
    }

    @Test(expected = GenerationException::class)
    fun `Given empty architecture package name when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = ""
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given invalid architecture package name when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "..."
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given architecture root that is a file when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { createNewFile() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }

    @Test
    fun `Given valid architecture package when generate then creates UseCase interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val useCaseFile = File(architectureRoot, "domain/src/main/java/com/example/architecture/domain/usecase/UseCase.kt")
        val expectedContent = """package com.example.architecture.domain.usecase

interface UseCase<REQUEST, RESULT> {
    fun execute(input: REQUEST, onResult: (RESULT) -> Unit)
}
"""
        assertEquals("UseCase.kt should have exact content", expectedContent, useCaseFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates DomainException class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val domainExceptionFile =
            File(
                architectureRoot,
                "domain/src/main/java/com/example/architecture/domain/exception/DomainException.kt"
            )
        val expectedContent = """package com.example.architecture.domain.exception

abstract class DomainException(cause: Throwable? = null) : Exception(cause)
"""
        assertEquals("DomainException.kt should have exact content", expectedContent, domainExceptionFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates UnknownDomainException class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val unknownDomainExceptionFile =
            File(architectureRoot, "domain/src/main/java/com/example/architecture/domain/exception/UnknownDomainException.kt")
        val expectedContent = """package com.example.architecture.domain.exception

class UnknownDomainException(cause: Throwable? = null) : DomainException(cause)
"""
        assertEquals("UnknownDomainException.kt should have exact content", expectedContent, unknownDomainExceptionFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates UseCaseExecutor class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val useCaseExecutorFile = File(architectureRoot, "domain/src/main/java/com/example/architecture/domain/UseCaseExecutor.kt")
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
    fun `Given valid architecture package when generate then creates UseCaseExecutorProvider typealias with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val useCaseExecutorProviderFile =
            File(
                architectureRoot,
                "domain/src/main/java/com/example/architecture/domain/UseCaseExecutorProvider.kt"
            )
        val expectedContent = """package com.example.architecture.domain

import kotlinx.coroutines.CoroutineScope

typealias UseCaseExecutorProvider =
    @JvmSuppressWildcards (coroutineScope: CoroutineScope) -> UseCaseExecutor
"""
        assertEquals("UseCaseExecutorProvider.kt should have exact content", expectedContent, useCaseExecutorProviderFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates BackgroundExecutingUseCase class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val backgroundUseCaseFile =
            File(architectureRoot, "domain/src/main/java/com/example/architecture/domain/usecase/BackgroundExecutingUseCase.kt")
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
    fun `Given valid architecture package when generate then creates ContinuousExecutingUseCase class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val continuousUseCaseFile =
            File(architectureRoot, "domain/src/main/java/com/example/architecture/domain/usecase/ContinuousExecutingUseCase.kt")
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
    fun `Given valid architecture package when generate then creates BaseViewModel class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val viewModelFile =
            File(architectureRoot, "presentation/src/main/java/com/example/architecture/presentation/viewmodel/BaseViewModel.kt")
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
    fun `Given valid architecture package when generate then creates PresentationNavigationEvent interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val navigationEventFile =
            File(
                architectureRoot,
                "presentation/src/main/java/com/example/architecture/presentation/navigation/PresentationNavigationEvent.kt"
            )
        val expectedContent = """package com.example.architecture.presentation.navigation

interface PresentationNavigationEvent {
    object Back : PresentationNavigationEvent
}
"""
        assertEquals("PresentationNavigationEvent.kt should have exact content", expectedContent, navigationEventFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates PresentationNotification interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val notificationFile =
            File(
                architectureRoot,
                "presentation/src/main/java/com/example/architecture/presentation/notification/PresentationNotification.kt"
            )
        val expectedContent = """package com.example.architecture.presentation.notification

interface PresentationNotification
"""
        assertEquals("PresentationNotification.kt should have exact content", expectedContent, notificationFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates ViewStateBinder interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val viewStateBinderFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/binder/ViewStateBinder.kt")
        val expectedContent = """package com.example.architecture.ui.binder

import com.example.architecture.ui.view.ViewsProvider

interface ViewStateBinder<in VIEW_STATE : Any, in VIEWS_PROVIDER : ViewsProvider> {
    fun VIEWS_PROVIDER.bindState(viewState: VIEW_STATE)
}
"""
        assertEquals("ViewStateBinder.kt should have exact content", expectedContent, viewStateBinderFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates UnhandledNavigationException class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val unhandledNavigationExceptionFile =
            File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/navigation/exception/UnhandledNavigationException.kt")
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
    fun `Given valid architecture package when generate then creates NavigationEventDestinationMapper class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val navigationEventDestinationMapperFile =
            File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/navigation/mapper/NavigationEventDestinationMapper.kt")
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
    fun `Given valid architecture package when generate then creates UiDestination interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val uiDestinationFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/navigation/model/UiDestination.kt")
        val expectedContent = """package com.example.architecture.ui.navigation.model

import androidx.navigation.NavController

fun interface UiDestination {
    fun navigate(navController: NavController)
}
"""
        assertEquals("UiDestination.kt should have exact content", expectedContent, uiDestinationFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates NotificationUiMapper interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val notificationUiMapperFile =
            File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/notification/mapper/NotificationUiMapper.kt")
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
    fun `Given valid architecture package when generate then creates UiNotification interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val uiNotificationFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/notification/model/UiNotification.kt")
        val expectedContent = """package com.example.architecture.ui.notification.model

fun interface UiNotification {
    fun present()
}
"""
        assertEquals("UiNotification.kt should have exact content", expectedContent, uiNotificationFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates BaseComposeHolder class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val baseComposeHolderFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/view/BaseComposeHolder.kt")
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
    fun `Given valid architecture package when generate then creates BaseFragment class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val baseFragmentFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/view/BaseFragment.kt")
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
    fun `Given valid architecture package when generate then creates ScreenEnterObserver function with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val screenEnterObserverFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/view/ScreenEnterObserver.kt")
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
    fun `Given valid architecture package when generate then creates ViewsProvider interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val viewsProviderFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/ui/view/ViewsProvider.kt")
        val expectedContent = """package com.example.architecture.ui.view

interface ViewsProvider
"""
        assertEquals("ViewsProvider.kt should have exact content", expectedContent, viewsProviderFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates Domain gradle file with correct content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val domainGradleFile = File(architectureRoot, "domain/build.gradle.kts")
        val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.coroutine)
    implementation(libs.kotlinx.coroutines.core)
}
"""
        assertEquals("Domain build.gradle.kts should have exact content", expectedContent, domainGradleFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates Presentation gradle file with correct content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val presentationGradleFile = File(architectureRoot, "presentation/build.gradle.kts")
        val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

dependencies {
    implementation(projects.architecture.domain)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit4)
}
"""
        assertEquals("Presentation build.gradle.kts should have exact content", expectedContent, presentationGradleFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates UI gradle file with correct content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val uiGradleFile = File(architectureRoot, "ui/build.gradle.kts")
        val expectedContent = """plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
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

dependencies {
    implementation(projects.architecture.presentation)

    implementation(projects.coroutine)

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
}
"""
        assertEquals("UI build.gradle.kts should have exact content", expectedContent, uiGradleFile.readText())
    }
}
