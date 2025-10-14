package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.generation.format.optimizeImports
import com.mitteloupe.cag.core.generation.generateFileIfMissing
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import java.io.File

class UiModuleCreator internal constructor() {
    fun generateUiContent(
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
                $$"""package $$moduleNamespace.ui.navigation.exception

import $$moduleNamespace.presentation.navigation.PresentationNavigationEvent

class UnhandledNavigationException(event: PresentationNavigationEvent) :
    IllegalArgumentException(
        "Navigation event ${event::class.simpleName} was not handled."
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
}
