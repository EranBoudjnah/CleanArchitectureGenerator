package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.text.AbstractDocument

private const val DATA_SOURCE_SUFFIX = "DataSource"

class CreateDataSourceDialog(
    project: Project?
) : DialogWrapper(project) {
    private val dataSourceNameTextField = JBTextField()

    val dataSourceNameWithSuffix: String
        get() = "$dataSourceName$DATA_SOURCE_SUFFIX"

    private val dataSourceName: String
        get() = dataSourceNameTextField.text.trim()

    private var useKtorInternal: Boolean = false
    val useKtor: Boolean
        get() = useKtorInternal

    private var useRetrofitInternal: Boolean = false
    val useRetrofit: Boolean
        get() = useRetrofitInternal

    init {
        title = CleanArchitectureGeneratorBundle.message("info.datasource.generator.title")
        init()

        dataSourceNameTextField.columns = 20

        (dataSourceNameTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }
    }

    override fun getPreferredFocusedComponent(): JComponent = dataSourceNameTextField

    override fun createCenterPanel(): JComponent {
        val suffixLabel =
            JBLabel(DATA_SOURCE_SUFFIX).apply {
                foreground = UIUtil.getLabelDisabledForeground()
            }

        val nameWithSuffixPanel =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                add(dataSourceNameTextField)
                add(Box.createHorizontalStrut(4))
                add(suffixLabel)
            }

        return panel {
            row(CleanArchitectureGeneratorBundle.message("dialog.datasource.name.label")) {
                cell(nameWithSuffixPanel)
            }
            row {
                checkBox("Add Ktor dependencies")
                    .bindSelected(::useKtorInternal)
            }
            row {
                checkBox("Add Retrofit dependencies")
                    .bindSelected(::useRetrofitInternal)
            }
        }
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
