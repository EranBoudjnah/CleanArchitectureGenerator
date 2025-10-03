package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import java.awt.EventQueue.invokeLater
import javax.swing.JComponent
import javax.swing.text.AbstractDocument

private const val DATA_SOURCE_SUFFIX = "DataSource"

class CreateDataSourceDialog(
    project: Project?
) : DialogWrapper(project) {
    private lateinit var dataSourceNameTextField: JBTextField

    val dataSourceNameWithSuffix: String
        get() = "$dataSourceName$DATA_SOURCE_SUFFIX"

    private var dataSourceName: String = ""

    private var useKtorInternal: Boolean = false
    val useKtor: Boolean
        get() = useKtorInternal

    private var useRetrofitInternal: Boolean = false
    val useRetrofit: Boolean
        get() = useRetrofitInternal

    init {
        title = CleanArchitectureGeneratorBundle.message("info.datasource.generator.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val suffixLabel =
            JBLabel(DATA_SOURCE_SUFFIX).apply {
                foreground = UIUtil.getLabelDisabledForeground()
            }

        return panel {
            row(CleanArchitectureGeneratorBundle.message("dialog.datasource.name.label")) {
                textField()
                    .bindText({ dataSourceName }, { dataSourceName = it })
                    .applyToComponent {
                        (document as AbstractDocument).documentFilter =
                            PredicateDocumentFilter { !it.isWhitespace() }
                        dataSourceNameTextField = this
                    }
                cell(suffixLabel)
            }
            row {
                checkBox("Add Ktor dependencies")
                    .bindSelected(::useKtorInternal)
            }
            row {
                checkBox("Add Retrofit dependencies")
                    .bindSelected(::useRetrofitInternal)
            }
        }.apply {
            invokeLater { dataSourceNameTextField.requestFocusInWindow() }
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
