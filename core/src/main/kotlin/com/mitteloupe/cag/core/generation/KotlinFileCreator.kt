package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class KotlinFileCreator(private val fileCreator: FileCreator) {
    fun writeKotlinFileInLayer(
        featureRoot: File,
        layer: String,
        featurePackageName: String,
        relativePackageSubPath: String,
        fileName: String,
        content: String
    ) {
        val sourceRoot = File(featureRoot, "$layer/src/main/java")
        val packageSegments = featurePackageName.toSegments()
        val basePackageDirectory = buildPackageDirectory(sourceRoot, packageSegments + layer)
        val targetDirectory =
            relativePackageSubPath.toSegments()
                .fold(basePackageDirectory) { parent, segment -> File(parent, segment) }

        if (!targetDirectory.exists()) {
            println("ERAN: Creating directory $targetDirectory")
            fileCreator.createDirectoryIfNotExists(targetDirectory)
        } else if (!targetDirectory.isDirectory) {
            throw GenerationException("Failed to create directory: ${targetDirectory.absolutePath} (Not a directory)")
        }

        println("ERAN: Creating file in $targetDirectory")

        writeKotlinFileInDirectory(targetDirectory, fileName, content)
    }

    fun writeKotlinFileInDirectory(
        targetDirectory: File,
        fileName: String,
        content: String
    ) {
        val targetFile = File(targetDirectory, fileName)
        if (targetFile.exists() && !targetFile.isFile) {
            throw GenerationException("Failed to create file: ${targetFile.absolutePath} (it's a directory)")
        }
        fileCreator.createFileIfNotExists(targetFile) { content }
    }
}
