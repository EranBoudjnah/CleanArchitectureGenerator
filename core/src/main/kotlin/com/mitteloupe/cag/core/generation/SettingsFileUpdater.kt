package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.ERROR_PREFIX
import java.io.File

class SettingsFileUpdater {
    fun updateProjectSettingsIfPresent(
        startDirectory: File,
        featureNameLowerCase: String
    ): String? {
        val projectRoot =
            DirectoryFinder()
                .findDirectory(startDirectory) { currentDirectory ->
                    kotlinSettingsFile(currentDirectory).exists() ||
                        groovySettingsFile(currentDirectory).exists()
                }
                ?: return null

        val ktsFile = kotlinSettingsFile(projectRoot)
        val groovyFile = groovySettingsFile(projectRoot)

        val settingsFile =
            when {
                ktsFile.exists() -> ktsFile
                groovyFile.exists() -> groovyFile
                else -> return null
            }

        return updateSettingsFile(settingsFile, featureNameLowerCase)
    }

    private fun updateSettingsFile(
        settingsFile: File,
        featureNameLowerCase: String
    ): String? {
        val originalFileContent =
            runCatching { settingsFile.readText() }
                .getOrElse {
                    return "${ERROR_PREFIX}Failed to read ${settingsFile.name}: ${it.message}"
                }

        val layers = listOf("ui", "presentation", "domain", "data")

        val hasGroupedInclude =
            originalFileContent.contains("include(\":features:$featureNameLowerCase:\$module\")") ||
                originalFileContent.contains("include \":features:$featureNameLowerCase:\$module\"")

        if (hasGroupedInclude) return null

        val modulePaths = layers.map { layer -> ":features:$featureNameLowerCase:$layer" }

        val allIncludedIndividually =
            modulePaths.all { path ->
                originalFileContent.contains("include\\(([\"'])$path\\1\\)".toRegex()) ||
                    originalFileContent.contains("include ([\"'])$path\\1".toRegex())
            }

        if (allIncludedIndividually) {
            return null
        }

        val filteredContent =
            originalFileContent
                .lines()
                .filterNot { line ->
                    modulePaths.any { path ->
                        line.contains("include(\"$path\")") ||
                            line.contains("include '$path'") ||
                            line.contains("include \"$path\"")
                    }
                }
                .joinToString(separator = "\n", postfix = if (originalFileContent.endsWith("\n")) "\n" else "")

        val contentToAppend =
            buildString {
                if (!filteredContent.endsWith("\n")) {
                    append('\n')
                }
                val layersKtsBlock = layers.joinToString(",\n") { "    \"$it\"" }
                val layersGroovyBlock = layers.joinToString(",\n") { "    '$it'" }
                if (settingsFile.name.endsWith(".kts")) {
                    append(
                        "setOf(\n" +
                            layersKtsBlock +
                            "\n).forEach { module ->\n" +
                            "    include(\":features:$featureNameLowerCase:${'$'}module\")\n" +
                            "}"
                    )
                } else {
                    append(
                        "[\n" +
                            layersGroovyBlock +
                            "\n].each { module ->\n" +
                            "    include \":features:$featureNameLowerCase:${'$'}module\"\n" +
                            "}"
                    )
                }
            }

        val updatedFileContent = filteredContent + contentToAppend

        return runCatching { settingsFile.writeText(updatedFileContent) }
            .exceptionOrNull()
            ?.let {
                "${ERROR_PREFIX}Failed to update ${settingsFile.name}: ${it.message}"
            }
    }

    private fun groovySettingsFile(projectRoot: File): File = File(projectRoot, "settings.gradle")

    private fun kotlinSettingsFile(currentDirectory: File): File = File(currentDirectory, "settings.gradle.kts")
}
