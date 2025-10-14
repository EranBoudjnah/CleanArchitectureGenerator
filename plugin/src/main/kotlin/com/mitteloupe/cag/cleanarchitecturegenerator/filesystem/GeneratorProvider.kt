package com.mitteloupe.cag.cleanarchitecturegenerator.filesystem

import com.intellij.openapi.project.Project
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog.current.VersionCatalogProjectSettingsService
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog.template.VersionCatalogAppSettingsService
import com.mitteloupe.cag.core.GeneratorFactory
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogSettingsAccessor

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

    private fun generator(project: Project?) = GeneratorFactory(IntelliJFileSystemBridge(project)).create()

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
