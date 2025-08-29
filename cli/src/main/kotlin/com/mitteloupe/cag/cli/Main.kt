package com.mitteloupe.cag.cli

import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.GenerateFeatureRequestBuilder
import com.mitteloupe.cag.core.Generator
import com.mitteloupe.cag.core.findGradleProjectRoot
import java.nio.file.Paths

fun main(args: Array<String>) {
    val projectRoot = findGradleProjectRoot(Paths.get("").toAbsolutePath().toFile()) ?: Paths.get("").toAbsolutePath().toFile()
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
    val generator = Generator()
    val request =
        GenerateFeatureRequestBuilder(
            destinationRootDir = projectModel.selectedModuleRootDir() ?: projectRoot,
            projectNamespace = basePackage ?: "com.unknown.app.",
            featureName = featureName
        ).featurePackageName(packageName)
            .enableCompose(true)
            .build()
    val result = generator.generateFeature(request)
    println(result)
}
