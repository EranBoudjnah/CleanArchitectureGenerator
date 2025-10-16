package com.mitteloupe.cag.cli.configuration

import com.mitteloupe.cag.cli.configuration.model.DependencyInjection
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
                    autoInitialize = override.git.autoInitialize ?: baseConfiguration.git.autoInitialize,
                    autoStage = override.git.autoStage ?: baseConfiguration.git.autoStage,
                    path = override.git.path ?: baseConfiguration.git.path
                ),
            dependencyInjection =
                DependencyInjectionConfiguration(
                    library = override.dependencyInjection.library ?: baseConfiguration.dependencyInjection.library
                )
        )

    internal fun parse(text: String): ClientConfiguration =
        ClientConfiguration(
            newProjectVersions = extractProjectVersions(text, "new.versions"),
            existingProjectVersions = extractProjectVersions(text, "existing.versions"),
            git = extractGitConfiguration(text),
            dependencyInjection = extractDependencyInjectionConfiguration(text)
        )

    private fun extractProjectVersions(
        text: String,
        versionLabel: String
    ): Map<String, String> =
        buildMap {
            visitLinesInSection(text, versionLabel) { line ->
                val index = line.indexOf('=')
                if (index > 0) {
                    val key = line.take(index).trim()
                    val value = line.substring(index + 1).trim()
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        this@buildMap[key] = value
                    }
                }
            }
        }

    private fun extractGitConfiguration(text: String): GitConfiguration {
        var autoInitializeGit: Boolean? = null
        var autoStageGit: Boolean? = null
        var gitPath: String? = null
        visitLinesInSection(text, "git") { line ->
            if (line.containsKey("autoInitialize")) {
                autoInitializeGit = line.substringAfter('=', "false").trim().toBoolean()
            } else if (line.containsKey("autoStage")) {
                autoStageGit = line.substringAfter('=', "true").trim().toBoolean()
            } else if (line.containsKey("path")) {
                gitPath = line.substringAfter('=', "").trim().ifEmpty { null }
            }
        }

        return GitConfiguration(autoInitialize = autoInitializeGit, autoStage = autoStageGit, path = gitPath)
    }

    private fun extractDependencyInjectionConfiguration(text: String): DependencyInjectionConfiguration {
        var library: DependencyInjection? = null

        visitLinesInSection(text, "dependencyInjection") { line ->
            if (line.containsKey("library")) {
                library =
                    DependencyInjection.fromString(
                        line.substringAfter('=', "").trim()
                    )
            }
        }

        return DependencyInjectionConfiguration(library)
    }

    private fun visitLinesInSection(
        text: String,
        section: String,
        visitLineInSection: (String) -> Unit
    ) {
        var isInSection = false

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) return@forEach

            if (line.startsWith("[") && line.endsWith("]")) {
                isInSection = line.substring(1, line.length - 1).equals(section, ignoreCase = true)
                return@forEach
            }

            if (!isInSection) return@forEach

            visitLineInSection(line)
        }
    }

    private fun String.containsKey(key: String) = matches("^\\s*$key\\s*=.*".toRegex(RegexOption.IGNORE_CASE))
}
