package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.mitteloupe.cag.cleanarchitecturegenerator.model.DependencyInjection
import java.io.File
import javax.swing.JComponent

class CreateCleanArchitecturePackageDialog(
    project: Project,
    private val appModuleDirectories: List<File>,
    defaultDependencyInjection: DependencyInjection
) : DialogWrapper(project) {
    private var enableCompose: Boolean = true
    private var enableKtlint: Boolean = false
    private var enableDetekt: Boolean = false

    private var appModuleSelectedIndex: Int = 0

    private var dependencyInjection = defaultDependencyInjection

    val selectedAppModuleDirectory: File?
        get() =
            if (appModuleDirectories.isEmpty()) {
                null
            } else {
                if (appModuleSelectedIndex in appModuleDirectories.indices) {
                    appModuleDirectories[appModuleSelectedIndex]
                } else {
                    null
                }
            }

    val selectedDependencyInjection: DependencyInjection
        get() = dependencyInjection

    init {
        title = CleanArchitectureGeneratorBundle.message("info.architecture.generator.title")
        init()
    }

    override fun createCenterPanel(): JComponent =
        panel {
            if (appModuleDirectories.size >= 2) {
                row(CleanArchitectureGeneratorBundle.message("dialog.feature.app.module.label")) {
                    val appModules = appModuleDirectories.map { it.name }
                    comboBox(appModules, null)
                        .bindItem(
                            getter = { appModules[appModuleSelectedIndex] },
                            setter = { appModuleSelectedIndex = it?.let(appModules::indexOf) ?: 0 }
                        )
                }
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.architecture.dependency.injection.label")) {
                comboBox(DependencyInjection.entries)
                    .bindItem(
                        { dependencyInjection },
                        { value -> value?.let { dependencyInjection = it } }
                    )
            }
            row {
                @Suppress("DialogTitleCapitalization")
                checkBox(CleanArchitectureGeneratorBundle.message("dialog.architecture.compose.label"))
                    .bindSelected(::enableCompose)
            }
            row {
                checkBox(CleanArchitectureGeneratorBundle.message("dialog.architecture.ktlint.label"))
                    .bindSelected(::enableKtlint)
            }
            row {
                checkBox(CleanArchitectureGeneratorBundle.message("dialog.architecture.detekt.label"))
                    .bindSelected(::enableDetekt)
            }
        }

    override fun doValidate(): ValidationInfo? = null

    fun isComposeEnabled(): Boolean = enableCompose

    fun isKtlintEnabled(): Boolean = enableKtlint

    fun isDetektEnabled(): Boolean = enableDetekt
}
