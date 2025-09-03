package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildArchitectureDomainGradleScript
import com.mitteloupe.cag.core.content.buildArchitecturePresentationGradleScript
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

        val layers = listOf("domain", "presentation", "ui")
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

        val domainRoot = File(architectureRoot, "domain")
        generateDomainContent(domainRoot, architecturePackageName, packageSegments + "domain")
        val presentationRoot = File(architectureRoot, "presentation")
        generatePresentationContent(presentationRoot, architecturePackageName, packageSegments + "presentation")
        val uiRoot = File(architectureRoot, "ui")
        generateUiContent(uiRoot, architecturePackageName, packageSegments + "ui")
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
}
