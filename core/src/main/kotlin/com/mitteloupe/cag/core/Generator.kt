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
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.generation.withoutSpaces
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File
import kotlin.String

class Generator {
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
        val catalogUpdater = VersionCatalogUpdater()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.BASIC_VERSIONS,
                libraries = if (request.enableCompose) LibraryConstants.COMPOSE_LIBRARIES else emptyList(),
                plugins =
                    PluginConstants.KOTLIN_PLUGINS +
                        PluginConstants.ANDROID_PLUGINS +
                        if (request.enableCompose) {
                            PluginConstants.COMPOSE_PLUGINS
                        } else {
                            emptyList()
                        }
            )
        catalogUpdater.updateVersionCatalogIfPresent(
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
            layers.map { layerName ->
                val layerSourceRoot = File(featureRoot, "$layerName/src/main/java")
                val destinationDirectory = buildPackageDirectory(layerSourceRoot, pathSegments)
                if (destinationDirectory.exists()) {
                    destinationDirectory.isDirectory
                } else {
                    destinationDirectory.mkdirs()
                }
            }.all { it }

        if (allCreated) {
            GradleFileCreator().writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "domain",
                content = buildDomainGradleScript(catalogUpdater)
            )
            DomainLayerContentGenerator()
                .generateDomainLayer(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName
                )
            GradleFileCreator().writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "presentation",
                content = buildPresentationGradleScript(featureNameLowerCase, catalogUpdater)
            )
            PresentationLayerContentGenerator()
                .generate(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )
            GradleFileCreator().writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "data",
                content = buildDataGradleScript(featureNameLowerCase, catalogUpdater)
            )
            DataLayerContentGenerator()
                .generate(
                    featureRoot = featureRoot,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )
            GradleFileCreator().writeGradleFileIfMissing(
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
            UiLayerContentGenerator()
                .generate(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )
            SettingsFileUpdater().updateProjectSettingsIfPresent(
                request.destinationRootDirectory,
                featureNameLowerCase
            )
            AppModuleContentGenerator().writeFeatureModuleIfPossible(
                startDirectory = request.destinationRootDirectory,
                projectNamespace = request.projectNamespace,
                featureName = request.featureName,
                featurePackageName = featurePackageName
            )
            AppModuleGradleUpdater().updateAppModuleDependenciesIfPresent(
                startDirectory = request.destinationRootDirectory,
                featureNameLowerCase = featureNameLowerCase
            )
        }

        if (!allCreated) {
            throw GenerationException("Failed to create directories for package '$featurePackageName'.")
        }
    }

    fun generateUseCase(request: GenerateUseCaseRequest) {
        val destinationDirectory = request.destinationDirectory
        val useCaseName = request.useCaseName.trim()

        DomainLayerContentGenerator()
            .generateUseCase(
                destinationDirectory = destinationDirectory,
                useCaseName = useCaseName,
                inputDataType = request.inputDataType,
                outputDataType = request.outputDataType
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

        val gradleFileCreator = GradleFileCreator()
        val catalogUpdater = VersionCatalogUpdater()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.BASIC_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )
        catalogUpdater.updateVersionCatalogIfPresent(
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

        SettingsFileUpdater().updateDataSourceSettingsIfPresent(destinationRootDirectory)
    }

    fun generateDataSource(
        destinationRootDirectory: File,
        dataSourceName: String,
        projectNamespace: String,
        useKtor: Boolean = false,
        useRetrofit: Boolean = false
    ) {
        generateDataSourceModules(destinationRootDirectory, useKtor, useRetrofit)

        DataSourceInterfaceCreator()
            .writeDataSourceInterface(
                destinationRootDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                dataSourceName = dataSourceName
            )

        DataSourceImplementationCreator()
            .writeDataSourceImplementation(
                destinationRootDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                dataSourceName = dataSourceName
            )

        DataSourceModuleCreator()
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

        CoroutineModuleContentGenerator()
            .generate(
                projectRoot = request.destinationRootDirectory,
                coroutinePackageName = architecturePackageName.replaceAfterLast(".", "coroutine")
            )

        ArchitectureModulesContentGenerator()
            .generate(
                architectureRoot = architectureRoot,
                architecturePackageName = architecturePackageName,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt
            )

        SettingsFileUpdater().updateArchitectureSettingsIfPresent(
            request.destinationRootDirectory
        )

        val buildSrcContentCreator = BuildSrcContentCreator()
        buildSrcContentCreator.writeGradleFile(request.destinationRootDirectory)
        buildSrcContentCreator.writeSettingsGradleFile(request.destinationRootDirectory)
        buildSrcContentCreator.writeProjectJavaLibraryFile(request.destinationRootDirectory)
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

        println("Comparing ${request.destinationRootDirectory.name} to ${projectName.withoutSpaces()}")
        val projectRoot =
            if (request.destinationRootDirectory.name == projectName.withoutSpaces()) {
                request.destinationRootDirectory
            } else {
                File(request.destinationRootDirectory, projectName.withoutSpaces())
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

        val catalogUpdater = VersionCatalogUpdater()
        val versions = VersionCatalogConstants.BASIC_VERSIONS + VersionCatalogConstants.ANDROID_VERSIONS
        val libraries =
            LibraryConstants.CORE_ANDROID_LIBRARIES +
                if (request.enableCompose) {
                    LibraryConstants.COMPOSE_LIBRARIES
                } else {
                    emptyList<SectionEntryRequirement.LibraryRequirement>() +
                        LibraryConstants.TESTING_LIBRARIES
                }
        val plugins =
            PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS +
                if (request.enableCompose) {
                    PluginConstants.COMPOSE_PLUGINS
                } else {
                    emptyList<SectionEntryRequirement.PluginRequirement>() +
                        if (request.enableKtlint) {
                            PluginConstants.CODE_QUALITY_PLUGINS.filter { it.id == "org.jlleitschuh.gradle.ktlint" }
                        } else {
                            emptyList<SectionEntryRequirement.PluginRequirement>() +
                                if (request.enableDetekt) {
                                    setOf(PluginConstants.DETEKT)
                                } else {
                                    emptySet()
                                }
                        }
                }
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = versions,
                libraries = libraries,
                plugins = plugins
            )
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = projectRoot,
            dependencyConfiguration = dependencyConfiguration
        )

        generateProjectStructure(projectRoot)
        SettingsFileUpdater().writeProjectSettings(
            projectRoot = projectRoot,
            projectName = projectName,
            featureNames = listOf("SampleFeature")
        )
        generateArchitectureModules(projectRoot, "$packageName.architecture", request)
        generateSampleFeature(
            projectRoot = projectRoot,
            featureName = "SampleFeature",
            packageName = packageName,
            request = request
        )
        generateDataSourceModules(projectRoot, request)
        generateAppModule(projectRoot = projectRoot, appName = projectName, packageName = packageName, request = request)
        generateGradleFiles(projectRoot, packageName, request)
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
                enableCompose = request.enableCompose
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
        val appModuleContentGenerator = AppModuleContentGenerator()
        appModuleContentGenerator.writeAppModule(
            startDirectory = projectRoot,
            appName = appName,
            projectNamespace = packageName,
            enableCompose = request.enableCompose
        )
    }

    private fun generateGradleFiles(
        projectRoot: File,
        packageName: String,
        request: GenerateProjectTemplateRequest
    ) {
        val gradleFileCreator = GradleFileCreator()
        val gradlePropertiesFileCreator = GradlePropertiesFileCreator()
        val catalogUpdater = VersionCatalogUpdater()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.BASIC_VERSIONS,
                libraries = if (request.enableCompose) LibraryConstants.COMPOSE_LIBRARIES else emptyList(),
                plugins =
                    PluginConstants.KOTLIN_PLUGINS +
                        PluginConstants.ANDROID_PLUGINS +
                        if (request.enableCompose) {
                            PluginConstants.COMPOSE_PLUGINS
                        } else {
                            emptyList()
                        }
            )
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = projectRoot,
            dependencyConfiguration = dependencyConfiguration
        )

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

        val buildSrcContentCreator = BuildSrcContentCreator()
        buildSrcContentCreator.writeGradleFile(projectRoot)
        buildSrcContentCreator.writeSettingsGradleFile(projectRoot)
        buildSrcContentCreator.writeProjectJavaLibraryFile(projectRoot)

        gradlePropertiesFileCreator.writeGradlePropertiesFile(projectRoot)
        GradleWrapperCreator().writeGradleWrapperFiles(projectRoot)
    }
}
