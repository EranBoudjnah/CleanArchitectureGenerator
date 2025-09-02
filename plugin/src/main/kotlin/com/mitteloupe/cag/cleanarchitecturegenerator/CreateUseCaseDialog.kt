package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.OnChangeDocumentListener
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import com.mitteloupe.cag.cleanarchitecturegenerator.validation.SymbolValidator
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.io.File
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.text.AbstractDocument

private const val USE_CASE_SUFFIX = "UseCase"
private const val DEFAULT_USE_CASE_NAME = "DoSomething"
private const val DEFAULT_DATA_TYPE = "Unit"
private const val SYMBOL_NOT_FOUND_ERROR_MESSAGE = "Symbol not found."

class CreateUseCaseDialog(
    project: Project?,
    suggestedDirectory: File?
) : DialogWrapper(project) {
    private val useCaseNameTextField = JBTextField()
    private val directoryField = TextFieldWithBrowseButton()
    private val inputDataTypeComboBox = ComboBox<String>()
    private val outputDataTypeComboBox = ComboBox<String>()
    private val modelClassFinder = ModelClassFinder()
    private val symbolValidator = SymbolValidator()

    private val inputWarningLabel = JBLabel()
    private val outputWarningLabel = JBLabel()

    val useCaseNameWithSuffix: String
        get() = "$useCaseName$USE_CASE_SUFFIX"

    private val useCaseName: String
        get() = useCaseNameTextField.text.trim()

    val inputDataType: String?
        get() = (inputDataTypeComboBox.selectedItem as? String)?.trim()?.takeIf { it.isNotEmpty() }

    val outputDataType: String?
        get() = (outputDataTypeComboBox.selectedItem as? String)?.trim()?.takeIf { it.isNotEmpty() }

    init {
        title = CleanArchitectureGeneratorBundle.message("info.usecase.generator.title")
        init()

        useCaseNameTextField.columns = 20
        directoryField.text = suggestedDirectory?.absolutePath.orEmpty()
        directoryField.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            val initialDirectory = LocalFileSystem.getInstance().findFileByIoFile(File(directoryField.text))
            val chosen = FileChooser.chooseFile(descriptor, project, initialDirectory)
            if (chosen != null) {
                directoryField.text = chosen.path
                updateComboBoxOptions()
            }
        }

        useCaseNameTextField.text = DEFAULT_USE_CASE_NAME
        useCaseNameTextField.selectAll()

        (useCaseNameTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }

        setupComboBoxes()
        updateComboBoxOptions()
        setupWarningLabels()
        setupValidationListeners()
    }

    private fun setupComboBoxes() {
        inputDataTypeComboBox.isEditable = true
        inputDataTypeComboBox.selectedItem = DEFAULT_DATA_TYPE
        outputDataTypeComboBox.isEditable = true
        outputDataTypeComboBox.selectedItem = DEFAULT_DATA_TYPE
    }

    private fun setupWarningLabels() {
        clearInputWarning()
        clearOutputWarning()
    }

    private fun setupValidationListeners() {
        inputDataTypeComboBox.addActionListener {
            validateFieldOnChange()
        }

        outputDataTypeComboBox.addActionListener {
            validateFieldOnChange()
        }

        val focusAdapter =
            object : FocusAdapter() {
                override fun focusGained(event: FocusEvent) {
                    setupDocumentListenersIfNeeded()
                }

                override fun focusLost(event: FocusEvent) {
                    validateFieldOnChange()
                }
            }

        inputDataTypeComboBox.addFocusListener(focusAdapter)
        outputDataTypeComboBox.addFocusListener(focusAdapter)
    }

    private var documentListenersSetup = false

    private fun setupDocumentListenersIfNeeded() {
        if (documentListenersSetup) return

        val inputEditor = inputDataTypeComboBox.editor.editorComponent as? JBTextField
        val outputEditor = outputDataTypeComboBox.editor.editorComponent as? JBTextField

        if (inputEditor != null && outputEditor != null) {
            inputEditor.document.addDocumentListener(
                OnChangeDocumentListener {
                    validateFieldOnChange()
                }
            )

            outputEditor.document.addDocumentListener(
                OnChangeDocumentListener {
                    validateFieldOnChange()
                }
            )

            inputEditor.addFocusListener(
                object : FocusAdapter() {
                    override fun focusLost(event: FocusEvent) {
                        validateFieldOnChange()
                    }
                }
            )

            outputEditor.addFocusListener(
                object : FocusAdapter() {
                    override fun focusLost(event: FocusEvent) {
                        validateFieldOnChange()
                    }
                }
            )

            documentListenersSetup = true
        }
    }

    private fun validateFieldOnChange() {
        validateInputField()
        validateOutputField()
    }

    private fun showFieldWarning(
        component: JComponent?,
        message: String
    ) {
        val label =
            when (component) {
                inputDataTypeComboBox -> inputWarningLabel
                outputDataTypeComboBox -> outputWarningLabel
                else -> return
            }
        label.icon = AllIcons.General.Warning
        label.text = message
    }

    private fun validateInputOutputTypes(): ValidationInfo? {
        val destinationDir = destinationDirectory ?: return null

        val inputType = inputDataType
        val outputType = outputDataType

        if (inputType != null) {
            if (!symbolValidator.isValidSymbolSyntax(inputType)) {
                return ValidationInfo(
                    "Invalid type syntax: $inputType",
                    inputDataTypeComboBox
                ).asWarning()
            } else if (!symbolValidator.isValidSymbolInContext(inputType, destinationDir)) {
                return ValidationInfo(
                    CleanArchitectureGeneratorBundle.message("validation.usecase.input.type.not.found", inputType),
                    inputDataTypeComboBox
                ).asWarning()
            }
        }

        if (outputType != null) {
            if (!symbolValidator.isValidSymbolSyntax(outputType)) {
                return ValidationInfo(
                    "Invalid type syntax: $outputType",
                    outputDataTypeComboBox
                ).asWarning()
            } else if (!symbolValidator.isValidSymbolInContext(outputType, destinationDir)) {
                return ValidationInfo(
                    CleanArchitectureGeneratorBundle.message("validation.usecase.output.type.not.found", outputType),
                    outputDataTypeComboBox
                ).asWarning()
            }
        }

        return null
    }

    private fun validateInputField() {
        val inputType = inputDataType

        if (inputType == null || inputType.isEmpty()) {
            clearInputWarning()
            return
        }

        if (!symbolValidator.isValidSymbolSyntax(inputType)) {
            showFieldWarning(inputDataTypeComboBox, "Invalid type syntax: $inputType")
            return
        }

        val destinationDir = destinationDirectory
        if (destinationDir != null && !symbolValidator.isValidSymbolInContext(inputType, destinationDir)) {
            showFieldWarning(inputDataTypeComboBox, SYMBOL_NOT_FOUND_ERROR_MESSAGE)
        } else {
            clearInputWarning()
        }
    }

    private fun validateOutputField() {
        val outputType = outputDataType

        if (outputType == null || outputType.isEmpty()) {
            clearOutputWarning()
            return
        }

        if (!symbolValidator.isValidSymbolSyntax(outputType)) {
            showFieldWarning(outputDataTypeComboBox, "Invalid type syntax: $outputType")
            return
        }

        val destinationDir = destinationDirectory
        if (destinationDir != null && !symbolValidator.isValidSymbolInContext(outputType, destinationDir)) {
            showFieldWarning(outputDataTypeComboBox, SYMBOL_NOT_FOUND_ERROR_MESSAGE)
        } else {
            clearOutputWarning()
        }
    }

    private fun clearInputWarning() {
        inputWarningLabel.icon = null
        inputWarningLabel.text = " "
    }

    private fun clearOutputWarning() {
        outputWarningLabel.icon = null
        outputWarningLabel.text = " "
    }

    private fun updateComboBoxOptions() {
        val destinationDirectory = destinationDirectory
        if (destinationDirectory != null) {
            val modelClasses = modelClassFinder.findModelClasses(destinationDirectory)
            val allOptions = ModelClassFinder.PRIMITIVE_TYPES + modelClasses

            val inputModel = DefaultComboBoxModel<String>()
            val outputModel = DefaultComboBoxModel<String>()

            allOptions.forEach { option ->
                inputModel.addElement(option)
                outputModel.addElement(option)
            }

            inputDataTypeComboBox.model = inputModel
            outputDataTypeComboBox.model = outputModel

            inputDataTypeComboBox.selectedItem = DEFAULT_DATA_TYPE
            outputDataTypeComboBox.selectedItem = DEFAULT_DATA_TYPE
        }
    }

    override fun getPreferredFocusedComponent(): JComponent = useCaseNameTextField

    override fun show() {
        super.show()

        javax.swing.SwingUtilities.invokeLater {
            validateFieldOnChange()
        }
    }

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

        val inputTypePanel =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                add(inputDataTypeComboBox)
                add(Box.createHorizontalStrut(8))
                add(
                    inputWarningLabel.apply {
                        preferredSize = java.awt.Dimension(150, preferredSize.height)
                        minimumSize = java.awt.Dimension(150, minimumSize.height)
                    }
                )
            }

        val outputTypePanel =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                add(outputDataTypeComboBox)
                add(Box.createHorizontalStrut(8))
                add(
                    outputWarningLabel.apply {
                        preferredSize = java.awt.Dimension(150, preferredSize.height)
                        minimumSize = java.awt.Dimension(150, minimumSize.height)
                    }
                )
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
                    CleanArchitectureGeneratorBundle.message("dialog.usecase.input.type.label"),
                    inputTypePanel,
                    1,
                    false
                )
                .addLabeledComponent(
                    CleanArchitectureGeneratorBundle.message("dialog.usecase.output.type.label"),
                    outputTypePanel,
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
            validateInputOutputTypes()
        }

    val destinationDirectory: File?
        get() = directoryField.text.trim().takeIf { it.isNotEmpty() }?.let { File(it) }
}
