package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildSettingsGradleScript
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import java.io.File

class SettingsFileUpdater(
    private val fileCreator: FileCreator
) {
    fun updateProjectSettingsIfPresent(
        startDirectory: File,
        featureNameLowerCase: String
    ) {
        updateSettingsIfPresent(
            startDirectory = startDirectory,
            moduleNamesPrefix = ":features:$featureNameLowerCase",
            moduleNames = listOf("ui", "presentation", "domain", "data")
        )
    }

    fun updateDataSourceSettingsIfPresent(startDirectory: File) {
        updateSettingsIfPresent(
            startDirectory = startDirectory,
            moduleNamesPrefix = ":datasource",
            moduleNames = listOf("source", "implementation")
        )
    }

    fun updateArchitectureSettingsIfPresent(projectRoot: File) {
        updateSettingsIfPresent(
            startDirectory = projectRoot,
            moduleNamesPrefix = ":architecture",
            moduleNames = listOf("ui", "instrumentation-test", "presentation", "presentation-test", "domain")
        )
    }

    fun updateModuleSettingsIfPresent(
        projectRoot: File,
        moduleName: String
    ) {
        updateSettingsIfPresent(
            startDirectory = projectRoot,
            moduleNamesPrefix = moduleName,
            moduleNames = emptyList()
        )
    }

    private fun updateSettingsIfPresent(
        startDirectory: File,
        moduleNamesPrefix: String,
        moduleNames: List<String>
    ) {
        val settingsFile = findSettingsFile(startDirectory) ?: return
        updateIncludes(settingsFile, moduleNamesPrefix, moduleNames)
    }

    private fun findSettingsFile(startDirectory: File): File? {
        val projectRoot =
            DirectoryFinder()
                .findDirectory(startDirectory) { currentDirectory ->
                    kotlinSettingsFile(currentDirectory).exists() ||
                        groovySettingsFile(currentDirectory).exists()
                }
                ?: return null

        val ktsFile = kotlinSettingsFile(projectRoot)
        val groovyFile = groovySettingsFile(projectRoot)

        return when {
            ktsFile.exists() -> ktsFile
            groovyFile.exists() -> groovyFile
            else -> null
        }
    }

    private fun updateIncludes(
        settingsFile: File,
        moduleNamesPrefix: String,
        moduleNames: List<String>
    ) {
        val originalFileContent =
            runCatching { settingsFile.readText() }
                .getOrElse {
                    throw GenerationException("Failed to read ${settingsFile.name}: ${it.message}")
                }

        val groupedIncludeKts = $$"include(\"$$moduleNamesPrefix:$module\")"
        val groupedIncludeGroovyDouble = $$"include \"$$moduleNamesPrefix:$module\""
        val groupedIncludeGroovySingle = $$"include '$$moduleNamesPrefix:$module'"

        val hasGroupedInclude =
            originalFileContent.contains(groupedIncludeKts) ||
                originalFileContent.contains(groupedIncludeGroovyDouble) ||
                originalFileContent.contains(groupedIncludeGroovySingle)

        if (hasGroupedInclude) {
            return
        }

        val modulePaths = moduleNames.map { moduleName -> "$moduleNamesPrefix:$moduleName" }

        fun includeRegexesFor(path: String): List<Regex> =
            listOf(
                "include\\(([\"'])$path\\1\\)".toRegex(),
                "include ([\"'])$path\\1".toRegex()
            )

        fun isModuleIncludedIndividually(moduleName: String): Boolean {
            val pathWithRoot = "$moduleNamesPrefix:$moduleName"
            val pathWithoutRoot = pathWithRoot.removePrefix(":")
            val regexes = includeRegexesFor(pathWithRoot) + includeRegexesFor(pathWithoutRoot)
            return regexes.any { regex -> originalFileContent.contains(regex) }
        }

        val allIncludedIndividually =
            moduleNames.isNotEmpty() && moduleNames.all { moduleName -> isModuleIncludedIndividually(moduleName) }

        if (allIncludedIndividually) {
            return
        }

        val filteredContent =
            originalFileContent
                .lines()
                .filterNot { line ->
                    modulePaths.any { pathWithRoot ->
                        val pathWithoutRoot = pathWithRoot.removePrefix(":")
                        line.contains("include\\s*\\(\\s*(['\"]):?$pathWithoutRoot\\1\\s*\\)".toRegex()) ||
                            line.contains("include\\s+(['\"]):?$pathWithoutRoot\\1".toRegex())
                    }
                }.joinToString(separator = "\n", postfix = if (originalFileContent.endsWith("\n")) "\n" else "")

        val contentToAppend =
            buildString {
                if (!filteredContent.endsWith("\n")) {
                    append('\n')
                }
                if (settingsFile.name.endsWith(".kts")) {
                    if (moduleNames.isEmpty()) {
                        append("include(\"$moduleNamesPrefix\")\n")
                    } else {
                        val modulesKtsBlock = moduleNames.joinToString(",\n") { "    \"$it\"" }
                        append(
                            "setOf(\n" +
                                modulesKtsBlock +
                                "\n).forEach { module ->\n" +
                                $$"    include(\"$$moduleNamesPrefix:$module\")\n" +
                                "}"
                        )
                    }
                } else {
                    if (moduleNames.isEmpty()) {
                        append("include \"$moduleNamesPrefix\"\n")
                    } else {
                        val modulesGroovyBlock = moduleNames.joinToString(",\n") { "    '$it'" }
                        append(
                            "[\n" +
                                modulesGroovyBlock +
                                "\n].each { module ->\n" +
                                $$"    include \"$$moduleNamesPrefix:$module\"\n" +
                                "}"
                        )
                    }
                }
            }

        runCatching { fileCreator.createOrUpdateFile(settingsFile) { filteredContent + contentToAppend } }
            .exceptionOrNull()
            ?.let { throw GenerationException("Failed to update ${settingsFile.name}: ${it.message}") }
    }

    private fun groovySettingsFile(projectRoot: File): File = File(projectRoot, "settings.gradle")

    private fun kotlinSettingsFile(currentDirectory: File): File = File(currentDirectory, "settings.gradle.kts")

    fun writeProjectSettings(
        projectRoot: File,
        projectName: String,
        featureNames: List<String>
    ) {
        val settingsFile = File(projectRoot, "settings.gradle.kts")
        runCatching {
            fileCreator.createOrUpdateFile(settingsFile) {
                buildSettingsGradleScript(projectName.withoutSpaces(), featureNames)
            }
        }.onFailure { throw GenerationException("Failed to create settings.gradle.kts: ${it.message}") }
    }
}
