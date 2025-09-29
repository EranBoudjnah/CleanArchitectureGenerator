package com.mitteloupe.cag.settings.versioncatalog.template

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle
import com.mitteloupe.cag.settings.versioncatalog.VersionCatalogConfigurable

class NewProjectLibrariesConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable =
        VersionCatalogConfigurable(
            VersionCatalogAppSettingsService.getInstance(),
            CleanArchitectureGeneratorBundle.message("settings.versions.new.projects.display.name"),
            "com.mitteloupe.cag.settings.VersionCatalogSettings.NewProjectLibraries",
            CleanArchitectureGeneratorBundle.message("settings.versions.new.projects.description")
        )
}
