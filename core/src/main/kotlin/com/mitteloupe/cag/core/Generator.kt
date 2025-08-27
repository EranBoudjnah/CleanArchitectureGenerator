package com.mitteloupe.cag.core

import java.io.File

interface Generator {
    fun generateFeature(request: GenerateFeatureRequest): String
}

private const val ERROR_PREFIX = "Error: "

class DefaultGenerator : Generator {
    override fun generateFeature(request: GenerateFeatureRequest): String {
        val packageName = request.featurePackageName?.trim()
        if (packageName.isNullOrEmpty()) {
            return "${ERROR_PREFIX}Feature package name is missing."
        }

        val pathSegments = packageName.split('.').filter { it.isNotBlank() }
        if (pathSegments.isEmpty()) {
            return "${ERROR_PREFIX}Feature package name is invalid."
        }

        val featureNameLowerCase = request.featureName.lowercase()
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
            populateDataModule(featureRoot, featureNameLowerCase)?.let { return it }
        }

        return if (allCreated) {
            "Success!"
        } else {
            "${ERROR_PREFIX}Failed to create directories for package '$packageName'."
        }
    }

    private fun populateDomainModule(featureRoot: File): String? =
        writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "domain",
            content = buildDomainGradleScript()
        )

    private fun populateDataModule(
        featureRoot: File,
        featureNameLowerCase: String
    ): String? =
        writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "data",
            content = buildDataGradleScript(featureNameLowerCase)
        )

    private fun buildPackageDirectory(
        root: File,
        packageSegments: List<String>
    ): File = packageSegments.fold(root) { parent, segment -> File(parent, segment) }

    private fun writeGradleFileIfMissing(
        featureRoot: File,
        layer: String,
        content: String
    ): String? {
        val moduleDirectory = File(featureRoot, layer)
        val buildGradleFile = File(moduleDirectory, "build.gradle.kts")
        if (!buildGradleFile.exists()) {
            runCatching { buildGradleFile.writeText(content) }
                .onFailure { return "${ERROR_PREFIX}Failed to create $layer/build.gradle.kts: ${it.message}" }
        }
        return null
    }

    private fun buildDataGradleScript(featureNameLowerCase: String): String =
        """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.features.$featureNameLowerCase.domain)
    implementation(projects.architecture.domain)

    implementation(projects.datasource.architecture)
    implementation(projects.datasource.source)
}
"""

    private fun buildDomainGradleScript(): String =
        """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.architecture.domain)
}
"""
}
