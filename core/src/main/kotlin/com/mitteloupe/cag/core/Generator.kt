package com.mitteloupe.cag.core

import com.mitteloupe.cag.core.content.buildDataGradleScript
import com.mitteloupe.cag.core.content.buildDataSourceImplementationGradleScript
import com.mitteloupe.cag.core.content.buildDataSourceSourceGradleScript
import com.mitteloupe.cag.core.content.buildDomainGradleScript
import com.mitteloupe.cag.core.content.buildPresentationGradleScript
import com.mitteloupe.cag.core.content.buildUiGradleScript
import com.mitteloupe.cag.core.generation.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.AppModuleGradleUpdater
import com.mitteloupe.cag.core.generation.BuildSrcContentCreator
import com.mitteloupe.cag.core.generation.ConfigurationFileCreator
import com.mitteloupe.cag.core.generation.DataLayerContentGenerator
import com.mitteloupe.cag.core.generation.DataSourceImplementationCreator
import com.mitteloupe.cag.core.generation.DataSourceInterfaceCreator
import com.mitteloupe.cag.core.generation.DataSourceModuleCreator
import com.mitteloupe.cag.core.generation.DomainLayerContentGenerator
import com.mitteloupe.cag.core.generation.GradleFileCreator
import com.mitteloupe.cag.core.generation.GradlePropertiesFileCreator
import com.mitteloupe.cag.core.generation.GradleWrapperCreator
import com.mitteloupe.cag.core.generation.PresentationLayerContentGenerator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.UiLayerContentGenerator
import com.mitteloupe.cag.core.generation.architecture.ArchitectureModulesContentGenerator
import com.mitteloupe.cag.core.generation.architecture.CoroutineModuleContentGenerator
import com.mitteloupe.cag.core.generation.versioncatalog.DependencyConfiguration
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.generation.withoutSpaces
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import com.mitteloupe.cag.core.request.GenerateArchitectureRequest
import com.mitteloupe.cag.core.request.GenerateFeatureRequest
import com.mitteloupe.cag.core.request.GenerateProjectTemplateRequest
import com.mitteloupe.cag.core.request.GenerateUseCaseRequest
import com.mitteloupe.cag.core.request.GenerateViewModelRequest
import java.io.File

class Generator(
    private val gradleFileCreator: GradleFileCreator,
    private val gradleWrapperCreator: GradleWrapperCreator,
    private val appModuleContentGenerator: AppModuleContentGenerator,
    private val buildSrcContentCreator: BuildSrcContentCreator,
    private val configurationFileCreator: ConfigurationFileCreator,
    private val uiLayerContentGenerator: UiLayerContentGenerator,
    private val presentationLayerContentGenerator: PresentationLayerContentGenerator,
    private val domainLayerContentGenerator: DomainLayerContentGenerator,
    private val dataLayerContentGenerator: DataLayerContentGenerator,
    private val dataSourceModuleCreator: DataSourceModuleCreator,
    private val dataSourceInterfaceCreator: DataSourceInterfaceCreator,
    private val dataSourceImplementationCreator: DataSourceImplementationCreator,
    private val gradlePropertiesFileCreator: GradlePropertiesFileCreator,
    private val architectureModulesContentGenerator: ArchitectureModulesContentGenerator,
    private val coroutineModuleContentGenerator: CoroutineModuleContentGenerator,
    private val catalogUpdater: VersionCatalogUpdater,
    private val settingsFileUpdater: SettingsFileUpdater
) {
    fun generateFeature(request: GenerateFeatureRequest) {
        val featurePackageName = request.featurePackageName?.trim()
        if (featurePackageName.isNullOrEmpty()) {
            throw GenerationException("Feature package name is missing.")
        }

        val pathSegments = featurePackageName.toSegments()
        if (pathSegments.isEmpty()) {
            throw GenerationException("Feature package name is invalid.")
        }

        val featureNameLowerCase = request.featureName.lowercase()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries =
                    if (request.enableCompose) {
                        LibraryConstants.COMPOSE_LIBRARIES
                    } else {
                        emptyList()
                    },
                plugins =
                    buildList {
                        addAll(PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS)
                        if (request.enableCompose) {
                            add(PluginConstants.COMPOSE_COMPILER)
                        }
                        if (request.enableKtlint) {
                            add(PluginConstants.KTLINT)
                        }
                        if (request.enableDetekt) {
                            add(PluginConstants.DETEKT)
                        }
                    }
            )
        catalogUpdater.createOrUpdateVersionCatalog(
            projectRootDir = request.destinationRootDirectory,
            dependencyConfiguration = dependencyConfiguration
        )
        val featureRoot = File(request.destinationRootDirectory, "features/$featureNameLowerCase")

        if (featureRoot.exists()) {
            throw GenerationException(
                if (featureRoot.isDirectory) {
                    "The feature directory already exists."
                } else {
                    "A file with the feature name exists where the feature directory should be created."
                }
            )
        }

        val layers = listOf("ui", "presentation", "domain", "data")

        val allCreated =
            layers
                .map { layerName ->
                    val layerSourceRoot = File(featureRoot, "$layerName/src/main/java")
                    val destinationDirectory = buildPackageDirectory(layerSourceRoot, pathSegments)
                    if (destinationDirectory.exists()) {
                        destinationDirectory.isDirectory
                    } else {
                        destinationDirectory.mkdirs()
                    }
                }.all { it }

        if (allCreated) {
            gradleFileCreator.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "domain",
                content = buildDomainGradleScript(catalogUpdater)
            )
            domainLayerContentGenerator
                .generateDomainLayer(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName
                )
            gradleFileCreator.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "presentation",
                content = buildPresentationGradleScript(featureNameLowerCase, catalogUpdater)
            )
            presentationLayerContentGenerator
                .generatePresentationLayer(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )
            gradleFileCreator.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "data",
                content = buildDataGradleScript(featureNameLowerCase, catalogUpdater)
            )
            dataLayerContentGenerator
                .generate(
                    featureRoot = featureRoot,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )
            gradleFileCreator.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "ui",
                content =
                    buildUiGradleScript(
                        featurePackageName = featurePackageName,
                        featureNameLowerCase = featureNameLowerCase,
                        enableCompose = request.enableCompose,
                        catalog = catalogUpdater
                    )
            )
            uiLayerContentGenerator
                .generate(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )
            if (request.enableDetekt) {
                configurationFileCreator.writeDetektConfigurationFile(request.destinationRootDirectory)
            }
            if (request.enableKtlint) {
                configurationFileCreator.writeEditorConfigFile(request.destinationRootDirectory)
            }
            settingsFileUpdater.updateProjectSettingsIfPresent(
                request.destinationRootDirectory,
                featureNameLowerCase
            )
            appModuleContentGenerator.writeFeatureModuleIfPossible(
                startDirectory = request.destinationRootDirectory,
                projectNamespace = request.projectNamespace,
                featureName = request.featureName,
                featurePackageName = featurePackageName,
                appModuleDirectory = request.appModuleDirectory
            )
            AppModuleGradleUpdater().updateAppModuleDependenciesIfPresent(
                startDirectory = request.destinationRootDirectory,
                featureNameLowerCase = featureNameLowerCase,
                appModuleDirectory = request.appModuleDirectory
            )
        }

        if (!allCreated) {
            throw GenerationException("Failed to create directories for package '$featurePackageName'.")
        }
    }

    fun generateUseCase(request: GenerateUseCaseRequest) {
        val destinationDirectory = request.destinationDirectory
        val useCaseName = request.useCaseName.trim()

        domainLayerContentGenerator
            .generateUseCase(
                destinationDirectory = destinationDirectory,
                useCaseName = useCaseName,
                inputDataType = request.inputDataType,
                outputDataType = request.outputDataType
            )
    }

    fun generateViewModel(request: GenerateViewModelRequest) {
        val destinationDirectory = request.destinationDirectory
        val viewModelName = request.viewModelName.trim()
        val featureName = viewModelName.removeSuffix("ViewModel")

        presentationLayerContentGenerator
            .generateViewState(
                destinationDirectory = File(destinationDirectory.parentFile, "model"),
                featurePackageName = request.featurePackageName,
                featureName = featureName
            )

        presentationLayerContentGenerator
            .generateViewModel(
                destinationDirectory = destinationDirectory,
                viewModelName = viewModelName,
                viewModelPackageName = request.viewModelPackageName,
                featurePackageName = request.featurePackageName,
                projectNamespace = request.projectNamespace
            )
    }

    private fun generateDataSourceModules(
        destinationRootDirectory: File,
        useKtor: Boolean,
        useRetrofit: Boolean
    ) {
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        val modules = listOf("source", "implementation")

        val allCreated =
            modules.all { moduleName ->
                val moduleDirectory = File(datasourceRoot, moduleName)
                if (moduleDirectory.exists()) {
                    moduleDirectory.isDirectory
                } else {
                    moduleDirectory.mkdirs()
                }
            }

        if (!allCreated) {
            throw GenerationException("Failed to create directories for datasource.")
        }

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )
        catalogUpdater.createOrUpdateVersionCatalog(
            projectRootDir = destinationRootDirectory,
            dependencyConfiguration = dependencyConfiguration
        )

        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = datasourceRoot,
            layer = "source",
            content = buildDataSourceSourceGradleScript(catalogUpdater)
        )

        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = datasourceRoot,
            layer = "implementation",
            content =
                buildDataSourceImplementationGradleScript(
                    catalog = catalogUpdater,
                    useKtor = useKtor,
                    useRetrofit = useRetrofit
                )
        )

        settingsFileUpdater.updateDataSourceSettingsIfPresent(destinationRootDirectory)
    }

    fun generateDataSource(
        destinationRootDirectory: File,
        dataSourceName: String,
        projectNamespace: String,
        useKtor: Boolean = false,
        useRetrofit: Boolean = false
    ) {
        generateDataSourceModules(destinationRootDirectory, useKtor, useRetrofit)

        dataSourceInterfaceCreator
            .writeDataSourceInterface(
                destinationRootDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                dataSourceName = dataSourceName
            )

        dataSourceImplementationCreator
            .writeDataSourceImplementation(
                destinationRootDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                dataSourceName = dataSourceName
            )

        dataSourceModuleCreator
            .writeDataSourceModule(
                destinationRootDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                dataSourceName = dataSourceName
            )
    }

    fun generateArchitecture(request: GenerateArchitectureRequest) {
        val architecturePackageName = request.architecturePackageName.trim()
        if (architecturePackageName.isEmpty()) {
            throw GenerationException("Architecture package name is missing.")
        }

        val pathSegments = architecturePackageName.toSegments()
        if (pathSegments.isEmpty()) {
            throw GenerationException("Architecture package name is invalid.")
        }

        val architectureRoot = File(request.destinationRootDirectory, "architecture")

        if (architectureRoot.exists()) {
            throw GenerationException(
                if (architectureRoot.isDirectory) {
                    "The architecture directory already exists."
                } else {
                    "A file with the architecture name exists where the architecture directory should be created."
                }
            )
        }

        if (!architectureRoot.mkdirs()) {
            throw GenerationException("Failed to create architecture directory.")
        }

        coroutineModuleContentGenerator
            .generate(
                projectRoot = request.destinationRootDirectory,
                coroutinePackageName = architecturePackageName.replaceAfterLast(".", "coroutine")
            )

        architectureModulesContentGenerator
            .generate(
                architectureRoot = architectureRoot,
                architecturePackageName = architecturePackageName,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt
            )

        settingsFileUpdater.updateArchitectureSettingsIfPresent(
            request.destinationRootDirectory
        )

        buildSrcContentCreator.writeGradleFile(request.destinationRootDirectory)
        buildSrcContentCreator.writeSettingsGradleFile(request.destinationRootDirectory)
        buildSrcContentCreator.writeProjectJavaLibraryFile(request.destinationRootDirectory)

        if (request.enableDetekt) {
            configurationFileCreator.writeDetektConfigurationFile(request.destinationRootDirectory)
        }
        if (request.enableKtlint) {
            configurationFileCreator.writeEditorConfigFile(request.destinationRootDirectory)
        }
    }

    fun generateProjectTemplate(request: GenerateProjectTemplateRequest) {
        val projectName = request.projectName.trim()
        if (projectName.isEmpty()) {
            throw GenerationException("Project name is missing.")
        }

        val packageName = request.packageName.trim()
        if (packageName.isEmpty()) {
            throw GenerationException("Package name is missing.")
        }

        val sanitizedProjectName = projectName.withoutSpaces()
        println("Comparing ${request.destinationRootDirectory.name} to $sanitizedProjectName")
        val projectRoot =
            if (request.destinationRootDirectory.name.matches("$sanitizedProjectName\\d*".toRegex())) {
                request.destinationRootDirectory
            } else {
                File(request.destinationRootDirectory, sanitizedProjectName)
            }

        if (projectRoot != request.destinationRootDirectory && projectRoot.exists()) {
            throw GenerationException(
                if (projectRoot.isDirectory) {
                    "The project directory already exists."
                } else {
                    "A file with the project name exists where the project directory should be created."
                }
            )
        }

        if (projectRoot != request.destinationRootDirectory && !projectRoot.mkdirs()) {
            throw GenerationException("Failed to create project directory.")
        }

        val libraries =
            LibraryConstants.CORE_ANDROID_LIBRARIES + LibraryConstants.TESTING_LIBRARIES +
                if (request.enableCompose) {
                    LibraryConstants.COMPOSE_LIBRARIES
                } else {
                    LibraryConstants.VIEW_LIBRARIES
                }
        val plugins =
            buildList {
                addAll(PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS + PluginConstants.HILT_ANDROID)
                if (request.enableCompose) {
                    add(PluginConstants.COMPOSE_COMPILER)
                }
                if (request.enableKtlint) {
                    add(PluginConstants.KTLINT)
                }
                if (request.enableDetekt) {
                    add(PluginConstants.DETEKT)
                }
            }
        val versionOverrides =
            listOf(
                VersionCatalogConstants.MIN_SDK_VERSION to request.overrideMinimumAndroidSdk?.toString(),
                VersionCatalogConstants.ANDROID_GRADLE_PLUGIN_VERSION to request.overrideAndroidGradlePluginVersion
            ).mapNotNull { override -> override.second?.let { override.first to it } }
                .toMap()
        val androidVersions =
            VersionCatalogConstants.ANDROID_VERSIONS.map { version ->
                versionOverrides[version]?.let { versionOverride ->
                    version.copy(version = versionOverride)
                } ?: version
            }
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = androidVersions,
                libraries = libraries,
                plugins = plugins
            )
        catalogUpdater.createOrReplaceVersionCatalog(
            projectRootDir = projectRoot,
            dependencyConfiguration = dependencyConfiguration
        )

        generateProjectStructure(projectRoot)
        generateArchitectureModules(projectRoot, "$packageName.architecture", request)
        generateDataSourceModules(projectRoot, request)
        generateSampleFeature(
            projectRoot = projectRoot,
            featureName = "SampleFeature",
            packageName = packageName,
            request = request
        )
        generateAppModule(projectRoot = projectRoot, appName = projectName, packageName = packageName, request = request)
        generateTemplateProjectGradleFiles(projectRoot, packageName, request)
        settingsFileUpdater.writeProjectSettings(
            projectRoot = projectRoot,
            projectName = projectName,
            featureNames = listOf("SampleFeature")
        )
    }

    private fun generateProjectStructure(projectRoot: File) {
        val directories =
            listOf(
                "app/src/main/java",
                "app/src/main/res/layout",
                "app/src/main/res/values",
                "app/src/main/res/drawable",
                "app/src/main/res/mipmap-anydpi-v26",
                "app/src/main/res/mipmap-hdpi",
                "app/src/main/res/mipmap-mdpi",
                "app/src/main/res/mipmap-xhdpi",
                "app/src/main/res/mipmap-xxhdpi",
                "app/src/main/res/mipmap-xxxhdpi",
                "app/src/test/java",
                "app/src/androidTest/java",
                "buildSrc/src/main/kotlin"
            )

        directories.forEach { directory ->
            val dir = File(projectRoot, directory)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    }

    private fun generateArchitectureModules(
        projectRoot: File,
        architecturePackageName: String,
        request: GenerateProjectTemplateRequest
    ) {
        val architectureRequest =
            GenerateArchitectureRequest(
                destinationRootDirectory = projectRoot,
                architecturePackageName = architecturePackageName,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt
            )
        generateArchitecture(architectureRequest)
    }

    private fun generateSampleFeature(
        projectRoot: File,
        featureName: String,
        packageName: String,
        request: GenerateProjectTemplateRequest
    ) {
        val featureRequest =
            GenerateFeatureRequest(
                destinationRootDirectory = projectRoot,
                featureName = featureName,
                featurePackageName = "$packageName.${featureName.lowercase()}",
                projectNamespace = packageName,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt,
                appModuleDirectory = null
            )
        generateFeature(featureRequest)
    }

    private fun generateDataSourceModules(
        projectRoot: File,
        request: GenerateProjectTemplateRequest
    ) {
        generateDataSourceModules(projectRoot, request.enableKtor, request.enableRetrofit)
    }

    private fun generateAppModule(
        projectRoot: File,
        appName: String,
        packageName: String,
        request: GenerateProjectTemplateRequest
    ) {
        appModuleContentGenerator.writeAppModule(
            startDirectory = projectRoot,
            appName = appName,
            projectNamespace = packageName,
            enableCompose = request.enableCompose
        )
    }

    private fun generateTemplateProjectGradleFiles(
        projectRoot: File,
        packageName: String,
        request: GenerateProjectTemplateRequest
    ) {
        gradleFileCreator.writeProjectGradleFile(
            projectRoot = projectRoot,
            enableKtlint = request.enableKtlint,
            enableDetekt = request.enableDetekt,
            catalog = catalogUpdater
        )

        gradleFileCreator.writeAppGradleFile(
            projectRoot = projectRoot,
            packageName = packageName,
            enableCompose = request.enableCompose,
            catalog = catalogUpdater
        )

        buildSrcContentCreator.writeGradleFile(projectRoot)
        buildSrcContentCreator.writeSettingsGradleFile(projectRoot)
        buildSrcContentCreator.writeProjectJavaLibraryFile(projectRoot)

        gradlePropertiesFileCreator.writeGradlePropertiesFile(projectRoot)
        gradleWrapperCreator.writeGradleWrapperFiles(projectRoot)

        if (request.enableDetekt) {
            configurationFileCreator.writeDetektConfigurationFile(projectRoot)
        }
        if (request.enableKtlint) {
            configurationFileCreator.writeEditorConfigFile(projectRoot)
        }
    }
}
