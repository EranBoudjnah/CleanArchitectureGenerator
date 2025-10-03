package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import com.mitteloupe.cag.cleanarchitecturegenerator.validation.SymbolValidator
import java.io.File
import javax.swing.JComponent
import javax.swing.text.AbstractDocument

private const val VIEW_MODEL_SUFFIX = "ViewModel"
private const val DEFAULT_VIEW_MODEL_NAME = "My"

class CreateViewModelDialog(
    project: Project?,
    suggestedDirectory: File?
) : DialogWrapper(project) {
    private val viewModelNameTextField = JBTextField()
    private val directoryField = TextFieldWithBrowseButton()
    private val symbolValidator = SymbolValidator()

    val viewModelNameWithSuffix: String
        get() = viewModelName.removeSuffix(VIEW_MODEL_SUFFIX) + VIEW_MODEL_SUFFIX

    private val viewModelName: String
        get() = viewModelNameTextField.text.trim()

    val destinationDirectory: File?
        get() = if (directoryField.text.isNotEmpty()) File(directoryField.text) else null

    init {
        title = CleanArchitectureGeneratorBundle.message("info.viewmodel.generator.title")
        init()

        viewModelNameTextField.columns = 20
        directoryField.text = suggestedDirectory?.absolutePath.orEmpty()
        directoryField.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            val initialDirectory = LocalFileSystem.getInstance().findFileByIoFile(File(directoryField.text))
            val chosen = FileChooser.chooseFile(descriptor, project, initialDirectory)
            if (chosen != null) {
                directoryField.text = chosen.path
            }
        }

        viewModelNameTextField.text = DEFAULT_VIEW_MODEL_NAME
        viewModelNameTextField.selectAll()

        (viewModelNameTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }
    }

    override fun createCenterPanel(): JComponent =
        panel {
            row(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.name.label")) {
                textField()
                    .bindText(viewModelNameTextField::getText, viewModelNameTextField::setText)
                label(VIEW_MODEL_SUFFIX)
                    .applyToComponent { foreground = UIUtil.getLabelDisabledForeground() }
            }

            row(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.directory.field.label")) {
                cell(directoryField)
                    .comment(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.directory.comment"))
                    .align(Align.FILL)
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
