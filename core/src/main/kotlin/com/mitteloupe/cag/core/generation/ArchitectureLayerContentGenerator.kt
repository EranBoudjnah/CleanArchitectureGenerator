package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import com.mitteloupe.cag.core.content.buildArchitectureDomainGradleScript
import com.mitteloupe.cag.core.content.buildArchitecturePresentationGradleScript
import com.mitteloupe.cag.core.content.buildArchitectureUiGradleScript
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class ArchitectureLayerContentGenerator {
    fun generate(
        architectureRoot: File,
        architecturePackageName: String,
        enableCompose: Boolean
    ): String? {
        val packageSegments = architecturePackageName.toSegments()
        if (packageSegments.isEmpty()) {
            return "Error: Architecture package name is invalid."
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
            return "Error: Failed to create directories for architecture package '$architecturePackageName'."
        }

        val catalogUpdater = VersionCatalogUpdater()
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = architectureRoot.parentFile,
            enableCompose = enableCompose
        )?.let { return it }

        createDomainModule(architectureRoot, catalogUpdater)?.let { return it }
        createPresentationModule(architectureRoot, catalogUpdater)?.let { return it }
        createUiModule(architectureRoot, catalogUpdater)?.let { return it }

        generateDomainContent(architectureRoot, architecturePackageName, architecturePackageName)?.let { return it }
        generatePresentationContent(architectureRoot, architecturePackageName, architecturePackageName)?.let { return it }
        generateUiContent(architectureRoot, architecturePackageName, architecturePackageName)?.let { return it }

        return null
    }

    private fun createDomainModule(
        architectureRoot: File,
        catalog: VersionCatalogUpdater
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "domain",
            content = buildArchitectureDomainGradleScript(catalog)
        )

    private fun createPresentationModule(
        architectureRoot: File,
        catalog: VersionCatalogUpdater
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "presentation",
            content = buildArchitecturePresentationGradleScript(catalog)
        )

    private fun createUiModule(
        architectureRoot: File,
        catalog: VersionCatalogUpdater
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "ui",
            content = buildArchitectureUiGradleScript(catalog)
        )

    private fun generateDomainContent(
        architectureRoot: File,
        projectNamespace: String,
        architecturePackageName: String
    ): String? {
        val domainRoot = File(architectureRoot, "domain/src/main/java")
        val packageDirectory = buildPackageDirectory(domainRoot, architecturePackageName.toSegments())

        generateUseCaseBase(packageDirectory, projectNamespace)?.let { return it }
        generateRepositoryBase(packageDirectory, projectNamespace)?.let { return it }

        return null
    }

    private fun generatePresentationContent(
        architectureRoot: File,
        projectNamespace: String,
        architecturePackageName: String
    ): String? {
        val presentationRoot = File(architectureRoot, "presentation/src/main/java")
        val packageDirectory = buildPackageDirectory(presentationRoot, architecturePackageName.toSegments())

        generateViewModelBase(packageDirectory, projectNamespace)?.let { return it }
        generateNavigationEventBase(packageDirectory, projectNamespace)?.let { return it }

        return null
    }

    private fun generateUiContent(
        architectureRoot: File,
        projectNamespace: String,
        architecturePackageName: String
    ): String? {
        val uiRoot = File(architectureRoot, "ui/src/main/java")
        val packageDirectory = buildPackageDirectory(uiRoot, architecturePackageName.toSegments())

        generateScreenBase(packageDirectory, projectNamespace)?.let { return it }
        generateMapperBase(packageDirectory, projectNamespace)?.let { return it }

        return null
    }

    private fun generateUseCaseBase(
        packageDirectory: File,
        projectNamespace: String
    ): String? {
        val useCaseFile = File(packageDirectory, "usecase/UseCase.kt")
        if (useCaseFile.exists()) return null

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
            return "${ERROR_PREFIX}Failed to generate use case: ${it.message}"
        }

        return null
    }

    private fun generateRepositoryBase(
        packageDirectory: File,
        projectNamespace: String
    ): String? {
        val repositoryFile = File(packageDirectory, "repository/Repository.kt")
        if (repositoryFile.exists()) return null

        val content =
            """
            package $projectNamespace.domain.repository

            interface Repository
            """.trimIndent()

        runCatching {
            repositoryFile.parentFile.mkdirs()
            repositoryFile.writeText(content)
        }.onFailure {
            return "${ERROR_PREFIX}Failed to generate repository: ${it.message}"
        }

        return null
    }

    private fun generateViewModelBase(
        packageDirectory: File,
        projectNamespace: String
    ): String? {
        val viewModelFile = File(packageDirectory, "viewmodel/BaseViewModel.kt")
        if (viewModelFile.exists()) return null

        val content =
            """
            package $projectNamespace.presentation.viewmodel

            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.viewModelScope
            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.StateFlow
            import kotlinx.coroutines.flow.asStateFlow
            import kotlinx.coroutines.launch

            abstract class BaseViewModel<State, Event> : ViewModel() {
                private val _state = MutableStateFlow(createInitialState())
                val state: StateFlow<State> = _state.asStateFlow()

                protected abstract fun createInitialState(): State

                protected fun setState(reduce: State.() -> State) {
                    val newState = state.value.reduce()
                    _state.value = newState
                }

                protected fun launch(block: suspend () -> Unit) {
                    viewModelScope.launch { block() }
                }

                abstract fun onEvent(event: Event)
            }
            """.trimIndent()

        runCatching {
            viewModelFile.parentFile.mkdirs()
            viewModelFile.writeText(content)
        }.onFailure {
            return "${ERROR_PREFIX}Failed to generate view model: ${it.message}"
        }

        return null
    }

    private fun generateNavigationEventBase(
        packageDirectory: File,
        projectNamespace: String
    ): String? {
        val navigationFile = File(packageDirectory, "navigation/PresentationNavigationEvent.kt")
        if (navigationFile.exists()) return null

        val content =
            """
            package $projectNamespace.presentation.navigation

            sealed class PresentationNavigationEvent
            """.trimIndent()

        runCatching {
            navigationFile.parentFile.mkdirs()
            navigationFile.writeText(content)
        }.onFailure {
            return "${ERROR_PREFIX}Failed to generate navigation event: ${it.message}"
        }

        return null
    }

    private fun generateScreenBase(
        packageDirectory: File,
        projectNamespace: String
    ): String? {
        val screenFile = File(packageDirectory, "view/Screen.kt")
        if (screenFile.exists()) return null

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
            return "${ERROR_PREFIX}Failed to generate base screen: ${it.message}"
        }

        return null
    }

    private fun generateMapperBase(
        packageDirectory: File,
        projectNamespace: String
    ): String? {
        val mapperFile = File(packageDirectory, "mapper/Mapper.kt")
        if (mapperFile.exists()) return null

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
            return "${ERROR_PREFIX}Failed to generate base mapper: ${it.message}"
        }

        return null
    }
}
