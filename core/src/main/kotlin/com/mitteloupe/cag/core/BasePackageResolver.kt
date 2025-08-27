package com.mitteloupe.cag.core

import java.io.File

private val pluginsBlockRegex =
    """(?s)plugins\s*\{([\n\r\s\S]*?)}""".toRegex()
private val idPluginLineRegex =
    """(?m)^\s*id\s*\(\s*['"]([^'"]+)['"]\s*\)[^\n\r]*""".toRegex()
private val aliasPluginLineRegex =
    """(?m)^\s*alias\s*\(\s*libs\.plugins\.([A-Za-z0-9_.\-]+)\s*\)[^\n\r]*""".toRegex()
private val applyFalseRegex = """apply\s*\(?\s*false\s*\)?""".toRegex()
private val androidAppPluginApplyRegex =
    """apply\s*\(?\s*plugin\s*[:=]\s*['"]com\.android\.application['"]""".toRegex()

private val namespaceRegex = """(?s)android\s*\{[\n\r\s\S]*?namespace(?:\s*=\s*|\s+)['"]([^'"]+)['"]""".toRegex()
private val versionCatalogEntryRegex =
    """(?m)^\s*([A-Za-z0-9_.\-]+)\s*=\s*\{[^}]*?\bid\s*=\s*['"]([^'"]+)['"][^}]*}.*$""".toRegex()
private const val PLUGINS_SECTION_MARKER = "[plugins]"

class BasePackageResolver() {
    fun determineBasePackage(projectModel: ProjectModel): String? {
        val selectedModule = projectModel.selectedModuleRootDir()
        val namespace = readAndroidNamespace(selectedModule)
        if (!namespace.isNullOrBlank()) return ensureTrailingDot(namespace)

        projectModel.allModuleRootDirs().forEach { moduleDir ->
            val moduleNamespace = readAndroidNamespace(moduleDir)
            if (!moduleNamespace.isNullOrBlank()) return ensureTrailingDot(moduleNamespace)
        }

        return null
    }

    private fun readAndroidNamespace(moduleDirectory: File?): String? {
        moduleDirectory ?: return null
        val gradleKtsFile = File(moduleDirectory, "build.gradle.kts")
        val gradleGroovyFile = File(moduleDirectory, "build.gradle")
        val fileContents =
            when {
                gradleKtsFile.exists() -> gradleKtsFile.readText()
                gradleGroovyFile.exists() -> gradleGroovyFile.readText()
                else -> return null
            }
        val hasAndroidAppPlugin =
            containsDirectAndroidAppIdInPluginsBlock(fileContents) ||
                androidAppPluginApplyRegex.containsMatchIn(fileContents) ||
                containsAndroidAppViaVersionCatalog(moduleDirectory, fileContents)
        if (!hasAndroidAppPlugin) {
            return null
        }
        val match = namespaceRegex.find(fileContents)
        return match?.groupValues?.get(1)?.trim()
    }

    private fun ensureTrailingDot(name: String): String = if (name.endsWith('.')) name else "$name."

    private fun containsAndroidAppViaVersionCatalog(
        moduleDirectory: File,
        buildFileContents: String
    ): Boolean {
        val aliasMatches =
            pluginsBlockRegex.findAll(buildFileContents).flatMap { blockMatch ->
                val block = blockMatch.groupValues[1]
                aliasPluginLineRegex.findAll(block)
            }.toList()
        if (aliasMatches.isEmpty()) return false

        val catalogFile = findNearestVersionCatalog(moduleDirectory) ?: return false
        val catalogText = runCatching { catalogFile.readText() }.getOrNull() ?: return false
        val pluginsSection = extractPluginsSection(catalogText) ?: return false

        val aliasToIdMap = parsePluginAliases(pluginsSection)
        if (aliasToIdMap.isEmpty()) return false

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

    private fun containsDirectAndroidAppIdInPluginsBlock(buildFileContents: String): Boolean =
        pluginsBlockRegex.findAll(buildFileContents).any { blockMatch ->
            val block = blockMatch.groupValues[1]
            idPluginLineRegex.findAll(block).any { idLineMatch ->
                val pluginId = idLineMatch.groupValues[1].trim()
                pluginId == "com.android.application" &&
                    !applyFalseRegex.containsMatchIn(idLineMatch.value)
            }
        }

    private fun findNearestVersionCatalog(startDirectory: File): File? =
        DirectoryFinder().findDirectory(startDirectory) { currentDirectory ->
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
}
