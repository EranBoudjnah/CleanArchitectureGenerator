package com.mitteloupe.cag.cleanarchitecturegenerator.filesystem

import com.intellij.openapi.project.Project
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog.current.VersionCatalogProjectSettingsService
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog.template.VersionCatalogAppSettingsService
import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.Generator
import com.mitteloupe.cag.core.generation.AppModuleContentGenerator
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
import com.mitteloupe.cag.core.generation.KotlinFileCreator
import com.mitteloupe.cag.core.generation.PresentationLayerContentGenerator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.UiLayerContentGenerator
import com.mitteloupe.cag.core.generation.architecture.ArchitectureModulesContentGenerator
import com.mitteloupe.cag.core.generation.architecture.CoroutineModuleContentGenerator
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogSettingsAccessor
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater

class GeneratorProvider {
    class GeneratorInitializer internal constructor(
        private val generatorProvider: GeneratorProvider,
        val project: Project?
    ) {
        fun generate() = generatorProvider.generator(project)
    }

    fun prepare(project: Project?) =
        GeneratorInitializer(this, project).also {
            installVersionProvider(project)
        }

    private fun generator(project: Project?): Generator {
        val fileCreator = FileCreator(IntelliJFileSystemBridge(project))
        val directoryFinder = DirectoryFinder()
        val kotlinFileCreator = KotlinFileCreator(fileCreator)
        val gradleFileCreator = GradleFileCreator(fileCreator)

        val catalogUpdater = VersionCatalogUpdater(fileCreator)
        return Generator(
            GradleFileCreator(fileCreator),
            GradleWrapperCreator(fileCreator),
            AppModuleContentGenerator(fileCreator, directoryFinder),
            BuildSrcContentCreator(fileCreator),
            ConfigurationFileCreator(fileCreator),
            UiLayerContentGenerator(kotlinFileCreator),
            PresentationLayerContentGenerator(kotlinFileCreator, fileCreator),
            DomainLayerContentGenerator(kotlinFileCreator),
            DataLayerContentGenerator(kotlinFileCreator),
            DataSourceModuleCreator(fileCreator),
            DataSourceInterfaceCreator(fileCreator),
            DataSourceImplementationCreator(fileCreator),
            GradlePropertiesFileCreator(fileCreator),
            ArchitectureModulesContentGenerator(gradleFileCreator, catalogUpdater),
            CoroutineModuleContentGenerator(gradleFileCreator, catalogUpdater),
            VersionCatalogUpdater(fileCreator),
            SettingsFileUpdater(fileCreator)
        )
    }

    private fun installVersionProvider(project: Project?) {
        val settingsService =
            if (project == null) {
                VersionCatalogAppSettingsService.getInstance()
            } else {
                VersionCatalogProjectSettingsService.getInstance(project)
            }
        settingsService.initialize()
        VersionCatalogSettingsAccessor.setProvider { key, default ->
            val values = settingsService.getCurrentValues()
            values[key] ?: default
        }
    }
}
