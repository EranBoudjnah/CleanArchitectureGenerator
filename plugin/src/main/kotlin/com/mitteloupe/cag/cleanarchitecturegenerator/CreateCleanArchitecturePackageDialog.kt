package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class CreateCleanArchitecturePackageDialog(
    project: Project
) : DialogWrapper(project) {
    private var enableCompose: Boolean = true
    private var enableKtlint: Boolean = false
    private var enableDetekt: Boolean = false

    init {
        title = CleanArchitectureGeneratorBundle.message("info.architecture.generator.title")
        init()
    }

    override fun createCenterPanel(): JComponent =
        panel {
            row {
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
