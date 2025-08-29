package com.mitteloupe.cag.core

import java.io.File

private val pluginsBlockRegex = """(?s)plugins\s*\{([\n\r\s\S]*?)}""".toRegex()
private val idPluginLineRegex = """(?m)^\s*id\s*\(\s*['"]([^'"]+)['"]\s*\)[^\n\r]*""".toRegex()
private val groovyIdPluginLineRegex = """(?m)^\s*id\s*['"]([^'"]+)['"][^\n\r]*""".toRegex()
private val aliasPluginLineRegex =
    """(?m)^\s*alias\s*\(\s*libs\.plugins\.([A-Za-z0-9_.\-]+)\s*\)[^\n\r]*""".toRegex()
private val applyFalseRegex = """apply\s*\(?\s*false\s*\)?""".toRegex()
private val androidAppPluginApplyRegex =
    """apply\s*\(?\s*plugin\s*[:=]\s*['"]com\.android\.application['"]""".toRegex()

private val versionCatalogEntryRegex =
    """(?m)^\s*([A-Za-z0-9_.\-]+)\s*=\s*\{[^}]*?\bid\s*=\s*['"]([^'"]+)['"][^}]*}.*$""".toRegex()
private const val PLUGINS_SECTION_MARKER = "[plugins]"

class AppModuleDirectoryFinder(private val directoryFinder: DirectoryFinder = DirectoryFinder()) {
    fun findAndroidAppModuleDirectories(projectRoot: File): List<File> {
        val moduleDirectories = findGradleModuleDirectories(projectRoot)
        return moduleDirectories.filter { moduleDirectory ->
            val text = readGradleBuildFileContents(moduleDirectory) ?: return@filter false

            containsAndroidAppPlugin(moduleDirectory, text)
        }
    }

    private fun containsAndroidAppPlugin(
        moduleDirectory: File,
        buildFileContents: String
    ): Boolean =
        containsDirectAndroidAppIdInPluginsBlock(buildFileContents) ||
            androidAppPluginApplyRegex.containsMatchIn(buildFileContents) ||
            containsAndroidAppViaVersionCatalog(moduleDirectory, buildFileContents)

    private fun containsDirectAndroidAppIdInPluginsBlock(buildFileContents: String): Boolean =
        pluginsBlockRegex.findAll(buildFileContents).any { blockMatch ->
            val block = blockMatch.groupValues[1]
            val ktsMatch =
                idPluginLineRegex.findAll(block).any { idLineMatch ->
                    val pluginId = idLineMatch.groupValues[1].trim()
                    pluginId == "com.android.application" &&
                        !applyFalseRegex.containsMatchIn(idLineMatch.value)
                }
            if (ktsMatch) return@any true
            groovyIdPluginLineRegex.findAll(block).any { idLineMatch ->
                val pluginId = idLineMatch.groupValues[1].trim()
                pluginId == "com.android.application" &&
                    !applyFalseRegex.containsMatchIn(idLineMatch.value)
            }
        }

    private fun containsAndroidAppViaVersionCatalog(
        moduleDirectory: File,
        buildFileContents: String
    ): Boolean {
        val aliasMatches =
            pluginsBlockRegex.findAll(buildFileContents).flatMap { blockMatch ->
                val block = blockMatch.groupValues[1]
                aliasPluginLineRegex.findAll(block)
            }.toList()
        if (aliasMatches.isEmpty()) {
            return false
        }

        val catalogFile = findNearestVersionCatalog(moduleDirectory) ?: return false
        val catalogText = runCatching { catalogFile.readText() }.getOrNull() ?: return false
        val pluginsSection = extractPluginsSection(catalogText) ?: return false

        val aliasToIdMap = parsePluginAliases(pluginsSection)
        if (aliasToIdMap.isEmpty()) {
            return false
        }

        return aliasMatches.any { matchResult ->
            val fullLine = matchResult.value
            if (applyFalseRegex.containsMatchIn(fullLine)) return@any false
            val rawAccessor = matchResult.groupValues[1].trim()
            val candidates =
                setOf(
                    rawAccessor,
                    rawAccessor.replace('.', '-'),
                    rawAccessor.replace('-', '.')
                )
            candidates.any { candidate ->
                aliasToIdMap[candidate] == "com.android.application"
            }
        }
    }

    private fun findNearestVersionCatalog(startDirectory: File): File? =
        directoryFinder.findDirectory(startDirectory) { currentDirectory ->
            val catalog = currentDirectory.versionCatalogFile()
            catalog.exists()
        }?.versionCatalogFile()

    private fun extractPluginsSection(toml: String): String? {
        val startIndex = toml.indexOf(PLUGINS_SECTION_MARKER)
        if (startIndex == -1) {
            return null
        }
        val rest = toml.substring(startIndex + PLUGINS_SECTION_MARKER.length)
        val nextSectionIndex = rest.indexOf("\n[")
        return if (nextSectionIndex == -1) {
            rest
        } else {
            rest.take(nextSectionIndex)
        }
    }

    private fun parsePluginAliases(pluginsSection: String): Map<String, String> =
        versionCatalogEntryRegex.findAll(pluginsSection).associate { match ->
            val alias = match.groupValues[1].trim()
            val id = match.groupValues[2].trim()
            alias to id
        }

    private fun File.versionCatalogFile() = File(File(this, "gradle"), "libs.versions.toml")

    private fun readGradleBuildFileContents(moduleDirectory: File): String? {
        val gradleKts = File(moduleDirectory, "build.gradle.kts")
        val gradleGroovy = File(moduleDirectory, "build.gradle")
        return when {
            gradleKts.exists() -> runCatching { gradleKts.readText() }.getOrNull()
            gradleGroovy.exists() -> runCatching { gradleGroovy.readText() }.getOrNull()
            else -> null
        }
    }

    private fun findGradleModuleDirectories(root: File): List<File> {
        if (!root.isDirectory) return emptyList()
        val queue = ArrayDeque<File>()
        val result = mutableListOf<File>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val directory = queue.removeFirst()
            val hasGradle =
                File(directory, "build.gradle.kts").exists() ||
                    File(directory, "build.gradle").exists()
            if (hasGradle) {
                result.add(directory)
            }
            if (!hasGradle || directory == root) {
                directory.listFiles()?.filter { it.isDirectory && !it.name.startsWith('.') && it.name != "build" }
                    ?.forEach(queue::add)
            }
        }
        return result
    }
}
