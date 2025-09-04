package com.mitteloupe.cag.core

import com.mitteloupe.cag.core.content.buildDataGradleScript
import com.mitteloupe.cag.core.content.buildDataSourceImplementationGradleScript
import com.mitteloupe.cag.core.content.buildDataSourceSourceGradleScript
import com.mitteloupe.cag.core.content.buildDomainGradleScript
import com.mitteloupe.cag.core.content.buildPresentationGradleScript
import com.mitteloupe.cag.core.content.buildUiGradleScript
import com.mitteloupe.cag.core.generation.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.AppModuleGradleUpdater
import com.mitteloupe.cag.core.generation.DataLayerContentGenerator
import com.mitteloupe.cag.core.generation.DataSourceImplementationCreator
import com.mitteloupe.cag.core.generation.DataSourceInterfaceCreator
import com.mitteloupe.cag.core.generation.DataSourceModuleCreator
import com.mitteloupe.cag.core.generation.DomainLayerContentGenerator
import com.mitteloupe.cag.core.generation.GradleFileCreator
import com.mitteloupe.cag.core.generation.PresentationLayerContentGenerator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.UiLayerContentGenerator
import com.mitteloupe.cag.core.generation.architecture.ArchitectureModulesContentGenerator
import com.mitteloupe.cag.core.generation.architecture.CoroutineModuleContentGenerator
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

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
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = request.destinationRootDirectory,
            enableCompose = request.enableCompose
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
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = destinationRootDirectory,
            enableCompose = false
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

        val projectRoot =
            if (request.destinationRootDirectory.name == projectName) {
                request.destinationRootDirectory
            } else {
                File(request.destinationRootDirectory, projectName)
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
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = projectRoot,
            enableCompose = request.enableCompose
        )

        generateProjectStructure(projectRoot)
        generateArchitectureModules(projectRoot, packageName, request)
        generateSampleFeature(projectRoot, packageName, request)
        generateDataSourceModules(projectRoot, request)
        generateAppModule(projectRoot, packageName, request)
        generateGradleFiles(projectRoot, projectName, request)
        generateSettingsFile(projectRoot, projectName, request)
    }

    private fun generateProjectStructure(projectRoot: File) {
        val directories =
            listOf(
                "app/src/main/java",
                "app/src/main/res/layout",
                "app/src/main/res/values",
                "app/src/main/res/drawable",
                "app/src/main/res/mipmap-hdpi",
                "app/src/main/res/mipmap-mdpi",
                "app/src/main/res/mipmap-xhdpi",
                "app/src/main/res/mipmap-xxhdpi",
                "app/src/main/res/mipmap-xxxhdpi",
                "app/src/test/java",
                "app/src/androidTest/java"
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
        packageName: String,
        request: GenerateProjectTemplateRequest
    ) {
        val architectureRequest =
            GenerateArchitectureRequest(
                destinationRootDirectory = projectRoot,
                architecturePackageName = packageName,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt
            )
        generateArchitecture(architectureRequest)
    }

    private fun generateSampleFeature(
        projectRoot: File,
        packageName: String,
        request: GenerateProjectTemplateRequest
    ) {
        val featureRequest =
            GenerateFeatureRequest(
                destinationRootDirectory = projectRoot,
                featureName = "SampleFeature",
                featurePackageName = "$packageName.samplefeature",
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
        packageName: String,
        request: GenerateProjectTemplateRequest
    ) {
        val appModuleContentGenerator = AppModuleContentGenerator()
        appModuleContentGenerator.writeAppModule(
            startDirectory = projectRoot,
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
        val catalogUpdater = VersionCatalogUpdater()
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = projectRoot,
            enableCompose = request.enableCompose
        )

        gradleFileCreator.writeProjectGradleFile(
            projectRoot = projectRoot,
            enableKtlint = request.enableKtlint,
            enableDetekt = request.enableDetekt
        )

        gradleFileCreator.writeAppGradleFile(
            projectRoot = projectRoot,
            packageName = packageName,
            enableCompose = request.enableCompose
        )
    }

    private fun generateSettingsFile(
        projectRoot: File,
        projectName: String,
        request: GenerateProjectTemplateRequest
    ) {
        val settingsFileUpdater = SettingsFileUpdater()
        settingsFileUpdater.writeProjectSettings(
            projectRoot = projectRoot,
            projectName = projectName,
            enableKtlint = request.enableKtlint,
            enableDetekt = request.enableDetekt
        )
    }
}
