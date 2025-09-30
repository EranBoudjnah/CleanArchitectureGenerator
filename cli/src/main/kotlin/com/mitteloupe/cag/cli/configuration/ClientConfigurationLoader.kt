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
        val homeConfiguration = if (homeFile?.exists() == true && homeFile.isFile) parse(homeFile.readText()) else ClientConfiguration.EMPTY
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
            existingProjectVersions = baseConfiguration.existingProjectVersions + override.existingProjectVersions
        )

    internal fun parse(text: String): ClientConfiguration {
        val newProjectVersions = mutableMapOf<String, String>()
        val existingProjectVersions = mutableMapOf<String, String>()

        var currentVersionsMap: MutableMap<String, String>? = null

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) return@forEach

            if (line.startsWith("[") && line.endsWith("]")) {
                currentVersionsMap =
                    when (line.substring(1, line.length - 1).lowercase()) {
                        "new.versions" -> newProjectVersions
                        "existing.versions" -> existingProjectVersions
                        else -> null
                    }
                return@forEach
            }

            val index = line.indexOf('=')
            if (index > 0 && currentVersionsMap != null) {
                val key = line.take(index).trim()
                val value = line.substring(index + 1).trim()
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    currentVersionsMap[key] = value
                }
            }
        }

        return ClientConfiguration(newProjectVersions = newProjectVersions, existingProjectVersions = existingProjectVersions)
    }
}
