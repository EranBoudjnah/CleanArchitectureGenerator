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
        enableCompose: Boolean = false,
        includeCoroutineDependencies: Boolean = false,
        enableKtlint: Boolean = false,
        enableDetekt: Boolean = false
    ) {
        val catalogTextBefore = readCatalogFile(projectRootDir)
        val existingPluginIdToAlias: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingPluginIdToAlias(it) } ?: emptyMap()
        val existingPluginAliasToId: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingPluginAliasToId(it) } ?: emptyMap()
        val existingLibraryAliasToModule: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingLibraryAliasToModule(it) } ?: emptyMap()

        val existingLibraryModuleToAlias: Map<String, String> =
            existingLibraryAliasToModule.entries.associate { (alias, module) ->
                module to alias
            }

        val desiredPlugins = desiredPlugins(enableCompose, enableKtlint, enableDetekt)
        val desiredLibraries = desiredLibraries(enableCompose, includeCoroutineDependencies)

        val resolvedPluginIdToAliasMutable = existingPluginIdToAlias.toMutableMap()
        val pluginRequirements = mutableListOf<SectionEntryRequirement.PluginRequirement>()
        val pluginAliasToIdMutable = existingPluginAliasToId.toMutableMap()
        for (desired in desiredPlugins) {
            val existingAlias = existingPluginIdToAlias[desired.id]
            if (existingAlias != null) {
                resolvedPluginIdToAliasMutable[desired.id] = existingAlias
                continue
            }
            var candidateAlias = desired.alias
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

        val resolvedLibraryModuleToAliasMutable = existingLibraryModuleToAlias.toMutableMap()
        val libraryRequirements = mutableListOf<SectionEntryRequirement.LibraryRequirement>()
        val libraryAliasToModuleMutable = existingLibraryAliasToModule.toMutableMap()
        for (desired in desiredLibraries) {
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

        resolvedPluginIdToAlias = resolvedPluginIdToAliasMutable.toMap()
        resolvedLibraryModuleToAlias = resolvedLibraryModuleToAliasMutable.toMap()

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = libraryRequirements
                    ),
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = pluginRequirements
                    )
                )
        )

        val addedAndroidLibraryAlias = pluginRequirements.any { it.id == "com.android.library" }
        val addedComposeBomAlias = libraryRequirements.any { it.module == "androidx.compose:compose-bom" }
        val addedDetektAlias = pluginRequirements.any { it.id == "io.gitlab.arturbosch.detekt" }

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements =
                            versionCatalogVersionRequirements(
                                includeAndroidGradlePlugin = addedAndroidLibraryAlias,
                                includeComposeBom = addedComposeBomAlias,
                                includeDetekt = addedDetektAlias
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

    private fun <SECTION_TYPE : SectionEntryRequirement> updateVersionCatalogIfPresent(
        projectRootDir: File,
        sectionRequirements: List<SectionTransaction<SECTION_TYPE>>
    ) {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (!catalogFile.exists()) {
            return
        }

        val catalogContent =
            runCatching { catalogFile.readText() }
                .getOrElse { throw GenerationException("Failed to read version catalog: ${it.message}") }
        val updatedContent = contentUpdater.updateCatalogText(catalogContent, sectionRequirements)
        if (updatedContent == catalogContent) {
            return
        }

        runCatching { catalogFile.writeText(updatedContent) }
            .exceptionOrNull()
            ?.let { throw GenerationException("Failed to update version catalog: ${it.message}") }
    }

    private fun versionCatalogVersionRequirements(
        includeAndroidGradlePlugin: Boolean,
        includeComposeBom: Boolean,
        includeDetekt: Boolean
    ): List<SectionEntryRequirement> =
        buildList {
            add(SectionEntryRequirement.VersionRequirement(key = "compileSdk", version = "35"))
            add(SectionEntryRequirement.VersionRequirement(key = "minSdk", version = "24"))
            if (includeAndroidGradlePlugin) {
                add(
                    SectionEntryRequirement.VersionRequirement(
                        key = "androidGradlePlugin",
                        version = "8.7.3"
                    )
                )
            }
            if (includeComposeBom) {
                add(
                    SectionEntryRequirement.VersionRequirement(
                        key = "composeBom",
                        version = "2025.08.01"
                    )
                )
                add(
                    SectionEntryRequirement.VersionRequirement(
                        key = "composeNavigation",
                        version = "2.9.3"
                    )
                )
            }
            if (includeDetekt) {
                add(
                    SectionEntryRequirement.VersionRequirement(
                        key = "detekt",
                        version = "1.23.6"
                    )
                )
            }
        }
}

private data class DesiredPlugin(val id: String, val alias: String, val versionRefKey: String)

private data class DesiredLibrary(
    val key: String,
    val module: String,
    val versionRefKey: String? = null,
    val versionLiteral: String? = null
)

private fun desiredLibraries(
    enableCompose: Boolean,
    includeCoroutineDependencies: Boolean
): List<DesiredLibrary> =
    buildList {
        if (includeCoroutineDependencies) {
            addAll(
                listOf(
                    DesiredLibrary(
                        key = "androidx-core-ktx",
                        module = "androidx.core:core-ktx",
                        versionLiteral = "1.12.0"
                    ),
                    DesiredLibrary(
                        key = "androidx-lifecycle-runtime-ktx",
                        module = "androidx.lifecycle:lifecycle-runtime-ktx",
                        versionLiteral = "2.7.0"
                    ),
                    DesiredLibrary(
                        key = "kotlinx-coroutines-core",
                        module = "org.jetbrains.kotlinx:kotlinx-coroutines-core",
                        versionLiteral = "1.7.3"
                    ),
                    DesiredLibrary(
                        key = "junit",
                        module = "junit:junit",
                        versionRefKey = "junit4"
                    ),
                    DesiredLibrary(
                        key = "androidx-test-ext-junit",
                        module = "androidx.test.ext:junit",
                        versionLiteral = "1.1.5"
                    ),
                    DesiredLibrary(
                        key = "androidx-test-espresso-core",
                        module = "androidx.test.espresso:espresso-core",
                        versionLiteral = "3.5.1"
                    )
                )
            )
        }

        if (enableCompose) {
            addAll(
                listOf(
                    DesiredLibrary(
                        key = "compose-bom",
                        module = "androidx.compose:compose-bom",
                        versionRefKey = "composeBom"
                    ),
                    DesiredLibrary(
                        key = "compose-ui",
                        module = "androidx.compose.ui:ui"
                    ),
                    DesiredLibrary(
                        key = "compose-ui-graphics",
                        module = "androidx.compose.ui:ui-graphics"
                    ),
                    DesiredLibrary(
                        key = "androidx-ui-tooling",
                        module = "androidx.compose.ui:ui-tooling"
                    ),
                    DesiredLibrary(
                        key = "androidx-ui-tooling-preview",
                        module = "androidx.compose.ui:ui-tooling-preview"
                    ),
                    DesiredLibrary(
                        key = "compose-material3",
                        module = "androidx.compose.material3:material3"
                    ),
                    DesiredLibrary(
                        key = "compose-navigation",
                        module = "androidx.navigation:navigation-compose",
                        versionRefKey = "composeNavigation"
                    )
                )
            )
        }
    }

private fun desiredPlugins(
    enableCompose: Boolean,
    enableKtlint: Boolean,
    enableDetekt: Boolean
): List<DesiredPlugin> =
    buildList {
        addAll(
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
                )
            )
        )

        if (enableCompose) {
            add(
                DesiredPlugin(
                    id = "org.jetbrains.kotlin.plugin.compose",
                    alias = "compose-compiler",
                    versionRefKey = "kotlin"
                )
            )
        }

        if (enableKtlint) {
            add(
                DesiredPlugin(
                    id = "org.jlleitschuh.gradle.ktlint",
                    alias = "ktlint",
                    versionRefKey = "ktlint"
                )
            )
        }

        if (enableDetekt) {
            add(
                DesiredPlugin(
                    id = "io.gitlab.arturbosch.detekt",
                    alias = "detekt",
                    versionRefKey = "detekt"
                )
            )
        }
    }
