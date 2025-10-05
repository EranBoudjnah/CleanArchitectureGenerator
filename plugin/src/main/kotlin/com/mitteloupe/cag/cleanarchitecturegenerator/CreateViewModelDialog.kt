package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import com.mitteloupe.cag.cleanarchitecturegenerator.validation.SymbolValidator
import java.awt.EventQueue.invokeLater
import java.io.File
import javax.swing.JComponent
import javax.swing.text.AbstractDocument

private const val VIEW_MODEL_SUFFIX = "ViewModel"
private const val DEFAULT_VIEW_MODEL_NAME = "My"

class CreateViewModelDialog(
    private val project: Project?,
    private val suggestedDirectory: File?
) : DialogWrapper(project) {
    private var viewModelNameText: String = ""
    private var directoryPath: String = ""
    private val symbolValidator = SymbolValidator()

    val viewModelNameWithSuffix: String
        get() = viewModelName.removeSuffix(VIEW_MODEL_SUFFIX) + VIEW_MODEL_SUFFIX

    private val viewModelName: String
        get() = viewModelNameText.trim()

    val destinationDirectory: File?
        get() =
            if (directoryPath.isNotEmpty()) {
                File(directoryPath)
            } else {
                null
            }

    init {
        title = CleanArchitectureGeneratorBundle.message("info.viewmodel.generator.title")
        init()
    }

    override fun createCenterPanel(): JComponent =
        panel {
            row(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.name.label")) {
                textField()
                    .bindText(::viewModelNameText)
                    .onChanged { viewModelNameText = it.text }
                    .applyToComponent {
                        invokeLater {
                            columns = 20
                            text = DEFAULT_VIEW_MODEL_NAME
                            selectAll()
                        }
                        toolTipText = CleanArchitectureGeneratorBundle.message("dialog.viewmodel.directory.tooltip")
                        (document as AbstractDocument).documentFilter = PredicateDocumentFilter { !it.isWhitespace() }
                    }
                label(VIEW_MODEL_SUFFIX)
                    .applyToComponent { foreground = UIUtil.getLabelDisabledForeground() }
            }

            row(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.directory.field.label")) {
                @Suppress("UnstableApiUsage")
                textFieldWithBrowseButton(
                    project = project,
                    fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                )
                    .bindText(::directoryPath)
                    .applyToComponent {
                        invokeLater { text = suggestedDirectory?.absolutePath.orEmpty() }
                        toolTipText = CleanArchitectureGeneratorBundle.message("dialog.viewmodel.directory.tooltip")
                    }
            }
        }

    override fun doValidate(): ValidationInfo? {
        if (viewModelName.isEmpty()) {
            return ValidationInfo(CleanArchitectureGeneratorBundle.message("validation.viewmodel.name.required"))
        }

        if (!symbolValidator.isValidSymbolSyntax(viewModelName)) {
            return ValidationInfo(CleanArchitectureGeneratorBundle.message("validation.viewmodel.name.invalid", viewModelName))
        }

        val directory = destinationDirectory
        if (directory != null && !directory.exists()) {
            return ValidationInfo(
                CleanArchitectureGeneratorBundle.message("validation.viewmodel.directory.not.exists", directory.absolutePath)
            )
        }

        return null
    }
}
