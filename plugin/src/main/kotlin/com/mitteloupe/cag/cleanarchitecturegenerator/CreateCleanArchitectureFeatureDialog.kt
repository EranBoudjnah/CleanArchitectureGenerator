package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AbstractDocument

private const val PLACEHOLDER = "FEATURE_NAME"

class CreateCleanArchitectureFeatureDialog(
    project: Project?,
    defaultPrefix: String? = null
) : DialogWrapper(project) {
    private val defaultPackagePrefix = defaultPrefix?.let { "${it}feature." }
    private val defaultPackageName =
        if (defaultPrefix == null) {
            null
        } else {
            "${defaultPrefix}feature.$PLACEHOLDER"
        }

    private val featureNameTextField = JBTextField()
    private val featurePackageTextField = JBTextField()
    private var lastFeatureName: String = PLACEHOLDER

    val featureName: String
        get() = featureNameTextField.text.trim()

    init {
        title = CleanArchitectureGeneratorBundle.message("info.feature.generator.title")
        init()
        featureNameTextField.text = PLACEHOLDER
        featureNameTextField.selectAll()

        (featureNameTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }
        if (!defaultPackageName.isNullOrBlank()) {
            featurePackageTextField.text = defaultPackageName
            val endOfModuleIndex = featurePackageTextField.text.length
            featurePackageTextField.select(endOfModuleIndex - PLACEHOLDER.length, endOfModuleIndex)
        }

        featureNameTextField.document.addDocumentListener(
            object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent) = onNameChanged()

                override fun removeUpdate(e: DocumentEvent) = onNameChanged()

                override fun changedUpdate(e: DocumentEvent) = onNameChanged()

                private fun onNameChanged() {
                    if (defaultPackagePrefix.isNullOrBlank()) return

                    if (isFollowingDefaultPackage(lastFeatureName)) {
                        featurePackageTextField.text = defaultPackagePrefix + featureName
                    }
                    lastFeatureName = featureName
                }
            }
        )

        featurePackageTextField.document.addDocumentListener(
            object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent) = onPackageChanged()

                override fun removeUpdate(e: DocumentEvent) = onPackageChanged()

                override fun changedUpdate(e: DocumentEvent) = onPackageChanged()

                private fun onPackageChanged() {
                    if (defaultPackagePrefix.isNullOrBlank()) return
                }
            }
        )
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
                .addLabeledComponent(
                    CleanArchitectureGeneratorBundle.message("dialog.feature.package.label"),
                    featurePackageTextField,
                    1,
                    false
                )
                .panel

        return formPanel
    }

    override fun doValidate(): ValidationInfo? =
        if (featureName.isEmpty()) {
            ValidationInfo(
                CleanArchitectureGeneratorBundle.message("validation.feature.name.required"),
                featureNameTextField
            )
        } else {
            null
        }

    private fun isFollowingDefaultPackage(lastFeatureName: String) = featurePackageTextField.text == defaultPackagePrefix + lastFeatureName
}
