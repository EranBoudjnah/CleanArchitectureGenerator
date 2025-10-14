package com.mitteloupe.cag.core.generation.bulk

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.generation.BuildSrcContentCreator
import com.mitteloupe.cag.core.generation.ConfigurationFileCreator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.app.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.gradle.GradleFileCreator
import com.mitteloupe.cag.core.generation.gradle.GradlePropertiesFileCreator
import com.mitteloupe.cag.core.generation.gradle.GradleWrapperCreator
import com.mitteloupe.cag.core.generation.versioncatalog.DependencyConfiguration
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.generation.withoutSpaces
import java.io.File

class ProjectTemplateFilesGenerator(
    private val catalogUpdater: VersionCatalogUpdater,
    private val settingsFileUpdater: SettingsFileUpdater,
    private val configurationFileCreator: ConfigurationFileCreator,
    private val gradleFileCreator: GradleFileCreator,
    private val gradlePropertiesFileCreator: GradlePropertiesFileCreator,
    private val gradleWrapperCreator: GradleWrapperCreator,
    private val appModuleContentGenerator: AppModuleContentGenerator,
    private val buildSrcContentCreator: BuildSrcContentCreator,
    private val dataSourceModulesGenerator: DataSourceModulesGenerator,
    private val architectureFilesGenerator: ArchitectureFilesGenerator,
    private val featureFilesGenerator: FeatureFilesGenerator
) {
    fun generateProjectTemplate(
        destinationRootDirectory: File,
        projectName: String,
        packageName: String,
        overrideMinimumAndroidSdk: Int?,
        overrideAndroidGradlePluginVersion: String?,
        enableHilt: Boolean,
        enableCompose: Boolean,
        enableKtlint: Boolean,
        enableDetekt: Boolean,
        enableKtor: Boolean,
        enableRetrofit: Boolean
    ) {
        val projectName = projectName.trim()
        if (projectName.isEmpty()) {
            throw GenerationException("Project name is missing.")
        }

        val packageName = packageName.trim()
        if (packageName.isEmpty()) {
            throw GenerationException("Package name is missing.")
        }

        val sanitizedProjectName = projectName.withoutSpaces()
        val projectRoot =
            if (destinationRootDirectory.name.matches("$sanitizedProjectName\\d*".toRegex())) {
                destinationRootDirectory
            } else {
                File(destinationRootDirectory, sanitizedProjectName)
            }

        if (projectRoot != destinationRootDirectory && projectRoot.exists()) {
            throw GenerationException(
                if (projectRoot.isDirectory) {
                    "The project directory already exists."
                } else {
                    "A file with the project name exists where the project directory should be created."
                }
            )
        }

        if (projectRoot != destinationRootDirectory && !projectRoot.mkdirs()) {
            throw GenerationException("Failed to create project directory.")
        }

        val libraries =
            LibraryConstants.CORE_ANDROID_LIBRARIES +
                LibraryConstants.HILT_LIBRARIES +
                LibraryConstants.TESTING_LIBRARIES +
                if (enableCompose) {
                    LibraryConstants.COMPOSE_LIBRARIES
                } else {
                    LibraryConstants.VIEW_LIBRARIES
                }
        val plugins =
            buildList {
                addAll(PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS + PluginConstants.HILT_ANDROID)
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
        val versionOverrides =
            listOf(
                VersionCatalogConstants.MIN_SDK_VERSION to overrideMinimumAndroidSdk?.toString(),
                VersionCatalogConstants.ANDROID_GRADLE_PLUGIN_VERSION to overrideAndroidGradlePluginVersion
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
        architectureFilesGenerator.generateArchitecture(
            destinationRootDirectory = projectRoot,
            architecturePackageName = "$packageName.architecture",
            enableHilt = enableHilt,
            enableCompose = enableCompose,
            enableKtlint = enableKtlint,
            enableDetekt = enableDetekt
        )
        dataSourceModulesGenerator.generateDataSourceModules(
            destinationRootDirectory = projectRoot,
            useKtor = enableKtor,
            useRetrofit = enableRetrofit
        )
        val featureName = "SampleFeature"
        featureFilesGenerator.generateFeature(
            "$packageName.${featureName.lowercase()}",
            featureName,
            packageName,
            projectRoot,
            null,
            enableCompose,
            enableKtlint,
            enableDetekt
        )
        appModuleContentGenerator.writeAppModule(
            startDirectory = projectRoot,
            appName = projectName,
            projectNamespace = packageName,
            enableCompose = enableCompose
        )
        generateTemplateProjectGradleFiles(
            projectRoot = projectRoot,
            packageName = packageName,
            enableHilt = enableHilt,
            enableCompose = enableCompose,
            enableKtlint = enableKtlint,
            enableDetekt = enableDetekt
        )
        settingsFileUpdater.writeProjectSettings(
            projectRoot = projectRoot,
            projectName = projectName,
            featureNames = listOf(featureName)
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
            val absoluteDirectory = File(projectRoot, directory)
            if (!absoluteDirectory.exists()) {
                absoluteDirectory.mkdirs()
            }
        }
    }

    private fun generateTemplateProjectGradleFiles(
        projectRoot: File,
        packageName: String,
        enableHilt: Boolean,
        enableCompose: Boolean,
        enableKtlint: Boolean,
        enableDetekt: Boolean
    ) {
        gradleFileCreator.writeProjectGradleFile(
            projectRoot = projectRoot,
            enableHilt = enableHilt,
            enableKtlint = enableKtlint,
            enableDetekt = enableDetekt,
            catalog = catalogUpdater
        )

        gradleFileCreator.writeAppGradleFile(
            projectRoot = projectRoot,
            packageName = packageName,
            enableHilt = enableHilt,
            enableCompose = enableCompose,
            catalog = catalogUpdater
        )

        buildSrcContentCreator.writeGradleFile(projectRoot)
        buildSrcContentCreator.writeSettingsGradleFile(projectRoot)
        buildSrcContentCreator.writeProjectJavaLibraryFile(projectRoot)

        gradlePropertiesFileCreator.writeGradlePropertiesFile(projectRoot)
        gradleWrapperCreator.writeGradleWrapperFiles(projectRoot)

        if (enableDetekt) {
            configurationFileCreator.writeDetektConfigurationFile(projectRoot)
        }
        if (enableKtlint) {
            configurationFileCreator.writeEditorConfigFile(projectRoot)
        }
    }
}
