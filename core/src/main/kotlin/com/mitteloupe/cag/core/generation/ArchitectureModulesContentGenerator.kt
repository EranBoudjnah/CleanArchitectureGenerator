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
        createUiModule(architectureRoot, catalogUpdater)

        generateDomainContent(architectureRoot, architecturePackageName, architecturePackageName)
        generatePresentationContent(
            architectureRoot,
            architecturePackageName,
            architecturePackageName
        )
        generateUiContent(architectureRoot, architecturePackageName, architecturePackageName)
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
        catalog: VersionCatalogUpdater
    ) {
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "ui",
            content = buildArchitectureUiGradleScript(catalog)
        )
    }

    private fun generateDomainContent(
        architectureRoot: File,
        moduleNamespace: String,
        architecturePackageName: String
    ) {
        val domainRoot = File(architectureRoot, "domain/src/main/java")
        val packageDirectory = buildPackageDirectory(domainRoot, architecturePackageName.toSegments())

        generateUseCase(packageDirectory, moduleNamespace)
        generateBackgroundExecutingUseCase(packageDirectory, moduleNamespace)
        generateContinuousExecutingUseCase(packageDirectory, moduleNamespace)
        generateRepositoryBase(packageDirectory, moduleNamespace)
        generateDomainException(packageDirectory, moduleNamespace)
        generateUnknownDomainException(packageDirectory, moduleNamespace)
        generateUseCaseExecutor(packageDirectory, moduleNamespace)
        generateUseCaseExecutorProvider(packageDirectory, moduleNamespace)
    }

    private fun generatePresentationContent(
        architectureRoot: File,
        moduleNamespace: String,
        architecturePackageName: String
    ) {
        val presentationRoot = File(architectureRoot, "presentation/src/main/java")
        val packageDirectory = buildPackageDirectory(presentationRoot, "$architecturePackageName.presentation".toSegments())

        generateViewModelBase(packageDirectory, moduleNamespace)
        generateNavigationEventBase(packageDirectory, moduleNamespace)
    }

    private fun generateUiContent(
        architectureRoot: File,
        moduleNamespace: String,
        architecturePackageName: String
    ) {
        val uiRoot = File(architectureRoot, "ui/src/main/java")
        val packageDirectory = buildPackageDirectory(uiRoot, architecturePackageName.toSegments())

        generateScreenBase(packageDirectory, moduleNamespace)
        generateMapperBase(packageDirectory, moduleNamespace)
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
import $moduleNamespace.coroutine.CoroutineContextProvider
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
}"""

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
import $moduleNamespace.coroutine.CoroutineContextProvider
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
}"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "usecase/ContinuousExecutingUseCase.kt",
            content = content,
            errorMessage = "continuous executing use case"
        )
    }

    private fun generateRepositoryBase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "repository/Repository.kt",
            content =
                """
                package $moduleNamespace.domain.repository

                interface Repository
                """.trimIndent(),
            errorMessage = "repository"
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
}"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "viewmodel/BaseViewModel.kt",
            content = content,
            errorMessage = "view model"
        )
    }

    private fun generateNavigationEventBase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "navigation/PresentationNavigationEvent.kt",
            content =
                """
                package $moduleNamespace.presentation.navigation

                sealed class PresentationNavigationEvent
                """.trimIndent(),
            errorMessage = "navigation event"
        )
    }

    private fun generateScreenBase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "view/Screen.kt",
            content =
                """package $moduleNamespace.ui.view

${
                    """
import androidx.compose.runtime.Composable
""".optimizeImports()
                }
interface Screen {
    @Composable
    fun Content()
}""",
            errorMessage = "base screen"
        )
    }

    private fun generateMapperBase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "mapper/Mapper.kt",
            content =
                """
                package $moduleNamespace.ui.mapper

                interface Mapper<Input, Output> {
                    fun map(input: Input): Output
                }
                """.trimIndent(),
            errorMessage = "base mapper"
        )
    }

    private fun generateFileIfMissing(
        packageDirectory: File,
        relativePath: String,
        content: String,
        errorMessage: String
    ) {
        val file = File(packageDirectory, relativePath)
        if (file.exists()) {
            return
        }

        runCatching {
            file.parentFile.mkdirs()
            file.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate $errorMessage: ${it.message}")
        }
    }
}
