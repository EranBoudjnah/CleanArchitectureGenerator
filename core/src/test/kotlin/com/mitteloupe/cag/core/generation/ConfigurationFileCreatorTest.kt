package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class ConfigurationFileCreatorTest {
    private lateinit var classUnderTest: ConfigurationFileCreator
    private lateinit var projectRoot: File

    @Before
    fun setUp() {
        classUnderTest = ConfigurationFileCreator(FileCreator(FakeFileSystemBridge()))
        projectRoot = createTempDirectory(prefix = "projectRoot").toFile()
    }

    @Test
    fun `Given project root when writeDetektConfigurationFile then creates detekt yml with correct content`() {
        // Given
        val expectedDetektFile = File(projectRoot, "detekt.yml")
        val expectedContent = """build:
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
  exclude: []"""

        // When
        classUnderTest.writeDetektConfigurationFile(projectRoot)

        // Then
        assertTrue("Detekt configuration file should exist", expectedDetektFile.exists())
        assertTrue("Detekt configuration file should be a file", expectedDetektFile.isFile)
        val content = expectedDetektFile.readText()
        assertEquals("Detekt configuration file should have exact content", expectedContent, content)
    }

    @Test
    fun `Given project root when writeEditorConfigFile then creates editorconfig with correct content`() {
        // Given
        val expectedEditorConfigFile = File(projectRoot, ".editorconfig")
        val expectedContent = """root = true

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
max_line_length = 100"""

        // When
        classUnderTest.writeEditorConfigFile(projectRoot)

        // Then
        assertTrue("EditorConfig file should exist", expectedEditorConfigFile.exists())
        assertTrue("EditorConfig file should be a file", expectedEditorConfigFile.isFile)
        val content = expectedEditorConfigFile.readText()
        assertEquals("EditorConfig file should have exact content", expectedContent, content)
    }

    @Test
    fun `Given detekt yml already exists when writeDetektConfigurationFile then does not overwrite existing file`() {
        // Given
        val detektFile = File(projectRoot, "detekt.yml")
        val initialContent = "custom: configuration\n"
        detektFile.writeText(initialContent)

        // When
        classUnderTest.writeDetektConfigurationFile(projectRoot)

        // Then
        assertEquals("Existing detekt.yml should not be overwritten", initialContent, detektFile.readText())
    }

    @Test
    fun `Given editorconfig already exists when writeEditorConfigFile then does not overwrite existing file`() {
        // Given
        val editorConfigFile = File(projectRoot, ".editorconfig")
        val initialContent = "root = false\n"
        editorConfigFile.writeText(initialContent)

        // When
        classUnderTest.writeEditorConfigFile(projectRoot)

        // Then
        assertEquals("Existing .editorconfig should not be overwritten", initialContent, editorConfigFile.readText())
    }

    @Test
    fun `Given project root with subdirectories when writeDetektConfigurationFile then creates file in project root`() {
        // Given
        val subDirectory = File(projectRoot, "subdirectory")
        subDirectory.mkdirs()
        val expectedDetektFile = File(projectRoot, "detekt.yml")

        // When
        classUnderTest.writeDetektConfigurationFile(projectRoot)

        // Then
        assertTrue("Detekt configuration file should exist in project root", expectedDetektFile.exists())
        assertFalse("Detekt configuration file should not exist in subdirectory", File(subDirectory, "detekt.yml").exists())
    }

    @Test
    fun `Given project root with subdirectories when writeEditorConfigFile then creates file in project root`() {
        // Given
        val subDirectory = File(projectRoot, "subdirectory")
        subDirectory.mkdirs()
        val expectedEditorConfigFile = File(projectRoot, ".editorconfig")

        // When
        classUnderTest.writeEditorConfigFile(projectRoot)

        // Then
        assertTrue("EditorConfig file should exist in project root", expectedEditorConfigFile.exists())
        assertFalse("EditorConfig file should not exist in subdirectory", File(subDirectory, ".editorconfig").exists())
    }

    @Test
    fun `Given detekt configuration content when buildDetektConfiguration then returns properly formatted YAML`() {
        // Given
        val expectedContent = """build:
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
  exclude: []"""

        // When
        classUnderTest.writeDetektConfigurationFile(projectRoot)
        val detektFile = File(projectRoot, "detekt.yml")
        val content = detektFile.readText()

        // Then
        assertEquals("Detekt configuration should have exact content", expectedContent, content)
    }

    @Test
    fun `Given editor config content when buildEditorConfig then returns properly formatted INI`() {
        // Given
        val expectedContent = """root = true

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
max_line_length = 100"""

        // When
        classUnderTest.writeEditorConfigFile(projectRoot)
        val editorConfigFile = File(projectRoot, ".editorconfig")
        val content = editorConfigFile.readText()

        // Then
        assertEquals("EditorConfig should have exact content", expectedContent, content)
    }
}
