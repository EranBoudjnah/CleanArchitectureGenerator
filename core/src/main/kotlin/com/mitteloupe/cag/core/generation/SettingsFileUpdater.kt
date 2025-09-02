package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.ERROR_PREFIX
import java.io.File

class SettingsFileUpdater {
    fun updateProjectSettingsIfPresent(
        startDirectory: File,
        featureNameLowerCase: String
    ): String? =
        updateSettingsIfPresent(
            startDirectory = startDirectory,
            groupPrefix = ":features:$featureNameLowerCase",
            moduleNames = listOf("ui", "presentation", "domain", "data")
        )

    fun updateDataSourceSettingsIfPresent(startDirectory: File): String? =
        updateSettingsIfPresent(
            startDirectory = startDirectory,
            groupPrefix = ":datasource",
            moduleNames = listOf("source", "implementation")
        )

    fun updateArchitectureSettingsIfPresent(projectRoot: File): String? =
        updateSettingsIfPresent(
            startDirectory = projectRoot,
            groupPrefix = ":architecture",
            moduleNames = listOf("domain", "presentation", "ui")
        )

    private fun updateSettingsIfPresent(
        startDirectory: File,
        groupPrefix: String,
        moduleNames: List<String>
    ): String? {
        val settingsFile = findSettingsFile(startDirectory) ?: return null
        return updateIncludes(settingsFile, groupPrefix, moduleNames)
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
        groupPrefix: String,
        moduleNames: List<String>
    ): String? {
        val originalFileContent =
            runCatching { settingsFile.readText() }
                .getOrElse {
                    return "${ERROR_PREFIX}Failed to read ${settingsFile.name}: ${it.message}"
                }

        val groupedIncludeKts = "include(\"$groupPrefix:${'$'}module\")"
        val groupedIncludeGroovyDouble = "include \"$groupPrefix:${'$'}module\""
        val groupedIncludeGroovySingle = "include '$groupPrefix:${'$'}module'"

        val hasGroupedInclude =
            originalFileContent.contains(groupedIncludeKts) ||
                originalFileContent.contains(groupedIncludeGroovyDouble) ||
                originalFileContent.contains(groupedIncludeGroovySingle)

        if (hasGroupedInclude) {
            return null
        }

        val modulePaths = moduleNames.map { moduleName -> "$groupPrefix:$moduleName" }

        // Allow both rooted (":group:module") and non-rooted ("group:module") includes
        fun includeRegexesFor(path: String): List<Regex> =
            listOf(
                "include\\(([\"'])$path\\1\\)".toRegex(),
                "include ([\"'])$path\\1".toRegex()
            )

        fun isModuleIncludedIndividually(moduleName: String): Boolean {
            val pathWithRoot = "$groupPrefix:$moduleName"
            val pathWithoutRoot = pathWithRoot.removePrefix(":")
            val regexes = includeRegexesFor(pathWithRoot) + includeRegexesFor(pathWithoutRoot)
            return regexes.any { regex -> originalFileContent.contains(regex) }
        }

        val allIncludedIndividually = moduleNames.all { moduleName -> isModuleIncludedIndividually(moduleName) }

        if (allIncludedIndividually) {
            return null
        }

        val filteredContent =
            originalFileContent
                .lines()
                .filterNot { line ->
                    modulePaths.any { pathWithRoot ->
                        val pathWithoutRoot = pathWithRoot.removePrefix(":")
                        line.contains("include(\"$pathWithRoot\")") ||
                            line.contains("include '$pathWithRoot'") ||
                            line.contains("include \"$pathWithRoot\"") ||
                            line.contains("include(\"$pathWithoutRoot\")") ||
                            line.contains("include '$pathWithoutRoot'") ||
                            line.contains("include \"$pathWithoutRoot\"")
                    }
                }
                .joinToString(separator = "\n", postfix = if (originalFileContent.endsWith("\n")) "\n" else "")

        val contentToAppend =
            buildString {
                if (!filteredContent.endsWith("\n")) append('\n')
                val modulesKtsBlock = moduleNames.joinToString(",\n") { "    \"$it\"" }
                val modulesGroovyBlock = moduleNames.joinToString(",\n") { "    '$it'" }
                if (settingsFile.name.endsWith(".kts")) {
                    append(
                        "setOf(\n" +
                            modulesKtsBlock +
                            "\n).forEach { module ->\n" +
                            "    include(\"$groupPrefix:${'$'}module\")\n" +
                            "}"
                    )
                } else {
                    append(
                        "[\n" +
                            modulesGroovyBlock +
                            "\n].each { module ->\n" +
                            "    include \"$groupPrefix:${'$'}module\"\n" +
                            "}"
                    )
                }
            }

        val updatedFileContent = filteredContent + contentToAppend

        return runCatching { settingsFile.writeText(updatedFileContent) }
            .exceptionOrNull()
            ?.let { "${ERROR_PREFIX}Failed to update ${settingsFile.name}: ${it.message}" }
    }

    private fun groovySettingsFile(projectRoot: File): File = File(projectRoot, "settings.gradle")

    private fun kotlinSettingsFile(currentDirectory: File): File = File(currentDirectory, "settings.gradle.kts")
}
