package com.mitteloupe.cag.cli

import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.GenerateFeatureRequestBuilder
import com.mitteloupe.cag.core.Generator
import com.mitteloupe.cag.core.findGradleProjectRoot
import java.nio.file.Paths

fun main(args: Array<String>) {
    val argumentParser = ArgumentParser()
    val projectRoot = findGradleProjectRoot(Paths.get("").toAbsolutePath().toFile()) ?: Paths.get("").toAbsolutePath().toFile()
    val projectModel = FilesystemProjectModel(projectRoot)
    val basePackage = BasePackageResolver().determineBasePackage(projectModel)

    val featureNames = argumentParser.parseFeatureNames(args)
    val dataSourceNames = argumentParser.parseDataSourceNames(args)
    if (featureNames.isEmpty() && dataSourceNames.isEmpty()) {
        println("usage: cag [--new-feature=FeatureName]... [--new-datasource=DataSourceName]...")
        return
    }

    val generator = Generator()
    val destinationRootDir = projectModel.selectedModuleRootDir() ?: projectRoot
    val projectNamespace = basePackage ?: "com.unknown.app."

    featureNames.forEach { featureName ->
        val packageName =
            if (basePackage == null) {
                null
            } else {
                "$basePackage${featureName.lowercase()}"
            }

        val request =
            GenerateFeatureRequestBuilder(
                destinationRootDir = destinationRootDir,
                projectNamespace = projectNamespace,
                featureName = featureName
            ).featurePackageName(packageName)
                .enableCompose(true)
                .build()
        val result = generator.generateFeature(request)
        println(result)
    }

    dataSourceNames.forEach { dataSourceName ->
        val result =
            generator.generateDataSource(
                destinationRootDirectory = destinationRootDir,
                dataSourceName = dataSourceName,
                projectNamespace = projectNamespace
            )
        println(result)
    }
}
