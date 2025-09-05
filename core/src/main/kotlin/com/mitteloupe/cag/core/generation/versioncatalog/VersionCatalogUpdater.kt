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
        includeCoroutines: Boolean = false,
        enableKtlint: Boolean = false,
        enableDetekt: Boolean = false
    ) {
        updateBasicVersions(projectRootDir)
        addEssentialAndroidVersions(projectRootDir)
        addKotlinPlugins(projectRootDir, enableCompose, enableKtlint, enableDetekt)
        addAndroidPlugins(projectRootDir)

        if (enableCompose || includeCoroutines) {
            addAndroidLibraries(projectRootDir, enableCompose, includeCoroutines)
        }
        if (enableCompose || includeCoroutines || enableKtlint || enableDetekt) {
            addTestLibraries(projectRootDir, enableCompose)
        }
    }

    fun updateBasicVersions(projectRootDir: File) {
        val versionRequirements = basicVersionRequirements()

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements = versionRequirements
                    )
                )
        )
    }

    fun addEssentialAndroidVersions(projectRootDir: File) {
        val versionRequirements =
            listOf(
                SectionEntryRequirement.VersionRequirement(key = "targetSdk", version = "35"),
                SectionEntryRequirement.VersionRequirement(key = "androidGradlePlugin", version = "8.7.3")
            )

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements = versionRequirements
                    )
                )
        )
    }

    fun addAndroidLibraries(
        projectRootDir: File,
        enableCompose: Boolean = false,
        includeCoroutines: Boolean = false
    ) {
        val catalogTextBefore = readCatalogFile(projectRootDir)
        val existingLibraryAliasToModule: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingLibraryAliasToModule(it) } ?: emptyMap()

        val existingLibraryModuleToAlias: Map<String, String> =
            existingLibraryAliasToModule.entries.associate { (alias, module) ->
                module to alias
            }

        val desiredLibraries = androidLibraries(enableCompose, includeCoroutines)

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

        resolvedLibraryModuleToAlias = resolvedLibraryModuleToAliasMutable.toMap()

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = libraryRequirements
                    )
                )
        )
    }

    fun addTestLibraries(
        projectRootDir: File,
        enableCompose: Boolean = false
    ) {
        val versionRequirements =
            listOf(
                SectionEntryRequirement.VersionRequirement(key = "junit4", version = "4.13.2")
            )

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements = versionRequirements
                    )
                )
        )

        val catalogTextBefore = readCatalogFile(projectRootDir)
        val existingLibraryAliasToModule: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingLibraryAliasToModule(it) } ?: emptyMap()

        val existingLibraryModuleToAlias: Map<String, String> =
            existingLibraryAliasToModule.entries.associate { (alias, module) ->
                module to alias
            }

        val desiredLibraries = testLibraries(enableCompose)

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

        resolvedLibraryModuleToAlias = resolvedLibraryModuleToAliasMutable.toMap()

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = libraryRequirements
                    )
                )
        )
    }

    fun addKotlinPlugins(
        projectRootDir: File,
        enableCompose: Boolean = false,
        enableKtlint: Boolean = false,
        enableDetekt: Boolean = false
    ) {
        val catalogTextBefore = readCatalogFile(projectRootDir)
        val existingPluginIdToAlias: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingPluginIdToAlias(it) } ?: emptyMap()
        val existingPluginAliasToId: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingPluginAliasToId(it) } ?: emptyMap()

        val desiredPlugins = kotlinPlugins(enableCompose, enableKtlint, enableDetekt)

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

        resolvedPluginIdToAlias = resolvedPluginIdToAliasMutable.toMap()

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = pluginRequirements
                    )
                )
        )
    }

    fun addAndroidPlugins(projectRootDir: File) {
        val catalogTextBefore = readCatalogFile(projectRootDir)
        val existingPluginIdToAlias: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingPluginIdToAlias(it) } ?: emptyMap()
        val existingPluginAliasToId: Map<String, String> =
            catalogTextBefore?.let { contentUpdater.parseExistingPluginAliasToId(it) } ?: emptyMap()

        val desiredPlugins = androidPlugins()

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

        resolvedPluginIdToAlias = resolvedPluginIdToAliasMutable.toMap()

        updateVersionCatalogIfPresent(
            projectRootDir = projectRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = pluginRequirements
                    )
                )
        )
    }

    fun createInitialVersionCatalog(
        projectRootDir: File,
        enableCompose: Boolean = false,
        includeCoroutines: Boolean = false,
        enableKtlint: Boolean = false,
        enableDetekt: Boolean = false
    ) {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (catalogFile.exists()) {
            updateVersionCatalogIfPresent(
                projectRootDir = projectRootDir,
                enableCompose = enableCompose,
                includeCoroutines = includeCoroutines,
                enableKtlint = enableKtlint,
                enableDetekt = enableDetekt
            )
            return
        }

        val catalogDirectory = catalogFile.parentFile
        if (!catalogDirectory.exists()) {
            if (!catalogDirectory.mkdirs()) {
                throw GenerationException("Failed to create gradle directory")
            }
        }

        val desiredPlugins = desiredPlugins(enableCompose, enableKtlint, enableDetekt)
        val desiredLibraries = desiredLibraries(enableCompose, includeCoroutines)

        val pluginRequirements =
            desiredPlugins.map { desired ->
                SectionEntryRequirement.PluginRequirement(
                    key = desired.alias,
                    id = desired.id,
                    versionRefKey = desired.versionRefKey
                )
            }

        val libraryRequirements =
            desiredLibraries.map { desired ->
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
            versionCatalogVersionRequirements(
                includeAndroidGradlePlugin = addedAndroidLibraryAlias,
                includeComposeBom = addedComposeBomAlias,
                includeDetekt = addedDetektAlias
            )

        val initialContent =
            contentUpdater.createInitialCatalogText(
                listOf(
                    SectionTransaction(
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements = versionRequirements
                    ),
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

        runCatching { catalogFile.writeText(initialContent) }
            .exceptionOrNull()
            ?.let { throw GenerationException("Failed to create version catalog: ${it.message}") }
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
            addAll(basicVersionRequirements())
            if (includeAndroidGradlePlugin) {
                add(SectionEntryRequirement.VersionRequirement(key = "targetSdk", version = "35"))
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
                add(
                    SectionEntryRequirement.VersionRequirement(
                        key = "composeCompiler",
                        version = "1.5.8"
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

private fun basicVersionRequirements(): List<SectionEntryRequirement> =
    listOf(
        SectionEntryRequirement.VersionRequirement(key = "kotlin", version = "2.1.0"),
        SectionEntryRequirement.VersionRequirement(key = "compileSdk", version = "35"),
        SectionEntryRequirement.VersionRequirement(key = "minSdk", version = "24")
    )

private fun androidLibraries(
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

        addAll(
            listOf(
                DesiredLibrary(
                    key = "androidx-fragment-ktx",
                    module = "androidx.fragment:fragment-ktx",
                    versionLiteral = "1.6.2"
                ),
                DesiredLibrary(
                    key = "androidx-navigation-fragment-ktx",
                    module = "androidx.navigation:navigation-fragment-ktx",
                    versionLiteral = "2.7.6"
                ),
                DesiredLibrary(
                    key = "material",
                    module = "com.google.android.material:material",
                    versionLiteral = "1.11.0"
                ),
                DesiredLibrary(
                    key = "androidx-appcompat",
                    module = "androidx.appcompat:appcompat",
                    versionLiteral = "1.6.1"
                ),
                DesiredLibrary(
                    key = "androidx-recyclerview",
                    module = "androidx.recyclerview:recyclerview",
                    versionLiteral = "1.3.2"
                ),
                DesiredLibrary(
                    key = "okhttp3",
                    module = "com.squareup.okhttp3:okhttp",
                    versionLiteral = "4.12.0"
                )
            )
        )
    }

private fun testLibraries(enableCompose: Boolean): List<DesiredLibrary> =
    buildList {
        addAll(
            listOf(
                DesiredLibrary(
                    key = "test-junit",
                    module = "junit:junit",
                    versionRefKey = "junit4"
                ),
                DesiredLibrary(
                    key = "test-androidx-junit",
                    module = "androidx.test.ext:junit",
                    versionLiteral = "1.1.5"
                ),
                DesiredLibrary(
                    key = "test-androidx-espresso-core",
                    module = "androidx.test.espresso:espresso-core",
                    versionLiteral = "3.5.1"
                ),
                DesiredLibrary(
                    key = "test-android-hilt",
                    module = "com.google.dagger:hilt-android-testing",
                    versionLiteral = "2.48"
                ),
                DesiredLibrary(
                    key = "test-android-uiautomator",
                    module = "androidx.test.uiautomator:uiautomator",
                    versionLiteral = "2.2.0"
                ),
                DesiredLibrary(
                    key = "test-android-mockwebserver",
                    module = "com.squareup.okhttp3:mockwebserver",
                    versionLiteral = "4.12.0"
                ),
                DesiredLibrary(
                    key = "test-androidx-rules",
                    module = "androidx.test:rules",
                    versionLiteral = "1.5.0"
                )
            )
        )

        if (enableCompose) {
            add(
                DesiredLibrary(
                    key = "test-compose-ui-junit4",
                    module = "androidx.compose.ui:ui-test-junit4",
                    versionRefKey = "composeBom"
                )
            )
        }
    }

private fun kotlinPlugins(
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
                    id = "com.google.devtools.ksp",
                    alias = "ksp",
                    versionRefKey = "kotlin"
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

private fun androidPlugins(): List<DesiredPlugin> =
    listOf(
        DesiredPlugin(
            id = "com.android.application",
            alias = "android-application",
            versionRefKey = "androidGradlePlugin"
        ),
        DesiredPlugin(
            id = "com.android.library",
            alias = "android-library",
            versionRefKey = "androidGradlePlugin"
        )
    )

private fun desiredLibraries(
    enableCompose: Boolean,
    includeCoroutineDependencies: Boolean
): List<DesiredLibrary> =
    buildList {
        addAll(androidLibraries(enableCompose, includeCoroutineDependencies))
        addAll(testLibraries(enableCompose))
    }

private fun desiredPlugins(
    enableCompose: Boolean,
    enableKtlint: Boolean,
    enableDetekt: Boolean
): List<DesiredPlugin> =
    buildList {
        addAll(kotlinPlugins(enableCompose, enableKtlint, enableDetekt))
        addAll(androidPlugins())
    }
