package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import java.io.File

data class SectionTransaction<SECTION_TYPE : SectionEntryRequirement>(
    val insertPositionIfMissing: CatalogInsertPosition,
    val requirements: List<SECTION_TYPE>
)

sealed class SectionEntryRequirement(val header: String) {
    abstract val key: String

    data class VersionRequirement(
        override val key: String,
        val version: String
    ) : SectionEntryRequirement("versions")

    data class LibraryRequirement(
        override val key: String,
        val module: String,
        val versionRefKey: String? = null,
        val versionLiteral: String? = null
    ) : SectionEntryRequirement("libraries")

    data class BundleRequirement(
        override val key: String,
        val members: List<String>
    ) : SectionEntryRequirement("bundles")

    data class PluginRequirement(
        override val key: String,
        val id: String,
        val versionRefKey: String
    ) : SectionEntryRequirement("plugins")
}

class VersionCatalogUpdater(
    private val contentUpdater: VersionCatalogContentUpdater = VersionCatalogContentUpdater()
) {
    fun updateVersionCatalogIfPresent(projectRootDir: File): String? {
        val catalogTextBefore = readCatalogFile(projectRootDir)
        val existingPluginIdToAlias: Map<String, String> =
            catalogTextBefore?.let { parseExistingPluginIdToAlias(it) } ?: emptyMap()

        val desiredPlugins = desiredPluginEntries()
        val missingDesiredPlugins = desiredPlugins.filter { it.id !in existingPluginIdToAlias.keys }

        val pluginRequirements =
            if (missingDesiredPlugins.isEmpty()) {
                emptyList()
            } else {
                versionCatalogPluginRequirements(missingDesiredPlugins)
            }

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = versionCatalogLibraryRequirements()
                    ),
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = pluginRequirements
                    )
                )
        )?.let { return it }

        val catalogTextAfter = readCatalogFile(projectRootDir)
        val addedAndroidLibraryAlias = missingDesiredPlugins.any { it.id == "com.android.library" }
        val addedComposeBomAlias =
            (catalogTextAfter?.contains(Regex("(?m)^\\s*compose-bom\\s*=")) == true) &&
                (catalogTextBefore?.contains(Regex("(?m)^\\s*compose-bom\\s*=")) != true)

        return updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements =
                            versionCatalogVersionRequirements(
                                includeAndroidGradlePlugin = addedAndroidLibraryAlias,
                                includeComposeBom = addedComposeBomAlias
                            )
                    )
                )
        )
    }

    private fun readCatalogFile(projectRootDir: File): String? {
        val catalogDirectory = File(projectRootDir, "gradle")
        val catalogFile = File(catalogDirectory, "libs.versions.toml")
        return runCatching { if (catalogFile.exists()) catalogFile.readText() else null }.getOrNull()
    }

    fun <SECTION_TYPE : SectionEntryRequirement> updateVersionCatalogIfPresent(
        projectRootDir: File,
        sectionRequirements: List<SectionTransaction<SECTION_TYPE>>
    ): String? {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (!catalogFile.exists()) {
            return null
        }

        val catalogContent =
            runCatching { catalogFile.readText() }
                .getOrElse { return "${ERROR_PREFIX}Failed to read version catalog: ${it.message}" }
        val updatedContent = contentUpdater.updateCatalogText(catalogContent, sectionRequirements)
        if (updatedContent == catalogContent) {
            return null
        }

        return runCatching { catalogFile.writeText(updatedContent) }
            .exceptionOrNull()
            ?.let { "${ERROR_PREFIX}Failed to update version catalog: ${it.message}" }
    }

    private fun versionCatalogVersionRequirements(
        includeAndroidGradlePlugin: Boolean,
        includeComposeBom: Boolean
    ): List<SectionEntryRequirement> {
        val requirements =
            mutableListOf<SectionEntryRequirement>(
                SectionEntryRequirement.VersionRequirement(key = "compileSdk", version = "35"),
                SectionEntryRequirement.VersionRequirement(key = "minSdk", version = "24")
            )
        if (includeAndroidGradlePlugin) {
            requirements.add(
                SectionEntryRequirement.VersionRequirement(
                    key = "androidGradlePlugin",
                    version = "8.7.3"
                )
            )
        }
        if (includeComposeBom) {
            requirements.add(
                SectionEntryRequirement.VersionRequirement(
                    key = "composeBom",
                    version = "2025.08.01"
                )
            )
        }
        return requirements
    }

    private fun versionCatalogPluginRequirements(missingDesired: List<DesiredPlugin>): List<SectionEntryRequirement> =
        missingDesired.map { desired ->
            SectionEntryRequirement.PluginRequirement(
                key = desired.alias,
                id = desired.id,
                versionRefKey = desired.versionRefKey
            )
        }

    private fun versionCatalogLibraryRequirements(): List<SectionEntryRequirement> =
        listOf(
            SectionEntryRequirement.LibraryRequirement(
                key = "compose-bom",
                module = "androidx.compose:compose-bom",
                versionRefKey = "composeBom"
            ),
            SectionEntryRequirement.LibraryRequirement(
                key = "compose-ui",
                module = "androidx.compose.ui:ui"
            ),
            SectionEntryRequirement.LibraryRequirement(
                key = "compose-ui-graphics",
                module = "androidx.compose.ui:ui-graphics"
            ),
            SectionEntryRequirement.LibraryRequirement(
                key = "androidx-ui-tooling",
                module = "androidx.compose.ui:ui-tooling"
            ),
            SectionEntryRequirement.LibraryRequirement(
                key = "androidx-ui-tooling-preview",
                module = "androidx.compose.ui:ui-tooling-preview"
            ),
            SectionEntryRequirement.LibraryRequirement(
                key = "compose-material3",
                module = "androidx.compose.material3:material3"
            )
        )
}

private data class DesiredPlugin(val id: String, val alias: String, val versionRefKey: String)

private fun desiredPluginEntries(): List<DesiredPlugin> =
    listOf(
        DesiredPlugin(
            id = "org.jetbrains.kotlin.jvm",
            alias = "kotlin-jvm",
            versionRefKey = "kotlin"
        ),
        DesiredPlugin(
            id = "org.jetbrains.kotlin.android",
            alias = "kotlin-android",
            versionRefKey = "kotlin"
        ),
        DesiredPlugin(
            id = "com.android.library",
            alias = "android-library",
            versionRefKey = "androidGradlePlugin"
        ),
        DesiredPlugin(
            id = "org.jetbrains.kotlin.plugin.compose",
            alias = "compose-compiler",
            versionRefKey = "kotlin"
        )
    )

private fun parseExistingPluginIdToAlias(catalogText: String): Map<String, String> {
    val pluginsSection = extractPluginsSection(catalogText) ?: return emptyMap()
    val aliasToId =
        VERSION_CATALOG_PLUGIN_ENTRY_REGEX.findAll(pluginsSection).associate { match ->
            val alias = match.groupValues[1].trim()
            val id = match.groupValues[2].trim()
            alias to id
        }
    return aliasToId.entries.associate { (alias, id) -> id to alias }
}

private fun extractPluginsSection(toml: String): String? {
    val marker = "[plugins]"
    val startIndex = toml.indexOf(marker)
    if (startIndex == -1) return null
    val rest = toml.substring(startIndex + marker.length)
    val nextSectionIndex = rest.indexOf("\n[")
    return if (nextSectionIndex == -1) rest else rest.take(nextSectionIndex)
}

private val VERSION_CATALOG_PLUGIN_ENTRY_REGEX =
    """(?m)^\s*([A-Za-z0-9_.\-]+)\s*=\s*\{[^}]*?\bid\s*=\s*['"]([^'"]+)['"][^}]*}.*$""".toRegex()
