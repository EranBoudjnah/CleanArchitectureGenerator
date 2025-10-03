package com.mitteloupe.cag.cli.configuration

import java.io.File

private const val FILE_NAME = ".cagrc"

class ClientConfigurationLoader {
    fun load(projectRoot: File): ClientConfiguration {
        val projectFile = File(projectRoot, FILE_NAME)
        val homeFile = File(System.getProperty("user.home"), FILE_NAME)
        return loadFromFiles(projectFile = projectFile, homeFile = homeFile)
    }

    internal fun loadFromFiles(
        projectFile: File?,
        homeFile: File?
    ): ClientConfiguration {
        val homeConfiguration =
            if (homeFile?.exists() == true && homeFile.isFile) {
                parse(homeFile.readText())
            } else {
                ClientConfiguration.EMPTY
            }
        val projectConfiguration =
            if (projectFile?.exists() == true && projectFile.isFile) {
                parse(projectFile.readText())
            } else {
                ClientConfiguration.EMPTY
            }
        return mergeConfigurations(baseConfiguration = homeConfiguration, override = projectConfiguration)
    }

    private fun mergeConfigurations(
        baseConfiguration: ClientConfiguration,
        override: ClientConfiguration
    ): ClientConfiguration =
        ClientConfiguration(
            newProjectVersions = baseConfiguration.newProjectVersions + override.newProjectVersions,
            existingProjectVersions = baseConfiguration.existingProjectVersions + override.existingProjectVersions,
            git =
                GitConfiguration(
                    autoInitialize = baseConfiguration.git.autoInitialize || override.git.autoInitialize,
                    autoStage = baseConfiguration.git.autoStage || override.git.autoStage
                )
        )

    internal fun parse(text: String): ClientConfiguration =
        ClientConfiguration(
            newProjectVersions = extractProjectVersions(text, "new.versions"),
            existingProjectVersions = extractProjectVersions(text, "existing.versions"),
            git = extractGitConfiguration(text)
        )

    private fun extractProjectVersions(
        text: String,
        versionLabel: String
    ): Map<String, String> {
        val currentVersionsMap = mutableMapOf<String, String>()
        var isUnderLabel = false

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) return@forEach

            if (line.startsWith("[") && line.endsWith("]")) {
                isUnderLabel = line.substring(1, line.length - 1).equals(versionLabel, true)
                return@forEach
            }

            if (isUnderLabel) {
                val index = line.indexOf('=')
                if (index > 0) {
                    val key = line.take(index).trim()
                    val value = line.substring(index + 1).trim()
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        currentVersionsMap[key] = value
                    }
                }
            }
        }

        return currentVersionsMap
    }

    private fun extractGitConfiguration(text: String): GitConfiguration {
        var autoInitializeGit = false
        var autoStageGit = false
        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (
                line.startsWith("[") && line.endsWith("]") &&
                line.substring(1, line.length - 1).equals("git", ignoreCase = true)
            ) {
                return@forEach
            }

            if (line.startsWith("autoInitialize")) {
                autoInitializeGit = line.substringAfter('=', "false").trim().toBoolean()
            } else if (line.startsWith("autoStage")) {
                autoStageGit = line.substringAfter('=', "true").trim().toBoolean()
            }
        }
        return GitConfiguration(autoInitialize = autoInitializeGit, autoStage = autoStageGit)
    }
}
