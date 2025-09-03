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
        generateContinuousExecutingUseCase(packageDirectory, moduleNamespace)
        generateRepositoryBase(packageDirectory, moduleNamespace)
        generateDomainException(packageDirectory, moduleNamespace)
        generateUnknownDomainException(packageDirectory, moduleNamespace)
        generateUseCaseExecutor(packageDirectory, moduleNamespace)
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
        val useCaseFile = File(packageDirectory, "usecase/UseCase.kt")
        if (useCaseFile.exists()) {
            return
        }

        val content =
            """
            package $moduleNamespace.domain.usecase

            interface UseCase<REQUEST, RESULT> {
                fun execute(input: REQUEST, onResult: (RESULT) -> Unit)
            }
            """.trimIndent()

        runCatching {
            useCaseFile.parentFile.mkdirs()
            useCaseFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate use case: ${it.message}")
        }
    }

    private fun generateContinuousExecutingUseCase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val useCaseFile = File(packageDirectory, "usecase/ContinuousExecutingUseCase.kt")
        if (useCaseFile.exists()) {
            return
        }

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

        runCatching {
            useCaseFile.parentFile.mkdirs()
            useCaseFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate continuous executing use case: ${it.message}")
        }
    }

    private fun generateRepositoryBase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val repositoryFile = File(packageDirectory, "repository/Repository.kt")
        if (repositoryFile.exists()) {
            return
        }

        val content =
            """
            package $moduleNamespace.domain.repository

            interface Repository
            """.trimIndent()

        runCatching {
            repositoryFile.parentFile.mkdirs()
            repositoryFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate repository: ${it.message}")
        }
    }

    private fun generateUseCaseExecutor(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val useCaseExecutorFile = File(packageDirectory, "UseCaseExecutor.kt")
        if (useCaseExecutorFile.exists()) {
            return
        }

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

        runCatching {
            useCaseExecutorFile.parentFile.mkdirs()
            useCaseExecutorFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate use case executor: ${it.message}")
        }
    }

    private fun generateDomainException(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val domainExceptionFile = File(packageDirectory, "exception/DomainException.kt")
        if (domainExceptionFile.exists()) {
            return
        }

        val content =
            """
            package $moduleNamespace.domain.exception

            abstract class DomainException(cause: Throwable? = null) : Exception(cause)
            """.trimIndent()

        runCatching {
            domainExceptionFile.parentFile.mkdirs()
            domainExceptionFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate domain exception: ${it.message}")
        }
    }

    private fun generateUnknownDomainException(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val domainExceptionFile = File(packageDirectory, "exception/UnknownDomainException.kt")
        if (domainExceptionFile.exists()) {
            return
        }

        val content =
            """
            package $moduleNamespace.domain.exception

            class UnknownDomainException(cause: Throwable? = null) : DomainException(cause)
            """.trimIndent()

        runCatching {
            domainExceptionFile.parentFile.mkdirs()
            domainExceptionFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate unknown domain exception: ${it.message}")
        }
    }

    private fun generateViewModelBase(
        packageDirectory: File,
        architecturePackage: String
    ) {
        val viewModelFile = File(packageDirectory, "viewmodel/BaseViewModel.kt")
        if (viewModelFile.exists()) {
            return
        }

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

        runCatching {
            viewModelFile.parentFile.mkdirs()
            viewModelFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate view model: ${it.message}")
        }
    }

    private fun generateNavigationEventBase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val navigationFile = File(packageDirectory, "navigation/PresentationNavigationEvent.kt")
        if (navigationFile.exists()) {
            return
        }

        val content =
            """
            package $moduleNamespace.presentation.navigation

            sealed class PresentationNavigationEvent
            """.trimIndent()

        runCatching {
            navigationFile.parentFile.mkdirs()
            navigationFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate navigation event: ${it.message}")
        }
    }

    private fun generateScreenBase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val screenFile = File(packageDirectory, "view/Screen.kt")
        if (screenFile.exists()) {
            return
        }

        val content =
            """package $moduleNamespace.ui.view

${
                """
import androidx.compose.runtime.Composable
""".optimizeImports()
            }
interface Screen {
    @Composable
    fun Content()
}"""

        runCatching {
            screenFile.parentFile.mkdirs()
            screenFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate base screen: ${it.message}")
        }
    }

    private fun generateMapperBase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val mapperFile = File(packageDirectory, "mapper/Mapper.kt")
        if (mapperFile.exists()) {
            return
        }

        val content =
            """
            package $moduleNamespace.ui.mapper

            interface Mapper<Input, Output> {
                fun map(input: Input): Output
            }
            """.trimIndent()

        runCatching {
            mapperFile.parentFile.mkdirs()
            mapperFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate base mapper: ${it.message}")
        }
    }
}
