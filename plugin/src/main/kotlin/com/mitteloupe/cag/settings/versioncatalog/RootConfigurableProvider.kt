package com.mitteloupe.cag.settings.versioncatalog

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.dsl.builder.panel
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle

class RootConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable = RootConfigurable()
}

private class RootConfigurable : BoundSearchableConfigurable(
    CleanArchitectureGeneratorBundle.message("settings.display.name"),
    "com.mitteloupe.cag.settings.Settings"
) {
    override fun createPanel() =
        panel {
            row {
                text(CleanArchitectureGeneratorBundle.message("settings.root.description"))
            }
        }
}
