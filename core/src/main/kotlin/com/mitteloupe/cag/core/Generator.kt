package com.mitteloupe.cag.core

import java.io.File

interface Generator {
    fun generateFeature(request: GenerateFeatureRequest): String
}

class DefaultGenerator : Generator {
    override fun generateFeature(request: GenerateFeatureRequest): String {
        val packageName = request.featurePackageName?.trim()
        if (packageName.isNullOrEmpty()) {
            return "Error: Feature package name is missing."
        }

        val pathSegments = packageName.split('.').filter { it.isNotBlank() }
        if (pathSegments.isEmpty()) {
            return "Error: Feature package name is invalid."
        }

        val sourceRoot = resolveSourceRoot(request.destinationRootDir)
        val destinationDirectory = pathSegments.fold(sourceRoot) { parent, segment -> File(parent, segment) }

        val createdOrExists =
            if (destinationDirectory.exists()) {
                destinationDirectory.isDirectory
            } else {
                destinationDirectory.mkdirs()
            }
        return if (createdOrExists) {
            "Success!"
        } else {
            "Error: Failed to create directories for package '$packageName'."
        }
    }

    private fun resolveSourceRoot(moduleRoot: File): File {
        val kotlinDir = File(moduleRoot, "src/main/kotlin")
        if (kotlinDir.exists()) return kotlinDir
        val javaDir = File(moduleRoot, "src/main/java")
        if (javaDir.exists()) return javaDir
        return moduleRoot
    }
}
