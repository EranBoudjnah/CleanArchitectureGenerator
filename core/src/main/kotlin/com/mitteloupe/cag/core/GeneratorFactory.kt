package com.mitteloupe.cag.core

import com.mitteloupe.cag.core.filesystem.FileSystemBridge
import com.mitteloupe.cag.core.generation.BuildSrcContentCreator
import com.mitteloupe.cag.core.generation.ConfigurationFileCreator
import com.mitteloupe.cag.core.generation.KotlinFileCreator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.app.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.app.DataSourceDependencyInjectionModuleCreator
import com.mitteloupe.cag.core.generation.architecture.ArchitectureModulesContentGenerator
import com.mitteloupe.cag.core.generation.architecture.CoroutineModuleContentGenerator
import com.mitteloupe.cag.core.generation.bulk.ArchitectureFilesGenerator
import com.mitteloupe.cag.core.generation.bulk.DataSourceFilesGenerator
import com.mitteloupe.cag.core.generation.bulk.DataSourceModulesGenerator
import com.mitteloupe.cag.core.generation.bulk.FeatureFilesGenerator
import com.mitteloupe.cag.core.generation.bulk.ProjectTemplateFilesGenerator
import com.mitteloupe.cag.core.generation.bulk.ViewModelFilesGenerator
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.gradle.GradleFileCreator
import com.mitteloupe.cag.core.generation.gradle.GradlePropertiesFileCreator
import com.mitteloupe.cag.core.generation.gradle.GradleWrapperCreator
import com.mitteloupe.cag.core.generation.layer.DataLayerContentGenerator
import com.mitteloupe.cag.core.generation.layer.DataSourceImplementationCreator
import com.mitteloupe.cag.core.generation.layer.DataSourceInterfaceCreator
import com.mitteloupe.cag.core.generation.layer.DomainLayerContentGenerator
import com.mitteloupe.cag.core.generation.layer.PresentationLayerContentGenerator
import com.mitteloupe.cag.core.generation.layer.UiLayerContentGenerator
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater

class GeneratorFactory(
    private val fileSystemBridge: FileSystemBridge
) {
    fun create(): Generator {
        val fileCreator = FileCreator(fileSystemBridge)
        val directoryFinder = DirectoryFinder()
        val kotlinFileCreator = KotlinFileCreator(fileCreator)
        val gradleFileCreator = GradleFileCreator(fileCreator)

        val gradleWrapperCreator = GradleWrapperCreator(fileCreator)
        val buildSrcContentCreator = BuildSrcContentCreator(fileCreator)
        val gradlePropertiesFileCreator = GradlePropertiesFileCreator(fileCreator)
        val catalogUpdater = VersionCatalogUpdater(fileCreator)
        val architectureModulesContentGenerator = ArchitectureModulesContentGenerator(gradleFileCreator, catalogUpdater)
        val coroutineModuleContentGenerator = CoroutineModuleContentGenerator(gradleFileCreator, catalogUpdater)
        val uiLayerContentGenerator = UiLayerContentGenerator(kotlinFileCreator)
        val dataLayerContentGenerator = DataLayerContentGenerator(kotlinFileCreator)
        val dataSourceDependencyInjectionModuleCreator = DataSourceDependencyInjectionModuleCreator(fileCreator)
        val dataSourceInterfaceCreator = DataSourceInterfaceCreator(fileCreator)
        val dataSourceImplementationCreator = DataSourceImplementationCreator(fileCreator)
        val settingsFileUpdater = SettingsFileUpdater(fileCreator)
        val domainLayerContentGenerator = DomainLayerContentGenerator(kotlinFileCreator)
        val presentationLayerContentGenerator = PresentationLayerContentGenerator(kotlinFileCreator, fileCreator)
        val configurationFileCreator = ConfigurationFileCreator(fileCreator)
        val appModuleContentGenerator = AppModuleContentGenerator(fileCreator, directoryFinder)
        val dataSourceModulesGenerator =
            DataSourceModulesGenerator(
                catalogUpdater,
                gradleFileCreator,
                settingsFileUpdater
            )
        val architectureFilesGenerator =
            ArchitectureFilesGenerator(
                coroutineModuleContentGenerator,
                architectureModulesContentGenerator,
                settingsFileUpdater,
                buildSrcContentCreator,
                configurationFileCreator
            )
        val featureFilesGenerator =
            FeatureFilesGenerator(
                catalogUpdater,
                gradleFileCreator,
                domainLayerContentGenerator,
                presentationLayerContentGenerator,
                dataLayerContentGenerator,
                uiLayerContentGenerator,
                settingsFileUpdater,
                configurationFileCreator,
                appModuleContentGenerator
            )
        return Generator(
            domainLayerContentGenerator,
            featureFilesGenerator,
            dataSourceModulesGenerator,
            DataSourceFilesGenerator(
                dataSourceInterfaceCreator,
                dataSourceImplementationCreator,
                dataSourceDependencyInjectionModuleCreator
            ),
            architectureFilesGenerator,
            ProjectTemplateFilesGenerator(
                catalogUpdater,
                settingsFileUpdater,
                configurationFileCreator,
                gradleFileCreator,
                gradlePropertiesFileCreator,
                gradleWrapperCreator,
                appModuleContentGenerator,
                buildSrcContentCreator,
                dataSourceModulesGenerator,
                architectureFilesGenerator,
                featureFilesGenerator
            ),
            ViewModelFilesGenerator(presentationLayerContentGenerator)
        )
    }
}
