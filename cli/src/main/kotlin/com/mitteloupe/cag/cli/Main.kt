package com.mitteloupe.cag.cli

import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.DefaultGenerator
import java.nio.file.Paths

fun main(args: Array<String>) {
    val projectRoot = Paths.get("").toAbsolutePath().toFile()
    val projectModel = FilesystemProjectModel(projectRoot)
    val basePackage = BasePackageResolver().determineBasePackage(projectModel)

    val featureName = args.firstOrNull() ?: "SampleFeature"
    val packageName =
        if (basePackage == null) {
            null
        } else {
            "$basePackage${featureName.lowercase()}"
        }
    println("Package: ${packageName ?: "<not found>" }")
    val generator = DefaultGenerator()
    val result = generator.generateFeature(featureName)
    println(result)
}
