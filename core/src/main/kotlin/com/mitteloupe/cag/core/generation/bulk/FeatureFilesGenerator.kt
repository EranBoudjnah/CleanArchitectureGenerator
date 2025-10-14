package com.mitteloupe.cag.core.generation.bulk

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildDataGradleScript
import com.mitteloupe.cag.core.content.buildDomainGradleScript
import com.mitteloupe.cag.core.content.buildPresentationGradleScript
import com.mitteloupe.cag.core.content.buildUiGradleScript
import com.mitteloupe.cag.core.generation.ConfigurationFileCreator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.app.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.app.AppModuleGradleUpdater
import com.mitteloupe.cag.core.generation.gradle.GradleFileCreator
import com.mitteloupe.cag.core.generation.layer.DataLayerContentGenerator
import com.mitteloupe.cag.core.generation.layer.DomainLayerContentGenerator
import com.mitteloupe.cag.core.generation.layer.PresentationLayerContentGenerator
import com.mitteloupe.cag.core.generation.layer.UiLayerContentGenerator
import com.mitteloupe.cag.core.generation.versioncatalog.DependencyConfiguration
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import com.mitteloupe.cag.core.option.DependencyInjection
import java.io.File

class FeatureFilesGenerator(
    private val catalogUpdater: VersionCatalogUpdater,
    private val gradleFileCreator: GradleFileCreator,
    private val domainLayerContentGenerator: DomainLayerContentGenerator,
    private val presentationLayerContentGenerator: PresentationLayerContentGenerator,
    private val dataLayerContentGenerator: DataLayerContentGenerator,
    private val uiLayerContentGenerator: UiLayerContentGenerator,
    private val settingsFileUpdater: SettingsFileUpdater,
    private val configurationFileCreator: ConfigurationFileCreator,
    private val appModuleContentGenerator: AppModuleContentGenerator
) {
    fun generateFeature(
        featurePackageName: String?,
        featureName: String,
        projectNamespace: String,
        destinationRootDirectory: File,
        appModuleDirectory: File?,
        dependencyInjection: DependencyInjection,
        enableCompose: Boolean,
        enableKtlint: Boolean,
        enableDetekt: Boolean
    ) {
        val featurePackageName = featurePackageName?.trim()
        if (featurePackageName.isNullOrEmpty()) {
            throw GenerationException("Feature package name is missing.")
        }

        val pathSegments = featurePackageName.toSegments()
        if (pathSegments.isEmpty()) {
            throw GenerationException("Feature package name is invalid.")
        }

        val featureNameLowercase = featureName.lowercase()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries =
                    if (enableCompose) {
                        LibraryConstants.COMPOSE_LIBRARIES
                    } else {
                        emptyList()
                    },
                plugins =
                    buildList {
                        addAll(PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS)
                        if (enableCompose) {
                            add(PluginConstants.COMPOSE_COMPILER)
                        }
                        if (enableKtlint) {
                            add(PluginConstants.KTLINT)
                        }
                        if (enableDetekt) {
                            add(PluginConstants.DETEKT)
                        }
                    }
            )
        catalogUpdater.createOrUpdateVersionCatalog(
            projectRootDirectory = destinationRootDirectory,
            dependencyConfiguration = dependencyConfiguration
        )
        val featureRoot = File(destinationRootDirectory, "features/$featureNameLowercase")

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
                layer = "domain"
            ) { buildDomainGradleScript(catalogUpdater) }
            domainLayerContentGenerator
                .generateDomainLayer(
                    featureRoot = featureRoot,
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName
                )
            gradleFileCreator.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "presentation"
            ) { buildPresentationGradleScript(featureNameLowercase, catalogUpdater) }
            presentationLayerContentGenerator
                .generatePresentationLayer(
                    featureRoot = featureRoot,
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = featureName
                )
            gradleFileCreator.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "data"
            ) { buildDataGradleScript(featureNameLowercase, catalogUpdater) }
            dataLayerContentGenerator
                .generate(
                    featureRoot = featureRoot,
                    featurePackageName = featurePackageName,
                    featureName = featureName
                )
            gradleFileCreator.writeGradleFileIfMissing(
                featureRoot = featureRoot,
                layer = "ui"
            ) {
                buildUiGradleScript(
                    featurePackageName = featurePackageName,
                    featureNameLowerCase = featureNameLowercase,
                    enableCompose = enableCompose,
                    catalog = catalogUpdater
                )
            }
            uiLayerContentGenerator
                .generate(
                    featureRoot = featureRoot,
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = featureName
                )
            if (enableDetekt) {
                configurationFileCreator.writeDetektConfigurationFile(destinationRootDirectory)
            }
            if (enableKtlint) {
                configurationFileCreator.writeEditorConfigFile(destinationRootDirectory)
            }
            settingsFileUpdater.updateProjectSettingsIfPresent(destinationRootDirectory, featureNameLowercase)
            appModuleContentGenerator.writeFeatureDependencyInjectionModuleIfPossible(
                startDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                featureName = featureName,
                featurePackageName = featurePackageName,
                appModuleDirectory = appModuleDirectory,
                dependencyInjection = dependencyInjection
            )
            AppModuleGradleUpdater().updateAppModuleDependenciesIfPresent(
                startDirectory = destinationRootDirectory,
                featureNameLowerCase = featureNameLowercase,
                appModuleDirectory = appModuleDirectory
            )
        }

        if (!allCreated) {
            throw GenerationException("Failed to create directories for package '$featurePackageName'.")
        }
    }
}
