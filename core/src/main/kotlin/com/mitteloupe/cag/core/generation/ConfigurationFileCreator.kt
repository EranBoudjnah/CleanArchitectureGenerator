package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import java.io.File

class ConfigurationFileCreator(
    private val fileCreator: FileCreator
) {
    fun writeDetektConfigurationFile(projectRoot: File) {
        val detektFile = File(projectRoot, "detekt.yml")
        fileCreator.createFileIfNotExists(detektFile) { buildDetektConfiguration() }
    }

    fun writeEditorConfigFile(projectRoot: File) {
        val editorConfigFile = File(projectRoot, ".editorconfig")
        fileCreator.createFileIfNotExists(editorConfigFile) { buildEditorConfig() }
    }

    private fun buildDetektConfiguration(): String =
        """
        build:
          maxIssues: 0
          excludeCorrectable: false
          weights:
            complexity: 2
            LongParameterList: 1
            style: 1
            comments: 1

        config:
          validation: true
          warningsAsErrors: false
          checkExhaustiveness: false

        processors:
          active: true
          exclude:
            - 'DetektProgressListener'

        console-reports:
          active: true
          exclude:
            - 'ProjectStatisticsReport'
            - 'ComplexityReport'
            - 'NotificationReport'
            - 'FindingsReport'
            - 'FileBasedFindingsReport'
            - 'ProjectMetricsReport'

        output-reports:
          active: true
          exclude: []
        """.trimIndent()

    private fun buildEditorConfig(): String =
        """
        root = true

        [*.{kt,kts}]
        end_of_line = lf
        ij_kotlin_allow_trailing_comma = false
        ij_kotlin_allow_trailing_comma_on_call_site = false
        ij_kotlin_imports_layout = *
        ij_kotlin_packages_to_use_import_on_demand = java.util.*, kotlinx.android.synthetic.**
        indent_size = 4
        indent_style = space
        insert_final_newline = true
        ktlint_code_style = android_studio
        ktlint_function_naming_ignore_when_annotated_with=Composable
        ktlint_function_signature_body_expression_wrapping = default
        ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than = unset
        ktlint_ignore_back_ticked_identifier = false
        max_line_length = 100
        """.trimIndent()
}
