package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import java.io.File

data class SectionTransaction(
    val sectionHeader: String,
    val insertPositionIfMissing: CatalogInsertPosition,
    val requirements: List<SectionRequirement>
)

data class SectionRequirement(val keyRegex: Regex, val lineToAdd: String)

class VersionCatalogUpdater(
    private val contentUpdater: VersionCatalogContentUpdater = VersionCatalogContentUpdater()
) {
    fun updateVersionCatalogIfPresent(projectRootDirectory: File): String? {
        val catalogTextBefore = readCatalogFile(projectRootDirectory)

        updateVersionCatalogIfPresent(
            projectRootDirectory = projectRootDirectory,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        sectionHeader = "libraries",
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = versionCatalogLibraryRequirements()
                    ),
                    SectionTransaction(
                        sectionHeader = "plugins",
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = versionCatalogPluginRequirements()
                    )
                )
        )?.let { return it }

        val catalogTextAfter = readCatalogFile(projectRootDirectory)
        val addedAndroidLibraryAlias =
            (catalogTextAfter?.contains(Regex("(?m)^\\s*android-library\\s*=")) == true) &&
                (catalogTextBefore?.contains(Regex("(?m)^\\s*android-library\\s*=")) != true)
        val addedComposeBomAlias =
            (catalogTextAfter?.contains(Regex("(?m)^\\s*compose-bom\\s*=")) == true) &&
                (catalogTextBefore?.contains(Regex("(?m)^\\s*compose-bom\\s*=")) != true)

        return updateVersionCatalogIfPresent(
            projectRootDirectory = projectRootDirectory,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        sectionHeader = "versions",
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

    private fun readCatalogFile(projectRootDirectory: File): String? {
        val catalogDirectory = File(projectRootDirectory, "gradle")
        val catalogFile = File(catalogDirectory, "libs.versions.toml")
        return runCatching { if (catalogFile.exists()) catalogFile.readText() else null }.getOrNull()
    }

    private fun updateVersionCatalogIfPresent(
        projectRootDirectory: File,
        sectionRequirements: List<SectionTransaction>
    ): String? {
        val catalogFile = File(projectRootDirectory, "gradle/libs.versions.toml")
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
    ): List<SectionRequirement> {
        val requirements =
            mutableListOf(
                SectionRequirement("^\\s*compileSdk\\s*=".toRegex(), "compileSdk = \"35\""),
                SectionRequirement("^\\s*minSdk\\s*=".toRegex(), "minSdk = \"24\"")
            )
        if (includeAndroidGradlePlugin) {
            requirements.add(
                SectionRequirement(
                    "^\\s*androidGradlePlugin\\s*=".toRegex(),
                    "androidGradlePlugin = \"8.7.3\""
                )
            )
        }
        if (includeComposeBom) {
            requirements.add(
                SectionRequirement(
                    "^\\s*composeBom\\s*=".toRegex(),
                    "composeBom = \"2025.08.01\""
                )
            )
        }
        return requirements
    }

    private fun versionCatalogPluginRequirements(): List<SectionRequirement> =
        listOf(
            SectionRequirement(
                "^\\s*kotlin-jvm\\s*=".toRegex(),
                "kotlin-jvm = { id = \"org.jetbrains.kotlin.jvm\", version.ref = \"kotlin\" }"
            ),
            SectionRequirement(
                "^\\s*kotlin-android\\s*=".toRegex(),
                "kotlin-android = { id = \"org.jetbrains.kotlin.android\", version.ref = \"kotlin\" }"
            ),
            SectionRequirement(
                "^\\s*android-library\\s*=".toRegex(),
                "android-library = { id = \"com.android.library\", version.ref = \"androidGradlePlugin\" }"
            ),
            SectionRequirement(
                "^\\s*compose-compiler\\s*=".toRegex(),
                "compose-compiler = { id = \"org.jetbrains.kotlin.plugin.compose\", version.ref = \"kotlin\" }"
            )
        )

    private fun versionCatalogLibraryRequirements(): List<SectionRequirement> =
        listOf(
            SectionRequirement(
                "^\\s*compose-bom\\s*=.*$".toRegex(),
                "compose-bom = { module = \"androidx.compose:compose-bom\", version.ref = \"composeBom\" }"
            ),
            SectionRequirement(
                "^\\s*compose-ui\\s*=.*$".toRegex(),
                "compose-ui = { module = \"androidx.compose.ui:ui\" }"
            ),
            SectionRequirement(
                "^\\s*compose-ui-graphics\\s*=.*$".toRegex(),
                "compose-ui-graphics = { module = \"androidx.compose.ui:ui-graphics\" }"
            ),
            SectionRequirement(
                "^\\s*androidx-ui-tooling\\s*=.*$".toRegex(),
                "androidx-ui-tooling = { module = \"androidx.compose.ui:ui-tooling\" }"
            ),
            SectionRequirement(
                "^\\s*androidx-ui-tooling-preview\\s*=.*$".toRegex(),
                "androidx-ui-tooling-preview = { module = \"androidx.compose.ui:ui-tooling-preview\" }"
            ),
            SectionRequirement(
                "^\\s*compose-material3\\s*=.*$".toRegex(),
                "compose-material3 = { module = \"androidx.compose.material3:material3\" }"
            )
        )
}
