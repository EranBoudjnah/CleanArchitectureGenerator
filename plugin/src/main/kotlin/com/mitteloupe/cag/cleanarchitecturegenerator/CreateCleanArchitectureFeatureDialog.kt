package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.mitteloupe.cag.cleanarchitecturegenerator.form.OnChangeDocumentListener
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import java.io.File
import javax.swing.JComponent
import javax.swing.text.AbstractDocument

private const val PLACEHOLDER = "FEATURE_NAME"

class CreateCleanArchitectureFeatureDialog(
    project: Project?,
    private val defaultPackagePrefix: String? = null,
    private val appModuleDirectories: List<File>
) : DialogWrapper(project) {
    private val featureNameTextField = JBTextField()
    private val featurePackageTextField = JBTextField()
    private var lastFeatureName: String = PLACEHOLDER

    val featureName: String
        get() = featureNameTextField.text

    val featurePackageName: String
        get() = featurePackageTextField.text.trim()

    private var appModuleSelectedIndex: Int = 0

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

    private var enableKtlintInternal: Boolean = false
    val enableKtlint: Boolean
        get() = enableKtlintInternal

    private var enableDetektInternal: Boolean = false
    val enableDetekt: Boolean
        get() = enableDetektInternal

    init {
        title = CleanArchitectureGeneratorBundle.message("info.feature.generator.title")
        init()
        featureNameTextField.text = PLACEHOLDER
        featureNameTextField.selectAll()

        (featureNameTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }
        if (!defaultPackagePrefix.isNullOrBlank()) {
            updateTemplatedPackageName()
        }

        featureNameTextField.document.addDocumentListener(
            OnChangeDocumentListener {
                if (defaultPackagePrefix.isNullOrBlank()) {
                    return@OnChangeDocumentListener
                }

                if (isFollowingFeatureName(lastFeatureName)) {
                    updateTemplatedPackageName(currentPackagePrefix() + ".")
                }
                lastFeatureName = featureName
            }
        )

        (featurePackageTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }

        featurePackageTextField.document.addDocumentListener(
            OnChangeDocumentListener {
                if (defaultPackagePrefix.isNullOrBlank()) {
                    return@OnChangeDocumentListener
                }
            }
        )
    }

    override fun getPreferredFocusedComponent(): JComponent = featureNameTextField

    private fun updateTemplatedPackageName(packagePrefix: String? = defaultPackagePrefix) {
        featurePackageTextField.text = "$packagePrefix${featureName.lowercase()}"
        val endOfModuleIndex = featurePackageTextField.text.length
        featurePackageTextField.select(endOfModuleIndex - featureName.length, endOfModuleIndex)
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
            row(CleanArchitectureGeneratorBundle.message("dialog.feature.name.label")) {
                cell(featureNameTextField)
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.feature.package.label")) {
                cell(featurePackageTextField)
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.feature.code.quality.label")) {
                checkBox("ktlint").bindSelected(::enableKtlintInternal)
                checkBox("detekt").bindSelected(::enableDetektInternal)
            }
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

    private fun isFollowingFeatureName(lastFeatureName: String) =
        featurePackageTextField.text.substringAfterLast('.') == lastFeatureName.lowercase()

    private fun currentPackagePrefix() = featurePackageTextField.text.substringBeforeLast('.')
}
