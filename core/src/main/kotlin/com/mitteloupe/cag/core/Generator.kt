package com.mitteloupe.cag.core

import com.mitteloupe.cag.core.content.buildDataGradleScript
import com.mitteloupe.cag.core.content.buildDomainGradleScript
import com.mitteloupe.cag.core.content.buildPresentationGradleScript
import com.mitteloupe.cag.core.content.buildUiGradleScript
import com.mitteloupe.cag.core.generation.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.AppModuleGradleUpdater
import com.mitteloupe.cag.core.generation.DataLayerContentGenerator
import com.mitteloupe.cag.core.generation.DomainLayerContentGenerator
import com.mitteloupe.cag.core.generation.GradleFileCreator
import com.mitteloupe.cag.core.generation.PresentationLayerContentGenerator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.UiLayerContentGenerator
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

const val ERROR_PREFIX = "Error: "

class Generator {
    fun generateFeature(request: GenerateFeatureRequest): String {
        val featurePackageName = request.featurePackageName?.trim()
        if (featurePackageName.isNullOrEmpty()) {
            return "${ERROR_PREFIX}Feature package name is missing."
        }

        val pathSegments = featurePackageName.toSegments()
        if (pathSegments.isEmpty()) {
            return "${ERROR_PREFIX}Feature package name is invalid."
        }

        val featureNameLowerCase = request.featureName.lowercase()
        val catalogUpdater = VersionCatalogUpdater()
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = request.destinationRootDirectory,
            enableCompose = request.enableCompose
        )?.let { return it }
        val featureRoot = File(request.destinationRootDirectory, "features/$featureNameLowerCase")

        if (featureRoot.exists()) {
            return ERROR_PREFIX +
                if (featureRoot.isDirectory) {
                    "The feature directory already exists."
                } else {
                    "A file with the feature name exists where the feature directory should be created."
                }
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
            createDomainModule(featureRoot, catalogUpdater)?.let { return it }
            DomainLayerContentGenerator()
                .generate(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName
                )?.let { return it }
            createPresentationModule(featureRoot, featureNameLowerCase, catalogUpdater)?.let { return it }
            PresentationLayerContentGenerator()
                .generate(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )?.let { return it }
            createDataModule(featureRoot, featureNameLowerCase, catalogUpdater)?.let { return it }
            DataLayerContentGenerator()
                .generate(
                    featureRoot = featureRoot,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )?.let { return it }
            createUiModule(
                featureRoot,
                featurePackageName,
                featureNameLowerCase,
                request.enableCompose,
                catalogUpdater
            )?.let { return it }
            UiLayerContentGenerator()
                .generate(
                    featureRoot = featureRoot,
                    projectNamespace = request.projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = request.featureName
                )?.let { return it }
            SettingsFileUpdater().updateProjectSettingsIfPresent(
                request.destinationRootDirectory,
                featureNameLowerCase
            )?.let { return it }
            AppModuleContentGenerator().writeFeatureModuleIfPossible(
                startDirectory = request.destinationRootDirectory,
                projectNamespace = request.projectNamespace,
                featureName = request.featureName,
                featurePackageName = featurePackageName
            )?.let { return it }
            AppModuleGradleUpdater().updateAppModuleDependenciesIfPresent(
                startDirectory = request.destinationRootDirectory,
                featureNameLowerCase = featureNameLowerCase
            )?.let { return it }
        }

        return if (allCreated) {
            "Success!"
        } else {
            "${ERROR_PREFIX}Failed to create directories for package '$featurePackageName'."
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun generateDataSource(
        destinationRootDirectory: File,
        dataSourceName: String
    ): String {
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        val modules = listOf("source", "implementation")

        val allCreated =
            modules.map { moduleName ->
                val moduleDirectory = File(datasourceRoot, moduleName)
                if (moduleDirectory.exists()) {
                    moduleDirectory.isDirectory
                } else {
                    moduleDirectory.mkdirs()
                }
            }.all { it }

        if (!allCreated) {
            return "${ERROR_PREFIX}Failed to create directories for datasource."
        }

        val gradleFileCreator = GradleFileCreator()
        for (moduleName in modules) {
            gradleFileCreator.writeGradleFileIfMissing(
                featureRoot = datasourceRoot,
                layer = moduleName,
                content = ""
            )?.let { return it }
        }

        SettingsFileUpdater().updateDataSourceSettingsIfPresent(destinationRootDirectory)?.let { return it }

        return "Success!"
    }

    private fun createDomainModule(
        featureRoot: File,
        catalog: VersionCatalogUpdater
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "domain",
            content = buildDomainGradleScript(catalog)
        )

    private fun createDataModule(
        featureRoot: File,
        featureNameLowerCase: String,
        catalog: VersionCatalogUpdater
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "data",
            content = buildDataGradleScript(featureNameLowerCase, catalog)
        )

    private fun createPresentationModule(
        featureRoot: File,
        featureNameLowerCase: String,
        catalog: VersionCatalogUpdater
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "presentation",
            content = buildPresentationGradleScript(featureNameLowerCase, catalog)
        )

    private fun createUiModule(
        featureRoot: File,
        featurePackageName: String,
        featureNameLowerCase: String,
        enableCompose: Boolean,
        catalog: VersionCatalogUpdater
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "ui",
            content =
                buildUiGradleScript(
                    featurePackageName = featurePackageName,
                    featureNameLowerCase = featureNameLowerCase,
                    enableCompose = enableCompose,
                    catalog = catalog
                )
        )
}
