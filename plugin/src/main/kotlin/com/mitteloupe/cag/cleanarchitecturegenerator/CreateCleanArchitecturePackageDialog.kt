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
    private val enableComposeCheckBox = JCheckBox("Enable Compose", true)
    private val enableKtlintCheckBox = JCheckBox("Enable ktlint", false)
    private val enableDetektCheckBox = JCheckBox("Enable detekt", false)

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
