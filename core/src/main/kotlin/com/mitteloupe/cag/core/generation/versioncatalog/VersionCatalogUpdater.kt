package com.mitteloupe.cag.core.generation.versioncatalog

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.generation.CatalogInsertPosition
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.LibraryRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.PluginRequirement
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
        val version: VersionRequirement? = null
    ) : SectionEntryRequirement("libraries")

    data class BundleRequirement(
        override val key: String,
        val members: List<String>
    ) : SectionEntryRequirement("bundles")

    data class PluginRequirement(
        override val key: String,
        val id: String,
        val version: VersionRequirement
    ) : SectionEntryRequirement("plugins")
}

class VersionCatalogUpdater(
    private val fileCreator: FileCreator,
    private val contentUpdater: VersionCatalogContentUpdater = VersionCatalogContentUpdater()
) : VersionCatalogReader {
    private var resolvedPluginIdToAlias: Map<String, String> = emptyMap()
    private var resolvedLibraryModuleToAlias: Map<String, String> = emptyMap()

    override fun getResolvedPluginAliasFor(requirement: PluginRequirement): String =
        resolvedPluginIdToAlias[requirement.id] ?: requirement.key

    override fun isPluginAvailable(requirement: PluginRequirement): Boolean = resolvedPluginIdToAlias.containsKey(requirement.id)

    override fun getResolvedLibraryAliasForModule(requirement: LibraryRequirement): String =
        resolvedLibraryModuleToAlias[requirement.module] ?: requirement.key

    fun createOrReplaceVersionCatalog(
        projectRootDir: File,
        dependencyConfiguration: DependencyConfiguration
    ) {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (catalogFile.exists()) {
            catalogFile.delete()
        }

        createNewVersionCatalog(projectRootDir, dependencyConfiguration)
    }

    fun createOrUpdateVersionCatalog(
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
        val pluginRequirements = mutableListOf<PluginRequirement>()
        val pluginAliasToIdMutable = existingPluginAliasToId.toMutableMap()
        val uniqueDesiredPlugins = dependencyConfiguration.plugins.distinctBy { it.id }

        for (desired in uniqueDesiredPlugins) {
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
                PluginRequirement(
                    key = candidateAlias,
                    id = desired.id,
                    version = desired.version
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
        val libraryRequirements = mutableListOf<LibraryRequirement>()
        val libraryAliasToModuleMutable = existingLibraryAliasToModule.toMutableMap()
        val uniqueDesiredLibraries = dependencyConfiguration.libraries.distinctBy { it.module }

        for (desired in uniqueDesiredLibraries) {
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
                LibraryRequirement(
                    key = candidateAlias,
                    module = desired.module,
                    version = desired.version
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
                        requirements =
                            (dependencyConfiguration.versions + pluginRequirements.versions + libraryRequirements.versions)
                                .associateBy { it.key }
                                .values
                                .toList()
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

        val finalCatalogText = readCatalogFile(projectRootDir)
        if (finalCatalogText != null) {
            updateResolvedMappingsFromCatalog(finalCatalogText)
        }
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

        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        fileCreator.createFileIfNotExists(catalogFile) { "" }

        val pluginRequirements =
            dependencyConfiguration.plugins.distinctBy { it.id }.map { desired ->
                PluginRequirement(
                    key = desired.key,
                    id = desired.id,
                    version = desired.version
                )
            }

        val libraryRequirements =
            dependencyConfiguration.libraries.distinctBy { it.module }.map { desired ->
                LibraryRequirement(
                    key = desired.key,
                    module = desired.module,
                    version = desired.version
                )
            }

        val versionRequirements =
            (pluginRequirements.versions + libraryRequirements.versions + dependencyConfiguration.versions)
                .associateBy { it.key }
                .values
                .toList()

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

        val finalCatalogText = readCatalogFile(projectRootDir)
        if (finalCatalogText != null) {
            updateResolvedMappingsFromCatalog(finalCatalogText)
        }
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

    private fun updateResolvedMappingsFromCatalog(catalogText: String) {
        resolvedPluginIdToAlias = contentUpdater.parseExistingPluginIdToAlias(catalogText)
        val existingLibraryAliasToModule = contentUpdater.parseExistingLibraryAliasToModule(catalogText)
        resolvedLibraryModuleToAlias =
            existingLibraryAliasToModule.entries.associate { (alias, module) ->
                module to alias
            }
    }

    private val List<SectionEntryRequirement>.versions
        get() =
            mapNotNull {
                when (it) {
                    is LibraryRequirement -> it.version
                    is PluginRequirement -> it.version
                    else -> null
                }
            }
}
