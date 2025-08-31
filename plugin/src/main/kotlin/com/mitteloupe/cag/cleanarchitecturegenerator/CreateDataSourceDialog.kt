package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.text.AbstractDocument

class CreateDataSourceDialog(
    project: Project?
) : DialogWrapper(project) {
    private val dataSourceNameTextField = JBTextField()
    private val ktorCheckBox = JBCheckBox("Add Ktor dependencies")
    private val retrofitCheckBox = JBCheckBox("Add Retrofit dependencies")

    val dataSourceNameWithSuffix: String
        get() = "${dataSourceName}DataSource"

    private val dataSourceName: String
        get() = dataSourceNameTextField.text.trim()

    val useKtor: Boolean
        get() = ktorCheckBox.isSelected

    val useRetrofit: Boolean
        get() = retrofitCheckBox.isSelected

    init {
        title = CleanArchitectureGeneratorBundle.message("info.datasource.generator.title")
        init()

        dataSourceNameTextField.columns = 20

        (dataSourceNameTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }
    }

    override fun getPreferredFocusedComponent(): JComponent? = dataSourceNameTextField

    override fun createCenterPanel(): JComponent {
        val suffixLabel =
            JBLabel("DataSource").apply {
                foreground = UIUtil.getLabelDisabledForeground()
            }

        val nameWithSuffixPanel =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                add(dataSourceNameTextField)
                add(Box.createHorizontalStrut(4))
                add(suffixLabel)
            }

        val formPanel: JPanel =
            FormBuilder.createFormBuilder()
                .addLabeledComponent(
                    CleanArchitectureGeneratorBundle.message("dialog.datasource.name.label"),
                    nameWithSuffixPanel,
                    1,
                    false
                )
                .addComponent(ktorCheckBox)
                .addComponent(retrofitCheckBox)
                .panel

        return formPanel
    }

    override fun doValidate(): ValidationInfo? =
        if (dataSourceName.isEmpty()) {
            ValidationInfo(
                CleanArchitectureGeneratorBundle.message("validation.datasource.name.required"),
                dataSourceNameTextField
            )
        } else {
            null
        }
}
