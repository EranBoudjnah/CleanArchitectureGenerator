package com.mitteloupe.cag.settings.versioncatalog.current

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle
import com.mitteloupe.cag.settings.versioncatalog.VersionCatalogConfigurable

class CurrentProjectLibrariesConfigurableProvider(private val project: Project) : ConfigurableProvider() {
    override fun createConfigurable(): Configurable =
        VersionCatalogConfigurable(
            VersionCatalogProjectSettingsService.getInstance(project),
            CleanArchitectureGeneratorBundle.message("settings.versions.current.project.display.name"),
            "com.mitteloupe.cag.settings.VersionCatalogSettings.CurrentProjectLibraries",
            CleanArchitectureGeneratorBundle.message("settings.versions.current.project.description")
        )
}
