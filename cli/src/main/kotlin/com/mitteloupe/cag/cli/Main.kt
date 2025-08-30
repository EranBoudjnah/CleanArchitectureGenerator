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

    if (argumentParser.isHelpRequested(args)) {
        println(
            """
            usage: cag [--new-feature=FeatureName [--package=PackageName]]... [--new-datasource=DataSourceName]...

            Options:
              --new-feature=FeatureName | --new-feature FeatureName | -nf=FeatureName | -nf FeatureName | -nfFeatureName
                                                        Generate a new feature named FeatureName
              --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
                                                        Override the feature package for the preceding feature
              --new-datasource=Name | --new-datasource Name | -nds=Name | -nds Name | -ndsName
                                                        Generate a new data source named NameDataSource
              --help, -h                                    Show this help message and exit
            """.trimIndent()
        )
        return
    }

    val featureNames = argumentParser.parseFeatureNames(args)
    val featurePackages = argumentParser.parseFeaturePackages(args)
    val dataSourceNames = argumentParser.parseDataSourceNames(args)
    if (featureNames.isEmpty() && dataSourceNames.isEmpty()) {
        println(
            """
            usage: cag [--new-feature=FeatureName [--package=PackageName]]... [--new-datasource=DataSourceName]...
            Run with --help or -h for more options.
            """.trimIndent()
        )
        return
    }

    val generator = Generator()
    val destinationRootDir = projectModel.selectedModuleRootDir() ?: projectRoot
    val projectNamespace = basePackage ?: "com.unknown.app."

    featureNames.forEachIndexed { index, featureName ->
        val explicitPackage = featurePackages.getOrNull(index)
        val packageName =
            if (explicitPackage != null) {
                explicitPackage
            } else if (basePackage == null) {
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
