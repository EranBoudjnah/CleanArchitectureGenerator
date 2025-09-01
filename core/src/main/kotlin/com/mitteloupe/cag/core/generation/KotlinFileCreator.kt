package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class KotlinFileCreator {
    fun writeKotlinFileInLayer(
        featureRoot: File,
        layer: String,
        featurePackageName: String,
        relativePackageSubPath: String,
        fileName: String,
        content: String
    ): String? {
        val sourceRoot = File(featureRoot, "$layer/src/main/java")
        val packageSegments = featurePackageName.toSegments()
        val basePackageDirectory = buildPackageDirectory(sourceRoot, packageSegments)
        val targetDirectory =
            (listOf(layer) + relativePackageSubPath.toSegments())
                .fold(basePackageDirectory) { parent, segment -> File(parent, segment) }

        if (!targetDirectory.exists()) {
            val created = runCatching { targetDirectory.mkdirs() }.getOrElse { false }
            if (!created) {
                return "${ERROR_PREFIX}Failed to create directory: ${targetDirectory.absolutePath}"
            }
        }

        return writeKotlinFileInLayer(targetDirectory, fileName, content)
    }

    fun writeKotlinFileInLayer(
        targetDirectory: File,
        fileName: String,
        content: String
    ): String? {
        val targetFile = File(targetDirectory, fileName)
        if (!targetFile.exists()) {
            runCatching { targetFile.writeText(content) }
                .onFailure {
                    return "${ERROR_PREFIX}Failed to create file: ${targetFile.absolutePath}: ${it.message}"
                }
        }
        return null
    }
}
