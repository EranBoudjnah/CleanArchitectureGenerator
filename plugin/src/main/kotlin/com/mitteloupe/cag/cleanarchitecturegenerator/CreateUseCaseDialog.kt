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
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.OnChangeDocumentListener
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import com.mitteloupe.cag.cleanarchitecturegenerator.validation.SymbolValidator
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.io.File
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.SwingUtilities
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
    private var documentListenersSetup = false

    val useCaseNameWithSuffix: String
        get() = "$useCaseName$USE_CASE_SUFFIX"

    private val useCaseName: String
        get() = useCaseNameTextField.text.trim()

    val inputDataType: String?
        get() = (inputDataTypeComboBox.selectedItem as? String)?.trim()?.takeIf { it.isNotEmpty() }

    val outputDataType: String?
        get() = (outputDataTypeComboBox.selectedItem as? String)?.trim()?.takeIf { it.isNotEmpty() }

    val destinationDirectory: File?
        get() = directoryField.text.trim().takeIf { it.isNotEmpty() }?.let { File(it) }

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

    override fun getPreferredFocusedComponent(): JComponent = useCaseNameTextField

    override fun show() {
        super.show()

        SwingUtilities.invokeLater {
            validateFieldOnChange()
        }
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.name.label")) {
                cell(useCaseNameTextField)
                cell(
                    JBLabel(USE_CASE_SUFFIX).apply {
                        foreground = UIUtil.getLabelDisabledForeground()
                    }
                )
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.input.type.label")) {
                cell(inputDataTypeComboBox)
                cell(inputWarningLabel)
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.output.type.label")) {
                cell(outputDataTypeComboBox)
                cell(outputWarningLabel)
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.directory.field.label")) {
                cell(directoryField)
            }
        }
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

    private fun setupComboBoxes() {
        inputDataTypeComboBox.isEditable = true
        inputDataTypeComboBox.selectedItem = DEFAULT_DATA_TYPE
        outputDataTypeComboBox.isEditable = true
        outputDataTypeComboBox.selectedItem = DEFAULT_DATA_TYPE
    }

    private fun setupWarningLabels() {
        inputWarningLabel.clearWarning()
        outputWarningLabel.clearWarning()
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

    private fun setupDocumentListenersIfNeeded() {
        if (documentListenersSetup) {
            return
        }

        val inputEditor = inputDataTypeComboBox.editor.editorComponent as? JBTextField
        val outputEditor = outputDataTypeComboBox.editor.editorComponent as? JBTextField

        if (inputEditor != null && outputEditor != null) {
            inputEditor.validateOnChange()
            outputEditor.validateOnChange()

            documentListenersSetup = true
        }
    }

    private fun validateFieldOnChange() {
        validateInputField()
        validateOutputField()
    }

    private fun JBLabel.showFieldWarning(message: String) {
        icon = AllIcons.General.Warning
        text = message
    }

    private fun validateInputOutputTypes(): ValidationInfo? {
        val destinationDirectory = this@CreateUseCaseDialog.destinationDirectory ?: return null

        inputDataType?.let { inputType ->
            if (!symbolValidator.isValidSymbolSyntax(inputType)) {
                return ValidationInfo(
                    "Invalid type syntax: $inputType",
                    inputDataTypeComboBox
                ).asWarning()
            } else if (!symbolValidator.isValidSymbolInContext(inputType, destinationDirectory)) {
                return ValidationInfo(
                    CleanArchitectureGeneratorBundle.message("validation.usecase.input.type.not.found", inputType),
                    inputDataTypeComboBox
                ).asWarning()
            }
        }

        outputDataType?.let { outputType ->
            if (!symbolValidator.isValidSymbolSyntax(outputType)) {
                return ValidationInfo(
                    "Invalid type syntax: $outputType",
                    outputDataTypeComboBox
                ).asWarning()
            } else if (!symbolValidator.isValidSymbolInContext(outputType, destinationDirectory)) {
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

        if (inputType.isNullOrEmpty()) {
            inputWarningLabel.clearWarning()
            return
        }

        if (!symbolValidator.isValidSymbolSyntax(inputType)) {
            inputWarningLabel.showFieldWarning("Invalid type syntax: $inputType")
            return
        }

        val destinationDir = destinationDirectory
        if (destinationDir != null && !symbolValidator.isValidSymbolInContext(inputType, destinationDir)) {
            inputWarningLabel.showFieldWarning(SYMBOL_NOT_FOUND_ERROR_MESSAGE)
        } else {
            inputWarningLabel.clearWarning()
        }
    }

    private fun validateOutputField() {
        val outputDataType = outputDataType
        if (outputDataType.isNullOrEmpty()) {
            outputWarningLabel.clearWarning()
            return
        }

        if (!symbolValidator.isValidSymbolSyntax(outputDataType)) {
            outputWarningLabel.showFieldWarning("Invalid type syntax: $outputDataType")
            return
        }

        val destinationDirectory = this@CreateUseCaseDialog.destinationDirectory
        if (destinationDirectory != null && !symbolValidator.isValidSymbolInContext(outputDataType, destinationDirectory)) {
            outputWarningLabel.showFieldWarning(SYMBOL_NOT_FOUND_ERROR_MESSAGE)
        } else {
            outputWarningLabel.clearWarning()
        }
    }

    private fun JBLabel.clearWarning() {
        icon = null
        text = " "
    }

    private fun updateComboBoxOptions() {
        destinationDirectory?.let { destinationDirectory ->
            val modelClasses = modelClassFinder.findModelClasses(destinationDirectory)
            val allOptions = ModelClassFinder.PRIMITIVE_TYPES + modelClasses

            inputDataTypeComboBox.populateComboBox(allOptions)
            outputDataTypeComboBox.populateComboBox(allOptions)
        }
    }

    private fun <T> ComboBox<T>.populateComboBox(options: List<T>) {
        model = DefaultComboBoxModel<T>().apply { addAll(options) }
        selectedItem = DEFAULT_DATA_TYPE
    }

    private fun JBTextField.validateOnChange() {
        val validateOnChangeDocument = OnChangeDocumentListener { validateFieldOnChange() }
        val validateOnFocusLost =
            object : FocusAdapter() {
                override fun focusLost(event: FocusEvent) {
                    validateFieldOnChange()
                }
            }
        document.addDocumentListener(validateOnChangeDocument)
        addFocusListener(validateOnFocusLost)
    }
}
