package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import java.io.File
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.text.AbstractDocument

private const val USE_CASE_SUFFIX = "UseCase"

class CreateUseCaseDialog(
    project: Project?,
    suggestedDirectory: File?
) : DialogWrapper(project) {
    private val useCaseNameTextField = JBTextField()
    private val directoryField = TextFieldWithBrowseButton()

    val useCaseNameWithSuffix: String
        get() = "$useCaseName$USE_CASE_SUFFIX"

    private val useCaseName: String
        get() = useCaseNameTextField.text.trim()

    init {
        title = CleanArchitectureGeneratorBundle.message("info.usecase.generator.title")
        init()

        useCaseNameTextField.columns = 20
        directoryField.text = suggestedDirectory?.absolutePath ?: ""
        directoryField.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            val initialDirectory = LocalFileSystem.getInstance().findFileByIoFile(File(directoryField.text))
            val chosen = FileChooser.chooseFile(descriptor, project, initialDirectory)
            if (chosen != null) {
                directoryField.text = chosen.path
            }
        }

        (useCaseNameTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }
    }

    override fun getPreferredFocusedComponent(): JComponent = useCaseNameTextField

    override fun createCenterPanel(): JComponent {
        val suffixLabel =
            JBLabel(USE_CASE_SUFFIX).apply {
                foreground = UIUtil.getLabelDisabledForeground()
            }

        val nameWithSuffixPanel =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                add(useCaseNameTextField)
                add(Box.createHorizontalStrut(4))
                add(suffixLabel)
            }

        val formPanel: JPanel =
            FormBuilder.createFormBuilder()
                .addLabeledComponent(
                    CleanArchitectureGeneratorBundle.message("dialog.usecase.name.label"),
                    nameWithSuffixPanel,
                    1,
                    false
                )
                .addLabeledComponent(
                    CleanArchitectureGeneratorBundle.message("dialog.usecase.directory.field.label"),
                    directoryField,
                    1,
                    false
                )
                .panel

        return formPanel
    }

    override fun doValidate(): ValidationInfo? =
        if (useCaseName.isEmpty()) {
            ValidationInfo(
                CleanArchitectureGeneratorBundle.message("validation.usecase.name.required"),
                useCaseNameTextField
            )
        } else {
            null
        }

    val destinationDirectory: File?
        get() = directoryField.text.trim().takeIf { it.isNotEmpty() }?.let { File(it) }
}
