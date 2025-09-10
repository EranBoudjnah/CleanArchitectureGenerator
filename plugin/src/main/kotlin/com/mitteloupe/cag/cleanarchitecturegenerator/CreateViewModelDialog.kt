package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import com.mitteloupe.cag.cleanarchitecturegenerator.validation.SymbolValidator
import java.io.File
import javax.swing.JComponent
import javax.swing.text.AbstractDocument

private val VIEW_MODEL_SUFFIX = CleanArchitectureGeneratorBundle.message("constants.viewmodel.suffix")
private val DEFAULT_VIEW_MODEL_NAME = CleanArchitectureGeneratorBundle.message("constants.viewmodel.default.name")

class CreateViewModelDialog(
    project: Project?,
    suggestedDirectory: File?
) : DialogWrapper(project) {
    private val viewModelNameTextField = JBTextField()
    private val directoryField = TextFieldWithBrowseButton()
    private val featurePackageNameTextField = JBTextField()
    private val projectNamespaceTextField = JBTextField()
    private val symbolValidator = SymbolValidator()

    val viewModelNameWithSuffix: String
        get() = "$viewModelName$VIEW_MODEL_SUFFIX"

    private val viewModelName: String
        get() = viewModelNameTextField.text.trim()

    val featurePackageName: String
        get() = featurePackageNameTextField.text.trim()

    val projectNamespace: String
        get() = projectNamespaceTextField.text.trim()

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

        featurePackageNameTextField.text = CleanArchitectureGeneratorBundle.message("constants.viewmodel.default.feature.package")
        projectNamespaceTextField.text = CleanArchitectureGeneratorBundle.message("constants.viewmodel.default.project.namespace")
    }

    override fun createCenterPanel(): JComponent =
        panel {
            row(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.name.label")) {
                cell(viewModelNameTextField)
                    .comment(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.name.comment"))
            }

            row(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.directory.field.label")) {
                cell(directoryField)
                    .comment(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.directory.comment"))
            }

            row(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.feature.package.label")) {
                cell(featurePackageNameTextField)
                    .comment(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.feature.package.comment"))
            }

            row(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.project.namespace.label")) {
                cell(projectNamespaceTextField)
                    .comment(CleanArchitectureGeneratorBundle.message("dialog.viewmodel.project.namespace.comment"))
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

        if (featurePackageName.isEmpty()) {
            return ValidationInfo(CleanArchitectureGeneratorBundle.message("validation.viewmodel.feature.package.required"))
        }

        if (!isValidPackageName(featurePackageName)) {
            return ValidationInfo(
                CleanArchitectureGeneratorBundle.message("validation.viewmodel.feature.package.invalid", featurePackageName)
            )
        }

        if (projectNamespace.isEmpty()) {
            return ValidationInfo(CleanArchitectureGeneratorBundle.message("validation.viewmodel.project.namespace.required"))
        }

        if (!isValidPackageName(projectNamespace)) {
            return ValidationInfo(
                CleanArchitectureGeneratorBundle.message("validation.viewmodel.project.namespace.invalid", projectNamespace)
            )
        }

        return null
    }

    private fun isValidPackageName(packageName: String): Boolean {
        if (packageName.isEmpty()) {
            return false
        }
        return packageName.split(".").all { part ->
            part.isNotEmpty() && part.all { it.isLetterOrDigit() || it == '_' } &&
                part.first().isLetter()
        }
    }
}
