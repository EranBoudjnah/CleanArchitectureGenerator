package com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.AppSettingsService

class RootConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable = RootConfigurable()
}

private class RootConfigurable : BoundSearchableConfigurable(
    CleanArchitectureGeneratorBundle.message("settings.display.name"),
    "com.mitteloupe.cag.settings.Settings"
) {
    private val serviceAutoAddToGit: Boolean
        get() = AppSettingsService.getInstance().autoAddGeneratedFilesToGit

    private var autoAddToGit: Boolean = serviceAutoAddToGit

    override fun createPanel() =
        panel {
            row {
                text(CleanArchitectureGeneratorBundle.message("settings.root.description"))
            }

            row {
                checkBox(CleanArchitectureGeneratorBundle.message("settings.auto.add.to.git.label"))
                    .applyToComponent {
                        toolTipText = CleanArchitectureGeneratorBundle.message("settings.auto.add.to.git.tooltip")
                    }
                    .bindSelected(::autoAddToGit)
            }

            onApply {
                AppSettingsService.getInstance().autoAddGeneratedFilesToGit = autoAddToGit
            }
            onReset {
                autoAddToGit = serviceAutoAddToGit
            }
            onIsModified {
                autoAddToGit != serviceAutoAddToGit
            }
        }
}
