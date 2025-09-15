package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildSettingsGradleScript
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import java.io.File

class SettingsFileUpdater(private val fileCreator: FileCreator) {
    fun updateProjectSettingsIfPresent(
        startDirectory: File,
        featureNameLowerCase: String
    ) {
        updateSettingsIfPresent(
            startDirectory = startDirectory,
            groupPrefix = ":features:$featureNameLowerCase",
            moduleNames = listOf("ui", "presentation", "domain", "data")
        )
    }

    fun updateDataSourceSettingsIfPresent(startDirectory: File) {
        updateSettingsIfPresent(
            startDirectory = startDirectory,
            groupPrefix = ":datasource",
            moduleNames = listOf("source", "implementation")
        )
    }

    fun updateArchitectureSettingsIfPresent(projectRoot: File) {
        updateSettingsIfPresent(
            startDirectory = projectRoot,
            groupPrefix = ":architecture",
            moduleNames = listOf("ui", "instrumentation-test", "presentation", "presentation-test", "domain")
        )
    }

    private fun updateSettingsIfPresent(
        startDirectory: File,
        groupPrefix: String,
        moduleNames: List<String>
    ) {
        val settingsFile = findSettingsFile(startDirectory) ?: return
        updateIncludes(settingsFile, groupPrefix, moduleNames)
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
    ) {
        val originalFileContent =
            runCatching { settingsFile.readText() }
                .getOrElse {
                    throw GenerationException("Failed to read ${settingsFile.name}: ${it.message}")
                }

        val groupedIncludeKts = "include(\"$groupPrefix:${'$'}module\")"
        val groupedIncludeGroovyDouble = "include \"$groupPrefix:${'$'}module\""
        val groupedIncludeGroovySingle = "include '$groupPrefix:${'$'}module'"

        val hasGroupedInclude =
            originalFileContent.contains(groupedIncludeKts) ||
                originalFileContent.contains(groupedIncludeGroovyDouble) ||
                originalFileContent.contains(groupedIncludeGroovySingle)

        if (hasGroupedInclude) {
            return
        }

        val modulePaths = moduleNames.map { moduleName -> "$groupPrefix:$moduleName" }

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
            return
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
        val content = buildSettingsGradleScript(projectName, featureNames)
        runCatching { fileCreator.createOrUpdateFile(settingsFile) { content } }
            .onFailure { throw GenerationException("Failed to create settings.gradle.kts: ${it.message}") }
    }
}
