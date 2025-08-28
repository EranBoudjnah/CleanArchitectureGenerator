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

        val modulePaths =
            listOf("ui", "presentation", "domain", "data")
                .map { layer -> ":features:$featureNameLowerCase:$layer" }

        val missingIncludes =
            modulePaths.filterNot { path ->
                originalFileContent.contains("include(\"$path\")") || originalFileContent.contains("include '$path'")
            }

        if (missingIncludes.isEmpty()) return null

        val contentToAppend =
            buildString {
                if (!originalFileContent.endsWith("\n")) {
                    append('\n')
                }
                missingIncludes.forEach { path ->
                    append("include(\"$path\")\n")
                }
            }

        val updatedFileContent = originalFileContent + contentToAppend

        return runCatching { settingsFile.writeText(updatedFileContent) }
            .exceptionOrNull()
            ?.let {
                "${ERROR_PREFIX}Failed to update ${settingsFile.name}: ${it.message}"
            }
    }

    private fun groovySettingsFile(projectRoot: File): File = File(projectRoot, "settings.gradle")

    private fun kotlinSettingsFile(currentDirectory: File): File = File(currentDirectory, "settings.gradle.kts")
}
