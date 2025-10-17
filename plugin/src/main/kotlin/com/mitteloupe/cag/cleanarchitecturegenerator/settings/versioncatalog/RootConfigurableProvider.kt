package com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.generator.nova.GenerationSpec.Companion.nullIfEmpty
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle
import com.mitteloupe.cag.cleanarchitecturegenerator.model.DependencyInjection
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.AppSettingsService
import com.mitteloupe.cag.git.Git
import java.io.File
import javax.swing.JLabel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class RootConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable = RootConfigurable()
}

private class RootConfigurable :
    BoundSearchableConfigurable(
        CleanArchitectureGeneratorBundle.message("settings.display.name"),
        "com.mitteloupe.cag.settings.Settings"
    ) {
    val appSettingsService = AppSettingsService.getInstance()
    private val serviceAutoAddToGit: Boolean
        get() = appSettingsService.autoAddGeneratedFilesToGit
    private val serviceGitPath: String?
        get() = appSettingsService.gitPath
    private val serviceDefaultDependencyInjection: DependencyInjection
        get() = DependencyInjection.fromString(appSettingsService.defaultDependencyInjection)

    private var autoAddToGit: Boolean = serviceAutoAddToGit
    private var gitPath: String = serviceGitPath.orEmpty()
    private var defaultDependencyInjection: DependencyInjection = serviceDefaultDependencyInjection

    private val autoAddToGitChanged: Boolean
        get() = autoAddToGit != serviceAutoAddToGit
    private val gitPathChanged: Boolean
        get() = gitPath != serviceGitPath.orEmpty()
    private val defaultDependencyInjectionChanged: Boolean
        get() = defaultDependencyInjection != serviceDefaultDependencyInjection

    override fun createPanel() =
        panel {
            var gitWarningLabel: JLabel? = null

            fun isGitAvailableForState(pathText: String): Boolean =
                Git(gitBinaryPath = pathText.nullIfEmpty()).isAvailable(File(System.getProperty("user.home")))

            fun updateWarnings(currentPath: String) {
                val showGitWarning = !isGitAvailableForState(currentPath.trim())
                gitWarningLabel?.isVisible = showGitWarning
            }

            row {
                @Suppress("DialogTitleCapitalization")
                text(CleanArchitectureGeneratorBundle.message("settings.root.description"))
            }

            row {
                checkBox(CleanArchitectureGeneratorBundle.message("settings.auto.add.to.git.label"))
                    .applyToComponent {
                        toolTipText = CleanArchitectureGeneratorBundle.message("settings.auto.add.to.git.tooltip")
                        addChangeListener { updateWarnings(gitPath) }
                    }.bindSelected(::autoAddToGit)
            }

            row(CleanArchitectureGeneratorBundle.message("settings.git.path.label")) {
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                textFieldWithBrowseButton(
                    project = null,
                    fileChooserDescriptor = descriptor,
                    fileChosen = { it.path }
                ).applyToComponent {
                    toolTipText = CleanArchitectureGeneratorBundle.message("settings.git.path.tooltip")
                    textField.document.addDocumentListener(
                        object : DocumentListener {
                            override fun insertUpdate(event: DocumentEvent) {
                                updateWarnings(text)
                            }

                            override fun removeUpdate(event: DocumentEvent) {
                                updateWarnings(text)
                            }

                            override fun changedUpdate(evenet: DocumentEvent) {
                                updateWarnings(text)
                            }
                        }
                    )
                }.bindText(::gitPath)
            }

            row {
                label(CleanArchitectureGeneratorBundle.message("settings.git.not.found.warning")).applyToComponent {
                    icon = AllIcons.General.Warning
                    gitWarningLabel = this
                    isVisible = false
                }
            }

            row(CleanArchitectureGeneratorBundle.message("settings.dependency.injection.label")) {
                comboBox(DependencyInjection.entries)
                    .bindItem(
                        { defaultDependencyInjection },
                        { newValue ->
                            if (newValue != null) {
                                defaultDependencyInjection = newValue
                            }
                        }
                    )
            }

            onApply {
                updateWarnings(gitPath)
            }
            onReset {
                updateWarnings(gitPath)
            }
            onIsModified {
                updateWarnings(gitPath)
                false
            }

            onApply {
                AppSettingsService.getInstance().autoAddGeneratedFilesToGit = autoAddToGit
                AppSettingsService.getInstance().gitPath = gitPath.ifBlank { null }
                AppSettingsService.getInstance().defaultDependencyInjection = defaultDependencyInjection.name
            }
            onReset {
                autoAddToGit = serviceAutoAddToGit
                gitPath = serviceGitPath.orEmpty()
                defaultDependencyInjection = serviceDefaultDependencyInjection
            }
            onIsModified {
                autoAddToGitChanged || gitPathChanged || defaultDependencyInjectionChanged
            }
        }
}
