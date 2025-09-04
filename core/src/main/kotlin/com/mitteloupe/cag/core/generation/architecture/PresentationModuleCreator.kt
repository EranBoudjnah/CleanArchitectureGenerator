package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.generation.generateFileIfMissing
import com.mitteloupe.cag.core.generation.optimizeImports
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import java.io.File

class PresentationModuleCreator internal constructor() {
    fun generatePresentationContent(
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
