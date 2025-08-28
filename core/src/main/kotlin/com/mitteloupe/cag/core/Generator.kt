package com.mitteloupe.cag.core

import com.mitteloupe.cag.core.content.buildDataGradleScript
import com.mitteloupe.cag.core.content.buildDomainGradleScript
import com.mitteloupe.cag.core.content.buildDomainRepositoryKotlinFile
import com.mitteloupe.cag.core.content.buildDomainUseCaseKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationGradleScript
import com.mitteloupe.cag.core.content.buildUiGradleScript
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

const val ERROR_PREFIX = "Error: "

class Generator {
    fun generateFeature(request: GenerateFeatureRequest): String {
        val featurePackageName = request.featurePackageName?.trim()
        if (featurePackageName.isNullOrEmpty()) {
            return "${ERROR_PREFIX}Feature package name is missing."
        }

        val pathSegments = featurePackageName.toSegments()
        if (pathSegments.isEmpty()) {
            return "${ERROR_PREFIX}Feature package name is invalid."
        }

        val featureNameLowerCase = request.featureName.lowercase()
        VersionCatalogUpdater().updateVersionCatalogIfPresent(
            projectRootDir = request.destinationRootDir,
            sectionRequirements =
                listOf(
                    SectionTransaction(
                        sectionHeader = "versions",
                        insertPositionIfMissing = CatalogInsertPosition.Start,
                        requirements = versionCatalogVersionRequirements()
                    ),
                    SectionTransaction(
                        sectionHeader = "plugins",
                        insertPositionIfMissing = CatalogInsertPosition.End,
                        requirements = versionCatalogPluginRequirements()
                    )
                )
        )?.let { return it }
        val featureRoot = File(request.destinationRootDir, "features/$featureNameLowerCase")

        if (featureRoot.exists()) {
            return ERROR_PREFIX +
                if (featureRoot.isDirectory) {
                    "The feature directory already exists."
                } else {
                    "A file with the feature name exists where the feature directory should be created."
                }
        }

        val layers = listOf("ui", "presentation", "domain", "data")

        val allCreated =
            layers.map { layerName ->
                val layerSourceRoot = File(featureRoot, "$layerName/src/main/java")
                val destinationDirectory = buildPackageDirectory(layerSourceRoot, pathSegments)
                if (destinationDirectory.exists()) {
                    destinationDirectory.isDirectory
                } else {
                    destinationDirectory.mkdirs()
                }
            }.all { it }

        if (allCreated) {
            populateDomainModule(featureRoot)?.let { return it }
            writeDomainRepositoryInterface(
                featureRoot = featureRoot,
                featurePackageName = featurePackageName
            )?.let { return it }
            writeDomainUseCaseFile(
                featureRoot = featureRoot,
                projectNamespace = request.projectNamespace,
                featurePackageName = featurePackageName
            )?.let { return it }
            populatePresentationModule(featureRoot, featureNameLowerCase)?.let { return it }
            populateDataModule(featureRoot, featureNameLowerCase)?.let { return it }
            populateUiModule(featureRoot, featurePackageName, featureNameLowerCase)?.let { return it }
            SettingsFileUpdater().updateProjectSettingsIfPresent(
                request.destinationRootDir,
                featureNameLowerCase
            )?.let { return it }
        }

        return if (allCreated) {
            "Success!"
        } else {
            "${ERROR_PREFIX}Failed to create directories for package '$featurePackageName'."
        }
    }

    private fun versionCatalogVersionRequirements(): List<SectionRequirement> =
        listOf(
            SectionRequirement("^\\s*compileSdk\\s*=".toRegex(), "compileSdk = \"35\""),
            SectionRequirement("^\\s*minSdk\\s*=".toRegex(), "minSdk = \"24\""),
            SectionRequirement("^\\s*androidGradlePlugin\\s*=".toRegex(), "androidGradlePlugin = \"8.7.3\"")
        )

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
            )
        )

    private fun populateDomainModule(featureRoot: File): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "domain",
            content = buildDomainGradleScript()
        )

    private fun populateDataModule(
        featureRoot: File,
        featureNameLowerCase: String
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "data",
            content = buildDataGradleScript(featureNameLowerCase)
        )

    private fun populatePresentationModule(
        featureRoot: File,
        featureNameLowerCase: String
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "presentation",
            content = buildPresentationGradleScript(featureNameLowerCase)
        )

    private fun populateUiModule(
        featureRoot: File,
        featurePackageName: String,
        featureNameLowerCase: String
    ): String? =
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "ui",
            content = buildUiGradleScript(featurePackageName, featureNameLowerCase)
        )

    private fun writeDomainUseCaseFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String
    ): String? {
        val content =
            buildDomainUseCaseKotlinFile(
                projectNamespace = projectNamespace,
                featurePackageName = featurePackageName
            )
        return KotlinFileCreator().writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "usecase",
            fileName = "PerformActionUseCase.kt",
            content = content
        )
    }

    private fun writeDomainRepositoryInterface(
        featureRoot: File,
        featurePackageName: String
    ): String? {
        val content = buildDomainRepositoryKotlinFile(featurePackageName)
        return KotlinFileCreator().writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "repository",
            fileName = "PerformExampleRepository.kt",
            content = content
        )
    }
}
