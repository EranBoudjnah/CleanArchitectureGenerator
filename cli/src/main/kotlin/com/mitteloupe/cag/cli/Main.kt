package com.mitteloupe.cag.cli

import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.DefaultGenerator
import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.GenerateFeatureRequestBuilder
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) {
    val projectRoot = findGradleProjectRoot(Paths.get("").toAbsolutePath().toFile())
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
    val request =
        GenerateFeatureRequestBuilder(
            destinationRootDir = projectModel.selectedModuleRootDir() ?: projectRoot,
            projectNamespace = basePackage ?: "com.unknown.app.",
            featureName = featureName
        ).featurePackageName(packageName)
            .build()
    val result = generator.generateFeature(request)
    println(result)
}

private fun findGradleProjectRoot(startDirectory: File): File =
    DirectoryFinder().findDirectory(startDirectory) { currentDirectory ->
        val hasSettings =
            File(currentDirectory, "settings.gradle.kts").exists() || File(currentDirectory, "settings.gradle").exists()
        val hasWrapper = File(currentDirectory, "gradlew").exists() || File(currentDirectory, "gradlew.bat").exists()
        hasSettings || hasWrapper
    } ?: startDirectory
