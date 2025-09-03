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
        projectNamespace: String,
        architecturePackageName: String
    ) {
        val domainRoot = File(architectureRoot, "domain/src/main/java")
        val packageDirectory = buildPackageDirectory(domainRoot, architecturePackageName.toSegments())

        generateUseCaseBase(packageDirectory, projectNamespace)
        generateRepositoryBase(packageDirectory, projectNamespace)
    }

    private fun generatePresentationContent(
        architectureRoot: File,
        projectNamespace: String,
        architecturePackageName: String
    ) {
        val presentationRoot = File(architectureRoot, "presentation/src/main/java")
        val packageDirectory = buildPackageDirectory(presentationRoot, "$architecturePackageName.presentation".toSegments())

        generateViewModelBase(packageDirectory, projectNamespace)
        generateNavigationEventBase(packageDirectory, projectNamespace)
    }

    private fun generateUiContent(
        architectureRoot: File,
        projectNamespace: String,
        architecturePackageName: String
    ) {
        val uiRoot = File(architectureRoot, "ui/src/main/java")
        val packageDirectory = buildPackageDirectory(uiRoot, architecturePackageName.toSegments())

        generateScreenBase(packageDirectory, projectNamespace)
        generateMapperBase(packageDirectory, projectNamespace)
    }

    private fun generateUseCaseBase(
        packageDirectory: File,
        projectNamespace: String
    ) {
        val useCaseFile = File(packageDirectory, "usecase/UseCase.kt")
        if (useCaseFile.exists()) {
            return
        }

        val content =
            """
            package $projectNamespace.domain.usecase

            abstract class UseCase<in Input, Output> {
                suspend operator fun invoke(input: Input): Output
            }

            abstract class NoInputUseCase<Output> {
                suspend operator fun invoke(): Output
            }
            """.trimIndent()

        runCatching {
            useCaseFile.parentFile.mkdirs()
            useCaseFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate use case: ${it.message}")
        }
    }

    private fun generateRepositoryBase(
        packageDirectory: File,
        projectNamespace: String
    ) {
        val repositoryFile = File(packageDirectory, "repository/Repository.kt")
        if (repositoryFile.exists()) {
            return
        }

        val content =
            """
            package $projectNamespace.domain.repository

            interface Repository
            """.trimIndent()

        runCatching {
            repositoryFile.parentFile.mkdirs()
            repositoryFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate repository: ${it.message}")
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
            """
            package $architecturePackage.presentation.viewmodel

            import $architecturePackage.domain.UseCaseExecutor
            import $architecturePackage.domain.exception.DomainException
            import $architecturePackage.domain.usecase.UseCase
            import $architecturePackage.presentation.navigation.PresentationNavigationEvent
            import $architecturePackage.presentation.notification.PresentationNotification
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
            """.trimIndent()

        runCatching {
            viewModelFile.parentFile.mkdirs()
            viewModelFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate view model: ${it.message}")
        }
    }

    private fun generateNavigationEventBase(
        packageDirectory: File,
        projectNamespace: String
    ) {
        val navigationFile = File(packageDirectory, "navigation/PresentationNavigationEvent.kt")
        if (navigationFile.exists()) {
            return
        }

        val content =
            """
            package $projectNamespace.presentation.navigation

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
        projectNamespace: String
    ) {
        val screenFile = File(packageDirectory, "view/Screen.kt")
        if (screenFile.exists()) {
            return
        }

        val content =
            """
            package $projectNamespace.ui.view

            import androidx.compose.runtime.Composable

            interface Screen {
                @Composable
                fun Content()
            }
            """.trimIndent()

        runCatching {
            screenFile.parentFile.mkdirs()
            screenFile.writeText(content)
        }.onFailure {
            throw GenerationException("Failed to generate base screen: ${it.message}")
        }
    }

    private fun generateMapperBase(
        packageDirectory: File,
        projectNamespace: String
    ) {
        val mapperFile = File(packageDirectory, "mapper/Mapper.kt")
        if (mapperFile.exists()) {
            return
        }

        val content =
            """
            package $projectNamespace.ui.mapper

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
