package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class CreateCleanArchitectureFeatureDialog(
    project: Project?,
    defaultPrefix: String? = null
) : DialogWrapper(project) {
    private val featureNameTextField = JBTextField()

    val featureName: String
        get() = featureNameTextField.text.trim()

    init {
        title = CleanArchitectureGeneratorBundle.message("info.feature.generator.title")
        init()
        if (!defaultPrefix.isNullOrBlank()) {
            val placeholder = "FEATURE_NAME"
            featureNameTextField.text = "${defaultPrefix}feature.$placeholder"
            val endOfModuleIndex = featureNameTextField.text.length
            featureNameTextField.select(endOfModuleIndex - placeholder.length, endOfModuleIndex)
        }
    }

    override fun createCenterPanel(): JComponent {
        val formPanel: JPanel =
            FormBuilder.createFormBuilder()
                .addLabeledComponent(
                    CleanArchitectureGeneratorBundle.message("dialog.feature.name.label"),
                    featureNameTextField,
                    1,
                    false
                )
                .panel

        return formPanel
    }

    override fun doValidate(): ValidationInfo? {
        if (featureName.isEmpty()) {
            return ValidationInfo(
                CleanArchitectureGeneratorBundle.message("validation.feature.name.required"),
                featureNameTextField
            )
        }
        return null
    }
}
