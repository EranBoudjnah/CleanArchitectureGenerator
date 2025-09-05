package com.mitteloupe.cag.core.generation.versioncatalog

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.generation.CatalogInsertPosition
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
) : VersionCatalogReader {
    private var resolvedPluginIdToAlias: Map<String, String> = emptyMap()
    private var resolvedLibraryModuleToAlias: Map<String, String> = emptyMap()

    override fun getResolvedPluginAliasFor(pluginId: String): String? = resolvedPluginIdToAlias[pluginId]

    override fun getResolvedLibraryAliasForModule(module: String): String? = resolvedLibraryModuleToAlias[module]

    fun updateVersionCatalogIfPresent(
        projectRootDir: File,
        dependencyConfiguration: DependencyConfiguration
    ) {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (catalogFile.exists()) {
            updateExistingVersionCatalog(projectRootDir, dependencyConfiguration)
            return
        }

        createNewVersionCatalog(projectRootDir, dependencyConfiguration)
    }

    private fun updateExistingVersionCatalog(
        projectRootDir: File,
        dependencyConfiguration: DependencyConfiguration
    ) {
        val catalogTextBefore = readCatalogFile(projectRootDir)
        val existingPluginIdToAlias: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingPluginIdToAlias(it) } ?: emptyMap()
        val existingPluginAliasToId: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingPluginAliasToId(it) } ?: emptyMap()

        val resolvedPluginIdToAliasMutable = existingPluginIdToAlias.toMutableMap()
        val pluginRequirements = mutableListOf<SectionEntryRequirement.PluginRequirement>()
        val pluginAliasToIdMutable = existingPluginAliasToId.toMutableMap()

        for (desired in dependencyConfiguration.plugins) {
            val existingAlias = existingPluginIdToAlias[desired.id]
            if (existingAlias != null) {
                resolvedPluginIdToAliasMutable[desired.id] = existingAlias
                continue
            }
            var candidateAlias = desired.key
            if (pluginAliasToIdMutable[candidateAlias] != null) {
                var counter = 2
                while (pluginAliasToIdMutable.containsKey("$candidateAlias-v$counter")) {
                    counter++
                }
                candidateAlias = "$candidateAlias-v$counter"
            }
            resolvedPluginIdToAliasMutable[desired.id] = candidateAlias
            pluginAliasToIdMutable[candidateAlias] = desired.id
            pluginRequirements.add(
                SectionEntryRequirement.PluginRequirement(
                    key = candidateAlias,
                    id = desired.id,
                    versionRefKey = desired.versionRefKey
                )
            )
        }

        resolvedPluginIdToAlias = resolvedPluginIdToAliasMutable.toMap()

        val catalogTextBeforeLibraries = readCatalogFile(projectRootDir)
        val existingLibraryAliasToModule: Map<String, String> =
            catalogTextBeforeLibraries?.let { contentUpdater.parseExistingLibraryAliasToModule(it) } ?: emptyMap()

        val existingLibraryModuleToAlias: Map<String, String> =
            existingLibraryAliasToModule.entries.associate { (alias, module) ->
                module to alias
            }

        val resolvedLibraryModuleToAliasMutable = existingLibraryModuleToAlias.toMutableMap()
        val libraryRequirements = mutableListOf<SectionEntryRequirement.LibraryRequirement>()
        val libraryAliasToModuleMutable = existingLibraryAliasToModule.toMutableMap()

        for (desired in dependencyConfiguration.libraries) {
            val existingAlias = existingLibraryModuleToAlias[desired.module]
            if (existingAlias != null) {
                resolvedLibraryModuleToAliasMutable[desired.module] = existingAlias
                continue
            }
            var candidateAlias = desired.key
            if (libraryAliasToModuleMutable[candidateAlias] != null) {
                var counter = 2
                while (libraryAliasToModuleMutable.containsKey("$candidateAlias-v$counter")) {
                    counter++
                }
                candidateAlias = "$candidateAlias-v$counter"
            }
            resolvedLibraryModuleToAliasMutable[desired.module] = candidateAlias
            libraryAliasToModuleMutable[candidateAlias] = desired.module
            libraryRequirements.add(
                SectionEntryRequirement.LibraryRequirement(
                    key = candidateAlias,
                    module = desired.module,
                    versionRefKey = desired.versionRefKey,
                    versionLiteral = desired.versionLiteral
                )
            )
        }

        resolvedLibraryModuleToAlias = resolvedLibraryModuleToAliasMutable.toMap()

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements = dependencyConfiguration.versions
                    ),
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = pluginRequirements
                    ),
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = libraryRequirements
                    )
                )
        )
    }

    private fun createNewVersionCatalog(
        projectRootDir: File,
        dependencyConfiguration: DependencyConfiguration
    ) {
        val catalogDirectory = File(projectRootDir, "gradle")
        if (!catalogDirectory.exists()) {
            if (!catalogDirectory.mkdirs()) {
                throw GenerationException("Failed to create gradle directory")
            }
        }

        val pluginRequirements =
            dependencyConfiguration.plugins.map { desired ->
                SectionEntryRequirement.PluginRequirement(
                    key = desired.key,
                    id = desired.id,
                    versionRefKey = desired.versionRefKey
                )
            }

        val libraryRequirements =
            dependencyConfiguration.libraries.map { desired ->
                SectionEntryRequirement.LibraryRequirement(
                    key = desired.key,
                    module = desired.module,
                    versionRefKey = desired.versionRefKey,
                    versionLiteral = desired.versionLiteral
                )
            }

        val addedAndroidLibraryAlias = pluginRequirements.any { it.id == "com.android.library" }
        val addedComposeBomAlias = libraryRequirements.any { it.module == "androidx.compose:compose-bom" }
        val addedDetektAlias = pluginRequirements.any { it.id == "io.gitlab.arturbosch.detekt" }

        val versionRequirements =
            buildList {
                addAll(dependencyConfiguration.versions)
                if (addedAndroidLibraryAlias) {
                    addAll(VersionCatalogConstants.ANDROID_VERSIONS)
                }
                if (addedComposeBomAlias) {
                    addAll(VersionCatalogConstants.COMPOSE_VERSIONS)
                }
                if (addedDetektAlias) {
                    addAll(VersionCatalogConstants.DETEKT_VERSIONS)
                }
            }

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements = versionRequirements
                    ),
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = pluginRequirements
                    ),
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = libraryRequirements
                    )
                )
        )
    }

    private fun <SECTION_TYPE : SectionEntryRequirement> updateVersionCatalogIfPresent(
        projectRootDir: File,
        sectionRequirements: List<SectionTransaction<SECTION_TYPE>>
    ) {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (!catalogFile.exists()) {
            return
        }

        val catalogTextBefore = readCatalogFile(projectRootDir)
        val updatedCatalogText =
            contentUpdater.updateCatalogText(
                catalogText = catalogTextBefore.orEmpty(),
                sectionTransactions = sectionRequirements
            )

        catalogFile.writeText(updatedCatalogText)
    }

    private fun readCatalogFile(projectRootDir: File): String? {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        return if (catalogFile.exists()) {
            catalogFile.readText()
        } else {
            null
        }
    }
}
