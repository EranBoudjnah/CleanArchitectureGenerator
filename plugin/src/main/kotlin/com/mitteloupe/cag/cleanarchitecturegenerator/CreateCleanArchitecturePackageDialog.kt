package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.panel
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JTextField

class CreateCleanArchitecturePackageDialog(
    project: Project,
    defaultPrefix: String
) : DialogWrapper(project) {
    private val packageField = JTextField(defaultPrefix + "architecture")
    private val enableComposeCheckBox = JCheckBox("Enable Compose", true)

    init {
        title = CleanArchitectureGeneratorBundle.message("info.architecture.generator.title")
        init()
    }

    override fun createCenterPanel(): JComponent =
        panel {
            row {
                label(CleanArchitectureGeneratorBundle.message("dialog.architecture.package.label"))
                cell(packageField).focused()
            }
            row {
                cell(enableComposeCheckBox)
            }
        }

    override fun doValidate(): ValidationInfo? {
        val packageName = packageField.text.trim()
        if (packageName.isEmpty()) {
            return ValidationInfo(
                CleanArchitectureGeneratorBundle.message("validation.architecture.package.required"),
                packageField
            )
        }

        return null
    }

    fun getPackageName(): String = packageField.text.trim()

    fun isComposeEnabled(): Boolean = enableComposeCheckBox.isSelected
}
