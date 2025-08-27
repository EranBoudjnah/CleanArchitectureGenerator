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

        val featureNameLower = request.featureName.lowercase()
        val featureRoot = File(request.destinationRootDir, "features/$featureNameLower")
        val sourceRoot = File(featureRoot, "src/main/java")
        val destinationDirectory = pathSegments.fold(sourceRoot) { parent, segment -> File(parent, segment) }

        if (destinationDirectory.exists()) {
            return ERROR_PREFIX +
                if (destinationDirectory.isDirectory) {
                    "The feature directory already exists."
                } else {
                    "A file with the feature name exists where the feature directory should be created."
                }
        }
        val createdOrExists =
            if (destinationDirectory.exists()) {
                destinationDirectory.isDirectory
            } else {
                destinationDirectory.mkdirs()
            }
        return if (createdOrExists) {
            "Success!"
        } else {
            "${ERROR_PREFIX}Failed to create directories for package '$packageName'."
        }
    }
}
