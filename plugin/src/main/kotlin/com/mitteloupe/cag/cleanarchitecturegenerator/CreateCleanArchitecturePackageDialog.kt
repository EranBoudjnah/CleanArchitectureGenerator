package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.panel
import javax.swing.JCheckBox
import javax.swing.JComponent

class CreateCleanArchitecturePackageDialog(
    project: Project
) : DialogWrapper(project) {
    private val enableComposeCheckBox = JCheckBox(CleanArchitectureGeneratorBundle.message("dialog.architecture.compose.label"), true)
    private val enableKtlintCheckBox = JCheckBox(CleanArchitectureGeneratorBundle.message("dialog.architecture.ktlint.label"), false)
    private val enableDetektCheckBox = JCheckBox(CleanArchitectureGeneratorBundle.message("dialog.architecture.detekt.label"), false)

    init {
        title = CleanArchitectureGeneratorBundle.message("info.architecture.generator.title")
        init()
    }

    override fun createCenterPanel(): JComponent =
        panel {
            row {
                cell(enableComposeCheckBox)
            }
            row {
                cell(enableKtlintCheckBox)
            }
            row {
                cell(enableDetektCheckBox)
            }
        }

    override fun doValidate(): ValidationInfo? = null

    fun isComposeEnabled(): Boolean = enableComposeCheckBox.isSelected

    fun isKtlintEnabled(): Boolean = enableKtlintCheckBox.isSelected

    fun isDetektEnabled(): Boolean = enableDetektCheckBox.isSelected
}
