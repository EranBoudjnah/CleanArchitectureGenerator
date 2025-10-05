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
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.whenItemChangedFromUi
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.OnChangeDocumentListener
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import com.mitteloupe.cag.cleanarchitecturegenerator.validation.SymbolValidator
import java.awt.EventQueue.invokeLater
import java.io.File
import javax.swing.DefaultComboBoxModel
import javax.swing.JTextField
import javax.swing.text.AbstractDocument

private const val USE_CASE_SUFFIX = "UseCase"
private const val DEFAULT_USE_CASE_NAME = "DoSomething"
private const val DEFAULT_DATA_TYPE = "Unit"

class CreateUseCaseDialog(
    project: Project?,
    suggestedDirectory: File?
) : DialogWrapper(project) {
    private val useCaseNameTextField = JBTextField()
    private val directoryField = TextFieldWithBrowseButton()
    private lateinit var inputDataTypeComboBox: ComboBox<String>
    private val inputWarningLabel = JBLabel()
    private lateinit var outputDataTypeComboBox: ComboBox<String>
    private val outputWarningLabel = JBLabel()
    private val modelClassFinder = ModelClassFinder()
    private val symbolValidator = SymbolValidator()
    private val inputDataTypeModel = DefaultComboBoxModel<String>()
    private val outputDataTypeModel = DefaultComboBoxModel<String>()

    val useCaseNameWithSuffix: String
        get() = useCaseName.removeSuffix(USE_CASE_SUFFIX) + USE_CASE_SUFFIX

    private val useCaseName: String
        get() = useCaseNameTextField.text.trim()

    private var _inputDataType: String? = null
    val inputDataType: String?
        get() = _inputDataType

    private var _outputDataType: String? = null
    val outputDataType: String?
        get() = _outputDataType

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
                setupDataTypeComboBoxes()
            }
        }

        useCaseNameTextField.text = DEFAULT_USE_CASE_NAME
        useCaseNameTextField.selectAll()

        (useCaseNameTextField.document as AbstractDocument).documentFilter =
            PredicateDocumentFilter { !it.isWhitespace() }

        setupDataTypeComboBoxes()
        setupWarningLabels()
    }

    override fun getPreferredFocusedComponent() = useCaseNameTextField

    override fun show() {
        super.show()

        invokeLater {
            validateFieldOnChange()
        }
    }

    override fun createCenterPanel() =
        panel {
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.name.label")) {
                textField()
                    .bindText(useCaseNameTextField::getText, useCaseNameTextField::setText)
                cell(
                    JBLabel(USE_CASE_SUFFIX).apply {
                        foreground = UIUtil.getLabelDisabledForeground()
                    }
                )
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.input.type.label")) {
                @Suppress("UnstableApiUsage")
                comboBox(inputDataTypeModel)
                    .whenItemChangedFromUi { _inputDataType = it }
                    .applyToComponent {
                        isEditable = true
                        (editor.editorComponent as JTextField).validateOnChange {
                            _inputDataType = text
                        }
                        selectedItem = DEFAULT_DATA_TYPE
                        inputDataTypeComboBox = this
                    }
                cell(inputWarningLabel)
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.output.type.label")) {
                @Suppress("UnstableApiUsage")
                comboBox(outputDataTypeModel)
                    .whenItemChangedFromUi { _outputDataType = it }
                    .applyToComponent {
                        isEditable = true
                        (editor.editorComponent as JTextField).validateOnChange {
                            _inputDataType = text
                        }
                        addActionListener {
                            validateFieldOnChange()
                        }
                        selectedItem = DEFAULT_DATA_TYPE
                        outputDataTypeComboBox = this
                    }
                cell(outputWarningLabel)
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.directory.field.label")) {
                cell(directoryField)
                    .comment(CleanArchitectureGeneratorBundle.message("dialog.usecase.directory.comment"))
                    .align(Align.FILL)
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

    private fun setupWarningLabels() {
        inputWarningLabel.clearWarning()
        outputWarningLabel.clearWarning()
    }

    private fun validateFieldOnChange() {
        inputDataType.validateDataType(inputWarningLabel)
        outputDataType.validateDataType(outputWarningLabel)
    }

    private fun validateInputOutputTypes(): ValidationInfo? {
        val destinationDirectory = destinationDirectory ?: return null

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

    private fun String?.validateDataType(warningLabel: JBLabel) {
        if (isNullOrEmpty()) {
            warningLabel.clearWarning()
            return
        }

        if (!symbolValidator.isValidSymbolSyntax(this)) {
            warningLabel.showFieldWarning("Invalid type syntax: $this")
            return
        }

        val destinationDirectory = destinationDirectory
        if (destinationDirectory != null && !symbolValidator.isValidSymbolInContext(this, destinationDirectory)) {
            warningLabel.showFieldWarning(
                CleanArchitectureGeneratorBundle.message("error.symbol.not.found")
            )
        } else {
            warningLabel.clearWarning()
        }
    }

    private fun JBLabel.clearWarning() {
        icon = null
        text = " "
    }

    private fun JBLabel.showFieldWarning(message: String) {
        icon = AllIcons.General.Warning
        text = message
    }

    private fun setupDataTypeComboBoxes() {
        destinationDirectory?.let { destinationDirectory ->
            val modelClasses = modelClassFinder.findModelClasses(destinationDirectory)
            val allOptions = ModelClassFinder.PRIMITIVE_TYPES + modelClasses

            inputDataTypeModel.addAll(allOptions)
            outputDataTypeModel.addAll(allOptions)
        }
    }

    private fun JTextField.validateOnChange(onChange: JTextField.() -> Unit) {
        document.addDocumentListener(
            OnChangeDocumentListener {
                onChange()
                validateFieldOnChange()
            }
        )
    }
}
