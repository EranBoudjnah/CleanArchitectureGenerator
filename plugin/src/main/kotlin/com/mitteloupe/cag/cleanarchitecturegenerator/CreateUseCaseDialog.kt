package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.whenItemSelectedFromUi
import com.intellij.util.ui.UIUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.form.PredicateDocumentFilter
import com.mitteloupe.cag.cleanarchitecturegenerator.validation.SymbolValidator
import java.awt.EventQueue.invokeLater
import java.io.File
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AbstractDocument

private const val USE_CASE_SUFFIX = "UseCase"
private const val DEFAULT_USE_CASE_NAME = "DoSomething"
private const val DEFAULT_DATA_TYPE = "Unit"

class CreateUseCaseDialog(
    private val project: Project?,
    suggestedDirectory: File?
) : DialogWrapper(project) {
    private val initialDirectory = suggestedDirectory?.absolutePath.orEmpty()
    private lateinit var useCaseNameTextField: JComponent
    private var useCaseNameText: String = ""
    private var directoryPath: String = initialDirectory
    private lateinit var inputDataTypeComboBox: ComboBox<String>
    private lateinit var inputWarningLabel: JLabel
    private lateinit var outputDataTypeComboBox: ComboBox<String>
    private lateinit var outputWarningLabel: JLabel
    private val modelClassFinder = ModelClassFinder()
    private val symbolValidator = SymbolValidator()
    private val inputDataTypeModel = DefaultComboBoxModel<String>()
    private val outputDataTypeModel = DefaultComboBoxModel<String>()

    val useCaseNameWithSuffix: String
        get() = useCaseName.removeSuffix(USE_CASE_SUFFIX) + USE_CASE_SUFFIX

    private val useCaseName: String
        get() = useCaseNameText.trim()

    private var _inputDataType: String? = null
    val inputDataType: String?
        get() = _inputDataType

    private var _outputDataType: String? = null
    val outputDataType: String?
        get() = _outputDataType

    val destinationDirectory: File?
        get() = directoryPath.trim().takeIf { it.isNotEmpty() }?.let { File(it) }

    init {
        title = CleanArchitectureGeneratorBundle.message("info.usecase.generator.title")
        init()

        setupDataTypeComboBoxes()
        setupWarningLabels()
    }

    override fun createCenterPanel(): DialogPanel =
        panel {
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.name.label")) {
                textField()
                    .bindText(::useCaseNameText)
                    .onChanged { useCaseNameText = it.text }
                    .applyToComponent {
                        useCaseNameTextField = this
                        invokeLater {
                            columns = 20
                            text = DEFAULT_USE_CASE_NAME
                            selectAll()
                            requestFocusInWindow()
                        }
                        (document as AbstractDocument).documentFilter = PredicateDocumentFilter { !it.isWhitespace() }
                    }
                label(USE_CASE_SUFFIX)
                    .applyToComponent { foreground = UIUtil.getLabelDisabledForeground() }
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.input.type.label")) {
                @Suppress("UnstableApiUsage")
                comboBox(inputDataTypeModel)
                    .whenItemSelectedFromUi { _inputDataType = it }
                    .applyToComponent {
                        inputDataTypeComboBox = this
                        isEditable = true
                        val textField = editor.editorComponent as JTextField
                        textField.handleChange {
                            println("ERAN: CHANGE! ${textField.text}")
                            _inputDataType = textField.text
                            inputDataType.validateDataType(inputWarningLabel)
                        }
                        invokeLater { selectedItem = DEFAULT_DATA_TYPE }
                    }
                label("").applyToComponent { inputWarningLabel = this }
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.output.type.label")) {
                @Suppress("UnstableApiUsage")
                comboBox(outputDataTypeModel)
                    .whenItemSelectedFromUi { _outputDataType = it }
                    .applyToComponent {
                        outputDataTypeComboBox = this
                        isEditable = true
                        val textField = editor.editorComponent as JTextField
                        textField.handleChange {
                            println("ERAN: CHANGE! ${textField.text}")
                            _outputDataType = textField.text
                            outputDataType.validateDataType(outputWarningLabel)
                        }
                        invokeLater { selectedItem = DEFAULT_DATA_TYPE }
                    }
                label("").applyToComponent { outputWarningLabel = this }
            }
            row(CleanArchitectureGeneratorBundle.message("dialog.usecase.directory.field.label")) {
                textFieldWithBrowseButton(
                    project = project,
                    fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    fileChosen = {
                        it.path.also { path ->
                            directoryPath = path
                            setupDataTypeComboBoxes()
                        }
                    }
                ).bindText(::directoryPath)
                    .applyToComponent {
                        invokeLater {
                            text = initialDirectory
                        }
                        toolTipText = CleanArchitectureGeneratorBundle.message("dialog.usecase.directory.comment")
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

    private fun setupWarningLabels() {
        inputWarningLabel.clearWarning()
        outputWarningLabel.clearWarning()
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

    private fun String?.validateDataType(warningLabel: JLabel) {
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
            warningLabel.showFieldWarning(CleanArchitectureGeneratorBundle.message("error.symbol.not.found"))
        } else {
            warningLabel.clearWarning()
        }
    }

    private fun JLabel.clearWarning() {
        icon = null
        text = " "
    }

    private fun JLabel.showFieldWarning(message: String) {
        icon = AllIcons.General.Warning
        text = message
    }

    private fun setupDataTypeComboBoxes() {
        File(initialDirectory).let { destinationDirectory ->
            val modelClasses = modelClassFinder.findModelClasses(destinationDirectory)
            val allOptions = ModelClassFinder.PRIMITIVE_TYPES + modelClasses

            inputDataTypeModel.addAll(allOptions)
            outputDataTypeModel.addAll(allOptions)
        }
    }

    private fun JTextField.handleChange(onChange: () -> Unit) {
        document.addDocumentListener(
            object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent) = onChange()

                override fun removeUpdate(e: DocumentEvent) = onChange()

                override fun changedUpdate(e: DocumentEvent) = onChange()
            }
        )
    }
}
