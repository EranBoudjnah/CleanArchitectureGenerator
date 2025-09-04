package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildArchitectureDomainGradleScript
import com.mitteloupe.cag.core.content.buildArchitectureInstrumentationTestGradleScript
import com.mitteloupe.cag.core.content.buildArchitecturePresentationGradleScript
import com.mitteloupe.cag.core.content.buildArchitecturePresentationTestGradleScript
import com.mitteloupe.cag.core.content.buildArchitectureUiGradleScript
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class ArchitectureModulesContentGenerator {
    fun generate(
        architectureRoot: File,
        architecturePackageName: String,
        enableCompose: Boolean
    ) {
        val packageSegments = architecturePackageName.toSegments()
        if (packageSegments.isEmpty()) {
            throw GenerationException("Architecture package name is invalid.")
        }

        val layers = listOf("domain", "presentation", "ui", "presentation-test", "instrumentation-test")
        val allCreated =
            layers.all { layerName ->
                val layerSourceRoot = File(architectureRoot, "$layerName/src/main/java")
                val destinationDirectory = buildPackageDirectory(layerSourceRoot, packageSegments)
                if (destinationDirectory.exists()) {
                    destinationDirectory.isDirectory
                } else {
                    destinationDirectory.mkdirs()
                }
            }

        if (!allCreated) {
            throw GenerationException("Failed to create directories for architecture package '$architecturePackageName'.")
        }

        val catalogUpdater = VersionCatalogUpdater()
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = architectureRoot.parentFile,
            enableCompose = enableCompose
        )

        createDomainModule(architectureRoot, catalogUpdater)
        createPresentationModule(architectureRoot, catalogUpdater)
        createUiModule(architectureRoot, architecturePackageName, catalogUpdater)
        createPresentationTestModule(architectureRoot, catalogUpdater)
        createInstrumentationTestModule(architectureRoot, architecturePackageName, catalogUpdater)

        val domainRoot = File(architectureRoot, "domain")
        generateDomainContent(domainRoot, architecturePackageName, packageSegments + "domain")
        val presentationRoot = File(architectureRoot, "presentation")
        generatePresentationContent(presentationRoot, architecturePackageName, packageSegments + "presentation")
        val presentationTestRoot = File(architectureRoot, "presentation-test")
        generatePresentationTestContent(presentationTestRoot, architecturePackageName, packageSegments + "presentation")
        val uiRoot = File(architectureRoot, "ui")
        generateUiContent(uiRoot, architecturePackageName, packageSegments + "ui")
        val instrumentationTestRoot = File(architectureRoot, "instrumentation-test")
        generateInstrumentationTestContent(
            instrumentationTestRoot,
            architecturePackageName.substringBeforeLast('.'),
            architecturePackageName.substringBeforeLast('.').toSegments() + "test"
        )
    }

    private fun createDomainModule(
        architectureRoot: File,
        catalog: VersionCatalogUpdater
    ) {
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "domain",
            content = buildArchitectureDomainGradleScript(catalog)
        )
    }

    private fun createPresentationModule(
        architectureRoot: File,
        catalog: VersionCatalogUpdater
    ) {
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "presentation",
            content = buildArchitecturePresentationGradleScript(catalog)
        )
    }

    private fun createPresentationTestModule(
        architectureRoot: File,
        catalog: VersionCatalogUpdater
    ) {
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "presentation-test",
            content = buildArchitecturePresentationTestGradleScript(catalog)
        )
    }

    private fun createUiModule(
        architectureRoot: File,
        architecturePackageName: String,
        catalog: VersionCatalogUpdater
    ) {
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "ui",
            content = buildArchitectureUiGradleScript(architecturePackageName, catalog)
        )
    }

    private fun createInstrumentationTestModule(
        architectureRoot: File,
        architecturePackageName: String,
        catalog: VersionCatalogUpdater
    ) {
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "instrumentation-test",
            content = buildArchitectureInstrumentationTestGradleScript(architecturePackageName, catalog)
        )
    }

    private fun generateDomainContent(
        architectureRoot: File,
        moduleNamespace: String,
        architecturePackageNameSegments: List<String>
    ) {
        val codeRoot = File(architectureRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(codeRoot, architecturePackageNameSegments)

        generateUseCase(packageDirectory, moduleNamespace)
        generateBackgroundExecutingUseCase(packageDirectory, moduleNamespace)
        generateContinuousExecutingUseCase(packageDirectory, moduleNamespace)
        generateDomainException(packageDirectory, moduleNamespace)
        generateUnknownDomainException(packageDirectory, moduleNamespace)
        generateUseCaseExecutor(packageDirectory, moduleNamespace)
        generateUseCaseExecutorProvider(packageDirectory, moduleNamespace)
    }

    private fun generatePresentationContent(
        architectureRoot: File,
        architecturePackageName: String,
        architecturePackageNameSegments: List<String>
    ) {
        val codeRoot = File(architectureRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(codeRoot, architecturePackageNameSegments)

        generateViewModelBase(packageDirectory, architecturePackageName)
        generateNavigationEventBase(packageDirectory, architecturePackageName)
        generatePresentationNotification(packageDirectory, architecturePackageName)
    }

    private fun generatePresentationTestContent(
        architectureRoot: File,
        architecturePackageName: String,
        architecturePackageNameSegments: List<String>
    ) {
        val codeRoot = File(architectureRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(codeRoot, architecturePackageNameSegments)

        generateBaseViewModelTest(packageDirectory, architecturePackageName)
    }

    private fun generateUiContent(
        architectureRoot: File,
        moduleNamespace: String,
        architecturePackageNameSegments: List<String>
    ) {
        val codeRoot = File(architectureRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(codeRoot, architecturePackageNameSegments)

        generateViewStateBinder(packageDirectory, moduleNamespace)
        generateUnhandledNavigationException(packageDirectory, moduleNamespace)
        generateNavigationEventDestinationMapper(packageDirectory, moduleNamespace)
        generateUiDestination(packageDirectory, moduleNamespace)
        generateNotificationUiMapper(packageDirectory, moduleNamespace)
        generateUiNotification(packageDirectory, moduleNamespace)
        generateBaseComposeHolder(packageDirectory, moduleNamespace)
        generateBaseFragment(packageDirectory, moduleNamespace)
        generateScreenEnterObserver(packageDirectory, moduleNamespace)
        generateViewsProvider(packageDirectory, moduleNamespace)
    }

    private fun generateUseCase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "usecase/UseCase.kt",
            content =
                """
                package $moduleNamespace.domain.usecase

                interface UseCase<REQUEST, RESULT> {
                    fun execute(input: REQUEST, onResult: (RESULT) -> Unit)
                }

                """.trimIndent(),
            errorMessage = "use case"
        )
    }

    private fun generateBackgroundExecutingUseCase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val imports =
            """
import ${moduleNamespace.substringBeforeLast('.')}.coroutine.CoroutineContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
""".optimizeImports()

        val content =
            """package $moduleNamespace.domain.usecase

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "usecase/BackgroundExecutingUseCase.kt",
            content = content,
            errorMessage = "background executing use case"
        )
    }

    private fun generateContinuousExecutingUseCase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val imports =
            """
import ${moduleNamespace.substringBeforeLast('.')}.coroutine.CoroutineContextProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
""".optimizeImports()

        val content =
            """package $moduleNamespace.domain.usecase

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "usecase/ContinuousExecutingUseCase.kt",
            content = content,
            errorMessage = "continuous executing use case"
        )
    }

    private fun generateUseCaseExecutor(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val content =
            """package $moduleNamespace.domain

${
                """
import $moduleNamespace.domain.exception.DomainException
import $moduleNamespace.domain.exception.UnknownDomainException
import $moduleNamespace.domain.usecase.UseCase
""".optimizeImports()
            }
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "UseCaseExecutor.kt",
            content = content,
            errorMessage = "use case executor"
        )
    }

    private fun generateUseCaseExecutorProvider(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val content =
            """package $moduleNamespace.domain

import kotlinx.coroutines.CoroutineScope

typealias UseCaseExecutorProvider =
    @JvmSuppressWildcards (coroutineScope: CoroutineScope) -> UseCaseExecutor
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "UseCaseExecutorProvider.kt",
            content = content,
            errorMessage = "use case executor provider"
        )
    }

    private fun generateDomainException(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "exception/DomainException.kt",
            content =
                """
                package $moduleNamespace.domain.exception

                abstract class DomainException(cause: Throwable? = null) : Exception(cause)

                """.trimIndent(),
            errorMessage = "domain exception"
        )
    }

    private fun generateUnknownDomainException(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "exception/UnknownDomainException.kt",
            content =
                """
                package $moduleNamespace.domain.exception

                class UnknownDomainException(cause: Throwable? = null) : DomainException(cause)

                """.trimIndent(),
            errorMessage = "unknown domain exception"
        )
    }

    private fun generateViewModelBase(
        packageDirectory: File,
        architecturePackage: String
    ) {
        val content =
            """package $architecturePackage.presentation.viewmodel

${
                """
import $architecturePackage.domain.UseCaseExecutor
import $architecturePackage.domain.exception.DomainException
import $architecturePackage.domain.usecase.UseCase
import $architecturePackage.presentation.navigation.PresentationNavigationEvent
import $architecturePackage.presentation.notification.PresentationNotification
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
""".optimizeImports()
            }
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "viewmodel/BaseViewModel.kt",
            content = content,
            errorMessage = "view model"
        )
    }

    private fun generateNavigationEventBase(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "navigation/PresentationNavigationEvent.kt",
            content =
                """
                package $architecturePackageName.presentation.navigation

                interface PresentationNavigationEvent {
                    object Back : PresentationNavigationEvent
                }
                
                """.trimIndent(),
            errorMessage = "navigation event"
        )
    }

    private fun generateViewStateBinder(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "binder/ViewStateBinder.kt",
            content =
                """package $moduleNamespace.ui.binder

import $moduleNamespace.ui.view.ViewsProvider

interface ViewStateBinder<in VIEW_STATE : Any, in VIEWS_PROVIDER : ViewsProvider> {
    fun VIEWS_PROVIDER.bindState(viewState: VIEW_STATE)
}
""",
            errorMessage = "view state binder"
        )
    }

    private fun generateUnhandledNavigationException(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "navigation/exception/UnhandledNavigationException.kt",
            content =
                """package $moduleNamespace.ui.navigation.exception

import $moduleNamespace.presentation.navigation.PresentationNavigationEvent

class UnhandledNavigationException(event: PresentationNavigationEvent) :
    IllegalArgumentException(
        "Navigation event ${'$'}{event::class.simpleName} was not handled."
    )
""",
            errorMessage = "unhandled navigation exception"
        )
    }

    private fun generateNavigationEventDestinationMapper(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val imports =
            """
import $moduleNamespace.presentation.navigation.PresentationNavigationEvent
import $moduleNamespace.ui.navigation.exception.UnhandledNavigationException
import $moduleNamespace.ui.navigation.model.UiDestination
import kotlin.reflect.KClass
""".optimizeImports()

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "navigation/mapper/NavigationEventDestinationMapper.kt",
            content =
                """package $moduleNamespace.ui.navigation.mapper

$imports
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
""",
            errorMessage = "navigation event destination mapper"
        )
    }

    private fun generateUiDestination(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "navigation/model/UiDestination.kt",
            content =
                """package $moduleNamespace.ui.navigation.model

import androidx.navigation.NavController

fun interface UiDestination {
    fun navigate(navController: NavController)
}
""",
            errorMessage = "ui destination"
        )
    }

    private fun generateNotificationUiMapper(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "notification/mapper/NotificationUiMapper.kt",
            content =
                """package $moduleNamespace.ui.notification.mapper

import $moduleNamespace.presentation.notification.PresentationNotification
import $moduleNamespace.ui.notification.model.UiNotification

interface NotificationUiMapper<in PRESENTATION_NOTIFICATION : PresentationNotification> {
    fun toUi(notification: PRESENTATION_NOTIFICATION): UiNotification
}
""",
            errorMessage = "notification ui mapper"
        )
    }

    private fun generateUiNotification(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "notification/model/UiNotification.kt",
            content =
                """package $moduleNamespace.ui.notification.model

fun interface UiNotification {
    fun present()
}
""",
            errorMessage = "ui notification"
        )
    }

    private fun generateBaseComposeHolder(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val imports =
            """
import $moduleNamespace.presentation.navigation.PresentationNavigationEvent
import $moduleNamespace.presentation.notification.PresentationNotification
import $moduleNamespace.presentation.viewmodel.BaseViewModel
import $moduleNamespace.ui.navigation.mapper.NavigationEventDestinationMapper
import $moduleNamespace.ui.notification.mapper.NotificationUiMapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
""".optimizeImports()

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "view/BaseComposeHolder.kt",
            content =
                """package $moduleNamespace.ui.view

$imports
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
""",
            errorMessage = "base compose holder"
        )
    }

    private fun generateBaseFragment(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val imports =
            """
import $moduleNamespace.presentation.navigation.PresentationNavigationEvent
import $moduleNamespace.presentation.notification.PresentationNotification
import $moduleNamespace.presentation.viewmodel.BaseViewModel
import $moduleNamespace.ui.binder.ViewStateBinder
import $moduleNamespace.ui.navigation.mapper.NavigationEventDestinationMapper
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
""".optimizeImports()

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "view/BaseFragment.kt",
            content =
                """package $moduleNamespace.ui.view

$imports
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
""",
            errorMessage = "base fragment"
        )
    }

    private fun generateScreenEnterObserver(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val imports =
            """
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
""".optimizeImports()

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "view/ScreenEnterObserver.kt",
            content =
                """package $moduleNamespace.ui.view

$imports
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
""",
            errorMessage = "screen enter observer"
        )
    }

    private fun generateViewsProvider(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "view/ViewsProvider.kt",
            content =
                """package $moduleNamespace.ui.view

interface ViewsProvider
""",
            errorMessage = "views provider"
        )
    }

    private fun generatePresentationNotification(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "notification/PresentationNotification.kt",
            content =
                """
                package $architecturePackageName.presentation.notification

                interface PresentationNotification

                """.trimIndent(),
            errorMessage = "presentation notification"
        )
    }

    private fun generateInstrumentationTestContent(
        architectureRoot: File,
        architecturePackageName: String,
        architecturePackageNameSegments: List<String>
    ) {
        val codeRoot = File(architectureRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(codeRoot, architecturePackageNameSegments)

        generateBaseTest(packageDirectory, architecturePackageName)
        generateClickChildView(packageDirectory, architecturePackageName)
        generateServerRequestResponseAnnotation(packageDirectory, architecturePackageName)
        generateLocalStoreAnnotation(packageDirectory, architecturePackageName)
        generateItemAtPositionMatcher(packageDirectory, architecturePackageName)
        generateAssetReader(packageDirectory, architecturePackageName)
        generateAppNotRespondingHandler(packageDirectory, architecturePackageName)
        generateComposeOkHttp3IdlingResource(packageDirectory, architecturePackageName)
        generateEspressoOkHttp3IdlingResource(packageDirectory, architecturePackageName)
        generateAppLauncher(packageDirectory, architecturePackageName)
        generateFromComposable(packageDirectory, architecturePackageName)
        generateKeyValueStore(packageDirectory, architecturePackageName)
        generateWithBackgroundColorMatcher(packageDirectory, architecturePackageName)
        generateWithDrawableIdMatcher(packageDirectory, architecturePackageName)
        generateHiltInjectorRule(packageDirectory, architecturePackageName)
        generateLocalStoreRule(packageDirectory, architecturePackageName)
        generateScreenshotFailureRule(packageDirectory, architecturePackageName)
        generateSdkAwareGrantPermissionRule(packageDirectory, architecturePackageName)
        generateWebServerRule(packageDirectory, architecturePackageName)
        generateLoggingSslSocketFactory(packageDirectory, architecturePackageName)
        generateMockDispatcher(packageDirectory, architecturePackageName)
        generateMockRequest(packageDirectory, architecturePackageName)
        generateMockRequestResponseFactory(packageDirectory, architecturePackageName)
        generateMockResponse(packageDirectory, architecturePackageName)
        generateMockWebServerProvider(packageDirectory, architecturePackageName)
        generateErrorResponseFactory(packageDirectory, architecturePackageName)
        generateMockResponseFactory(packageDirectory, architecturePackageName)
        generateSequenceResponseFactory(packageDirectory, architecturePackageName)
        generateSimpleResponseFactory(packageDirectory, architecturePackageName)
        generateResponseBinder(packageDirectory, architecturePackageName)
        generateResponseStore(packageDirectory, architecturePackageName)
        generateDoesNot(packageDirectory, architecturePackageName)
        generateRetry(packageDirectory, architecturePackageName)
    }

    private fun generateBaseTest(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
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
import $architecturePackageName.test.idlingresource.findAndCloseAppNotRespondingDialog
import $architecturePackageName.test.idlingresource.registerAppNotRespondingWatcher
import $architecturePackageName.test.launcher.AppLauncher
import $architecturePackageName.test.localstore.KeyValueStore
import $architecturePackageName.test.rule.HiltInjectorRule
import $architecturePackageName.test.rule.LocalStoreRule
import $architecturePackageName.test.rule.ScreenshotFailureRule
import $architecturePackageName.test.rule.SdkAwareGrantPermissionRule
import $architecturePackageName.test.rule.WebServerRule
import $architecturePackageName.test.server.MockDispatcher
import $architecturePackageName.test.server.MockWebServerProvider
import $architecturePackageName.test.server.ResponseStore
import dagger.hilt.android.testing.HiltAndroidRule
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.RuleChain
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.test

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "test/BaseTest.kt",
            content = content,
            errorMessage = "base test"
        )
    }

    private fun generateClickChildView(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.action

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "action/ClickChildView.kt",
            content = content,
            errorMessage = "click child view"
        )
    }

    private fun generateServerRequestResponseAnnotation(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "annotation/ServerRequestResponse.kt",
            content =
                """package $architecturePackageName.test.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class ServerRequestResponse(val requestResponseIds: Array<String>)
""",
            errorMessage = "server request response annotation"
        )
    }

    private fun generateLocalStoreAnnotation(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "annotation/LocalStore.kt",
            content =
                """package $architecturePackageName.test.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class LocalStore(val localStoreDataIds: Array<String>)
""",
            errorMessage = "local store annotation"
        )
    }

    private fun generateItemAtPositionMatcher(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.assertion

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "assertion/ItemAtPositionMatcher.kt",
            content = content,
            errorMessage = "item at position matcher"
        )
    }

    private fun generateAssetReader(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.asset

$imports
fun getAssetAsString(name: String): String =
    processAssetStream(name) { stream -> stream.bufferedReader().readText() }

fun <OUTPUT> processAssetStream(
    filename: String,
    performOnStream: (inputStream: InputStream) -> OUTPUT
): OUTPUT = InstrumentationRegistry.getInstrumentation().context.assets.open(filename)
    .use { stream -> performOnStream(stream) }
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "asset/AssetReader.kt",
            content = content,
            errorMessage = "asset reader"
        )
    }

    private fun generateAppNotRespondingHandler(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.util.Log
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.idlingresource

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "idlingresource/AppNotRespondingHandler.kt",
            content = content,
            errorMessage = "app not responding handler"
        )
    }

    private fun generateComposeOkHttp3IdlingResource(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import androidx.compose.ui.test.IdlingResource
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.idlingresource

$imports
class ComposeOkHttp3IdlingResource private constructor(dispatcher: Dispatcher) : IdlingResource {
    override val isIdleNow: Boolean = dispatcher.runningCallsCount() == 0

    companion object {
        fun create(client: OkHttpClient): ComposeOkHttp3IdlingResource =
            ComposeOkHttp3IdlingResource(client.dispatcher)
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "idlingresource/ComposeOkHttp3IdlingResource.kt",
            content = content,
            errorMessage = "compose okhttp3 idling resource"
        )
    }

    private fun generateEspressoOkHttp3IdlingResource(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.idlingresource

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "idlingresource/EspressoOkHttp3IdlingResource.kt",
            content = content,
            errorMessage = "espresso okhttp3 idling resource"
        )
    }

    private fun generateAppLauncher(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "launcher/AppLauncher.kt",
            content =
                """package $architecturePackageName.test.launcher

fun interface AppLauncher {
    fun launch()
}
""",
            errorMessage = "app launcher"
        )
    }

    private fun generateFromComposable(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import $architecturePackageName.test.test.TypedAndroidComposeTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.launcher

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "launcher/FromComposable.kt",
            content = content,
            errorMessage = "from composable"
        )
    }

    private fun generateKeyValueStore(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "localstore/KeyValueStore.kt",
            content =
                """package $architecturePackageName.test.localstore

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
""",
            errorMessage = "key value store"
        )
    }

    private fun generateWithBackgroundColorMatcher(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.matcher

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "matcher/WithBackgroundColorMatcher.kt",
            content = content,
            errorMessage = "with background color matcher"
        )
    }

    private fun generateWithDrawableIdMatcher(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
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
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.matcher

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "matcher/WithDrawableIdMatcher.kt",
            content = content,
            errorMessage = "with drawable id matcher"
        )
    }

    private fun generateHiltInjectorRule(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.rule

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "rule/HiltInjectorRule.kt",
            content = content,
            errorMessage = "hilt injector rule"
        )
    }

    private fun generateLocalStoreRule(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.content.SharedPreferences
import androidx.core.content.edit
import $architecturePackageName.test.annotation.LocalStore
import $architecturePackageName.test.localstore.KeyValueStore
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.rule

$imports
class LocalStoreRule(
    private val lazySharedPreferences: Lazy<SharedPreferences>,
    private val lazyKeyValueStore: Lazy<KeyValueStore>
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        LocalStoreInitializationStatement(
            lazySharedPreferences,
            lazyKeyValueStore,
            base,
            description
        )

    private class LocalStoreInitializationStatement(
        private val lazySharedPreferences: Lazy<SharedPreferences>,
        private val lazyKeyValueStore: Lazy<KeyValueStore>,
        private val base: Statement,
        private val description: Description
    ) : Statement() {
        private val sharedPreferences by lazy { lazySharedPreferences.value }
        private val keyValueStore by lazy { lazyKeyValueStore.value }

        override fun evaluate() {
            sharedPreferences.edit {
                clear()

                description.localStoreDataIds()
                    .map { localStoreDataId ->
                        requireNotNull(keyValueStore.keyValues[localStoreDataId]) {
                            "Request/Response ID ${'$'}localStoreDataId not found."
                        }
                    }.forEach { keyValuePair ->
                        val (key, value) = keyValuePair
                        persistValue(key, value)
                    }
            }

            base.evaluate()

            sharedPreferences.edit {
                clear()
            }
        }

        private fun Description.localStoreDataIds() = annotations.filterIsInstance<LocalStore>()
            .flatMap { serverResponse -> serverResponse.localStoreDataIds.toList() }

        private fun SharedPreferences.Editor.persistValue(key: String, value: Any) = when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
            is Set<*> -> {
                @Suppress("UNCHECKED_CAST")
                putStringSet(key, value as Set<String>)
            }

            else -> throw IllegalArgumentException("${'$'}value is of an unsupported type.")
        }
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "rule/LocalStoreRule.kt",
            content = content,
            errorMessage = "local store rule"
        )
    }

    private fun generateScreenshotFailureRule(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.rules.TestWatcher
import org.junit.runner.Description
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.rule

$imports
private const val TAG = "Test"

private val deviceLanguage by lazy { Locale.getDefault().language }

private val dateFormat by lazy { SimpleDateFormat("EEE-MMMM-dd-HH:mm:ss", Locale.US) }
private fun getDate() = dateFormat.format(Date())

private const val SCREENSHOT_FOLDER_LOCATION = ""

private val contentValues = ContentValues().apply {
    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    }
}

class ScreenshotFailureRule : TestWatcher() {
    override fun failed(e: Throwable?, description: Description) {
        val screenShotName = "${'$'}deviceLanguage-${'$'}{description.methodName}-${'$'}{getDate()}"
        val bitmap = getInstrumentation().uiAutomation.takeScreenshot()
        storeFailureScreenshot(bitmap, screenShotName)
    }
}

private fun storeFailureScreenshot(bitmap: Bitmap, screenshotName: String) {
    val contentResolver = getInstrumentation().targetContext.applicationContext.contentResolver

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        useMediaStoreScreenshotStorage(
            contentResolver,
            screenshotName,
            bitmap
        )
    } else {
        usePublicExternalScreenshotStorage(
            contentResolver,
            screenshotName,
            bitmap
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun useMediaStoreScreenshotStorage(
    contentResolver: ContentResolver,
    screenshotName: String,
    bitmap: Bitmap
) {
    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, screenshotName.jpg)
    contentValues.put(
        MediaStore.Images.Media.RELATIVE_PATH,
        Environment.DIRECTORY_PICTURES + SCREENSHOT_FOLDER_LOCATION
    )

    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?.let { uri ->
            Log.d(TAG, "Saving screenshot to ${'$'}uri")
            contentResolver.openOutputStream(uri)?.let { saveScreenshotToStream(bitmap, it) }
            contentResolver.update(uri, contentValues, null, null)
        }
}

private fun usePublicExternalScreenshotStorage(
    contentResolver: ContentResolver,
    screenshotName: String,
    bitmap: Bitmap
) {
    val directory = File(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES + SCREENSHOT_FOLDER_LOCATION
        ).toString()
    )

    if (!directory.exists()) {
        directory.mkdirs()
    }

    val file = File(directory, screenshotName.jpg)
    Log.d(TAG, "Saving screenshot to ${'$'}{file.absolutePath}")
    saveScreenshotToStream(bitmap, FileOutputStream(file))

    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

private fun saveScreenshotToStream(bitmap: Bitmap, outputStream: OutputStream) {
    outputStream.use { openStream ->
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, openStream)
            Log.d(TAG, "Screenshot saved.")
        } catch (ioException: IOException) {
            Log.e(TAG, "Screenshot was not stored at this time: ${'$'}{ioException.message}")
        }
    }
}

private val String.jpg
    get() = "${'$'}{this.replace(":", "_")}.jpg"
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "rule/ScreenshotFailureRule.kt",
            content = content,
            errorMessage = "screenshot failure rule"
        )
    }

    private fun generateSdkAwareGrantPermissionRule(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.test.internal.platform.ServiceLoaderWrapper
import androidx.test.internal.platform.content.PermissionGranter
import androidx.test.runner.permission.PermissionRequester
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.rule

$imports
@SuppressLint("RestrictedApi")
class SdkAwareGrantPermissionRule(
    private val permissionGranter: PermissionGranter,
    vararg permissions: String
) : TestRule {

    init {
        val permissionSet = satisfyPermissionDependencies(*permissions)
        permissionGranter.addPermissions(*permissionSet.toTypedArray())
    }

    @VisibleForTesting
    private fun satisfyPermissionDependencies(vararg permissions: String): Set<String> {
        val permissionsSet: MutableSet<String> = LinkedHashSet(listOf(*permissions))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (permissionsSet.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionsSet.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            permissionsSet.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return permissionsSet
    }

    override fun apply(base: Statement, description: Description): Statement =
        RequestPermissionStatement(base, permissionGranter)

    private class RequestPermissionStatement(
        private val base: Statement,
        private val permissionGranter: PermissionGranter
    ) : Statement() {

        @Throws(Throwable::class)
        override fun evaluate() {
            permissionGranter.requestPermissions()
            base.evaluate()
        }
    }

    companion object {
        fun grant(vararg permissions: String): SdkAwareGrantPermissionRule {
            val granter = ServiceLoaderWrapper.loadSingleService(PermissionGranter::class.java) {
                PermissionRequester()
            }
            return SdkAwareGrantPermissionRule(granter, *permissions)
        }
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "rule/SdkAwareGrantPermissionRule.kt",
            content = content,
            errorMessage = "sdk aware grant permission rule"
        )
    }

    private fun generateWebServerRule(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.test.annotation.ServerRequestResponse
import $architecturePackageName.test.server.MockRequest
import $architecturePackageName.test.server.ResponseBinder
import $architecturePackageName.test.server.ResponseStore
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.rule

$imports
class WebServerRule(
    private val lazyMockDispatcher: Lazy<ResponseBinder>,
    private val lazyResponseStore: Lazy<ResponseStore>
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        WebServerStatement(lazyMockDispatcher, lazyResponseStore, base, description)
}

private class WebServerStatement(
    private val lazyMockDispatcher: Lazy<ResponseBinder>,
    private val lazyResponseStore: Lazy<ResponseStore>,
    private val base: Statement,
    private val description: Description
) : Statement() {
    val responseStore by lazy { lazyResponseStore.value }
    val mockDispatcher by lazy { lazyMockDispatcher.value }

    override fun evaluate() {
        mockDispatcher.testName = description.displayName
        val stubbedResponseKeys = bindRequestResponseFactories()
        base.evaluate()
        assertAllStubsUsed(stubbedResponseKeys)
        mockDispatcher.reset()
    }

    private fun bindRequestResponseFactories(): Set<MockRequest> {
        val requestResponses = description.requestResponseIds()
            .map { requestResponseId ->
                requireNotNull(
                    responseStore.responseFactories[requestResponseId]
                ) { "Request/Response ID ${'$'}requestResponseId not found." }
            }

        requestResponses.forEach { requestResponse ->
            mockDispatcher.bindResponse(requestResponse)
        }
        val stubbedResponseKeys = requestResponses
            .map { requestResponse -> requestResponse.request }
            .toSet()
        return stubbedResponseKeys
    }

    private fun assertAllStubsUsed(stubbedResponseKeys: Set<MockRequest>) {
        val usedResponseKeys = mockDispatcher.usedEndpoints.toSet()

        val unusedResponseKeys = stubbedResponseKeys - usedResponseKeys
        check(unusedResponseKeys.isEmpty()) {
            "${'$'}{unusedResponseKeys.size} unused stubbed URLs:\n[" +
                unusedResponseKeys.joinToString("]\n[") + "]"
        }
    }

    private fun Description.requestResponseIds() =
        annotations.filterIsInstance<ServerRequestResponse>()
            .flatMap { serverResponse -> serverResponse.requestResponseIds.toList() }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "rule/WebServerRule.kt",
            content = content,
            errorMessage = "web server rule"
        )
    }

    private fun generateLoggingSslSocketFactory(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.util.Log
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.server

$imports
class LoggingSslSocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket =
        delegate.createSocket(s, host, port, autoClose).apply { enableLogging(this) }

    override fun createSocket(host: String?, port: Int): Socket =
        delegate.createSocket(host, port).apply { enableLogging(this) }

    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket =
        delegate.createSocket(host, port, localHost, localPort).apply { enableLogging(this) }

    override fun createSocket(host: InetAddress?, port: Int): Socket =
        delegate.createSocket(host, port).apply { enableLogging(this) }

    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int
    ): Socket =
        delegate.createSocket(address, port, localAddress, localPort).apply { enableLogging(this) }

    private fun enableLogging(socket: Socket) {
        if (socket is SSLSocket) {
            socket.addHandshakeCompletedListener { event ->
                Log.d(
                    "SSL",
                    "Handshake completed with peerHost=${'$'}{event.socket.inetAddress.hostName}"
                )
            }
        }
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/LoggingSslSocketFactory.kt",
            content = content,
            errorMessage = "logging ssl socket factory"
        )
    }

    private fun generateMockDispatcher(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.util.Log
import $architecturePackageName.test.server.response.MockResponseFactory
import okhttp3.Headers
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.server

$imports
class MockDispatcher :
    Dispatcher(),
    ResponseBinder {
    override var testName: String = ""

    override val usedEndpoints: Set<MockRequest>
        field = mutableSetOf()

    private val responses = mutableMapOf<MockRequest, MockResponseFactory>()

    var webSocket: WebSocket? = null

    override var onWebSocketMessage: (String) -> Unit = {}

    override fun bindResponse(requestResponseFactory: MockRequestResponseFactory) {
        responses[requestResponseFactory.request] = requestResponseFactory.responseFactory
    }

    override fun reset() {
        responses.clear()
        usedEndpoints.clear()
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
        val endPoint = request.path!!.substringBefore("?")
        val matchingRequest = responses.entries.firstOrNull { requestResponse ->
            requestResponse.key.url == endPoint
        }?.also { requestResponse ->
            usedEndpoints.add(requestResponse.key)
        }
        val response = matchingRequest?.value?.mockResponse() ?: MockResponse(code = 404).also {
            Log.w(TAG, "${'$'}testName: ${'$'}{request.path} not stubbed!")
        }
        return if (response.upgradeToWebSocket) {
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        this@MockDispatcher.webSocket = webSocket
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        this@MockDispatcher.onWebSocketMessage(text)
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        this@MockDispatcher.webSocket = null
                    }
                }
            )
        } else {
            MockResponse().apply {
                headers = Headers.headersOf(*response.headers.toArray())
            }.setResponseCode(response.code)
                .setBody(response.body)
        }
    }

    private fun Collection<Pair<String, String>>.toArray(): Array<String> =
        flatMap { listOf(it.first, it.second) }.toTypedArray()

    companion object {
        const val TAG = "MockDispatcher"
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/MockDispatcher.kt",
            content = content,
            errorMessage = "mock dispatcher"
        )
    }

    private fun generateMockRequest(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/MockRequest.kt",
            content =
                """package $architecturePackageName.test.server

data class MockRequest(val url: String)
""",
            errorMessage = "mock request"
        )
    }

    private fun generateMockRequestResponseFactory(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.test.server.response.MockResponseFactory
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.server

$imports
data class MockRequestResponseFactory(
    val request: MockRequest,
    val responseFactory: MockResponseFactory
)
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/MockRequestResponseFactory.kt",
            content = content,
            errorMessage = "mock request response factory"
        )
    }

    private fun generateMockResponse(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/MockResponse.kt",
            content =
                """package $architecturePackageName.test.server

data class MockResponse(
    val upgradeToWebSocket: Boolean = false,
    val code: Int = 200,
    val headers: List<Pair<String, String>> = emptyList(),
    val body: String = ""
)
""",
            errorMessage = "mock response"
        )
    }

    private fun generateMockWebServerProvider(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.test.asset.processAssetStream
import java.lang.Thread.MAX_PRIORITY
import java.security.KeyStore
import java.util.logging.Level
import java.util.logging.Logger
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.concurrent.thread
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockWebServer
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.server

$imports
/**
 * To generate a new keystore and extract the certificate, follow the steps below:
 * 1. Download BouncyCastle from:
 *
 *    https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.69/
 *
 * 2. Run the below commands on your terminal:
 *
 *   > keytool -genkey -v -alias localhost -ext SAN=dns:localhost -keypass 123456 -storepass 123456
 *       -keyalg RSA -keysize 2048 -validity 10000 -storetype BKS -keystore teststore_keystore.bks
 *       -provider org.bouncycastle.jce.provider.BouncyCastleProvider
 *       -providerpath ~/Downloads/bcprov-jdk15on-1.69.jar
 *   > keytool -exportcert -alias localhost -keystore teststore_keystore.bks -file teststore.crt
 *       -storetype BKS -storepass 123456
 *       -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath
 *   > openssl x509 -inform der -in teststore.crt -out teststore.pem
 *
 * 3. Place teststore_keystore.bks in the assets folder of the app being tested.
 *
 * 4. Place teststore.pem in the raw folder of the app and add it to xml/network_security_config.xml
 *    of the app being tested.
 */
class MockWebServerProvider {
    private val algorithm by lazy {
        KeyManagerFactory.getDefaultAlgorithm()
    }

    private val server by lazy {
        val mockWebServer = MockWebServer()
        val thread = thread(priority = MAX_PRIORITY) {
            mockWebServer.start()
        }
        thread.join()
        Logger.getLogger(MockWebServer::class.java.name).level = Level.ALL

        val keyStorePassword = "123456".toCharArray()
        val serverKeyStore = KeyStore.getInstance("BKS")
        processAssetStream("teststore_keystore.bks") { keyStoreStream ->
            serverKeyStore.load(keyStoreStream, keyStorePassword)
        }

        val keyManagerFactory = KeyManagerFactory.getInstance(algorithm)
            .apply { init(serverKeyStore, keyStorePassword) }

        val trustManagerFactory = TrustManagerFactory.getInstance(algorithm)
            .apply { init(serverKeyStore) }

        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(
            keyManagerFactory.keyManagers,
            trustManagerFactory.trustManagers,
            null
        )
        val socketFactory = sslContext.socketFactory

        mockWebServer.useHttps(socketFactory, false)

        mockWebServer
    }

    val serverUrl: String
        get() {
            var result = ""
            val thread = thread(priority = MAX_PRIORITY) {
                result = server.hostName + ":" + server.port
            }
            thread.join()
            return result
        }

    fun mockWebServer(dispatcher: Dispatcher): MockWebServer {
        server.dispatcher = dispatcher

        return server
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/MockWebServerProvider.kt",
            content = content,
            errorMessage = "mock web server provider"
        )
    }

    private fun generateErrorResponseFactory(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.test.server.MockResponse
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.server.response

$imports
sealed class ErrorResponseFactory {
    object NotFound : MockResponseFactory {
        override fun mockResponse() = MockResponse(code = 404)
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/response/ErrorResponseFactory.kt",
            content = content,
            errorMessage = "error response factory"
        )
    }

    private fun generateMockResponseFactory(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.test.server.MockResponse
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.server.response

$imports
interface MockResponseFactory {
    fun mockResponse(): MockResponse
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/response/MockResponseFactory.kt",
            content = content,
            errorMessage = "mock response factory"
        )
    }

    private fun generateSequenceResponseFactory(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.test.server.MockResponse
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.server.response

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/response/SequenceResponseFactory.kt",
            content = content,
            errorMessage = "sequence response factory"
        )
    }

    private fun generateSimpleResponseFactory(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.test.asset.getAssetAsString
import $architecturePackageName.test.server.MockResponse
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.server.response

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/response/SimpleResponseFactory.kt",
            content = content,
            errorMessage = "simple response factory"
        )
    }

    private fun generateResponseBinder(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val content =
            """package $architecturePackageName.test.server

interface ResponseBinder {
    var testName: String

    fun bindResponse(requestResponseFactory: MockRequestResponseFactory)

    val usedEndpoints: Set<MockRequest>

    fun reset()

    var onWebSocketMessage: (String) -> Unit
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/ResponseBinder.kt",
            content = content,
            errorMessage = "response binder"
        )
    }

    private fun generateResponseStore(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val content =
            """package $architecturePackageName.test.server

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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "server/ResponseStore.kt",
            content = content,
            errorMessage = "response store"
        )
    }

    private fun generateDoesNot(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "test/DoesNot.kt",
            content =
                """package $architecturePackageName.test.test

import junit.framework.AssertionFailedError

fun doesNot(description: String, block: () -> Unit) {
    try {
        block()
        error("Unexpected: ${'$'}description")
    } catch (_: AssertionFailedError) {
    }
}
""",
            errorMessage = "does not"
        )
    }

    private fun generateRetry(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import junit.framework.AssertionFailedError
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.test

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "test/Retry.kt",
            content = content,
            errorMessage = "retry"
        )
    }

    private fun generateBaseViewModelTest(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.domain.UseCaseExecutor
import $architecturePackageName.domain.exception.DomainException
import $architecturePackageName.domain.usecase.UseCase
import $architecturePackageName.presentation.notification.PresentationNotification
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
""".optimizeImports()

        val content =
            """package $architecturePackageName.presentation.viewmodel

$imports
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

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "viewmodel/BaseViewModelTest.kt",
            content = content,
            errorMessage = "base view model test"
        )
    }
}
