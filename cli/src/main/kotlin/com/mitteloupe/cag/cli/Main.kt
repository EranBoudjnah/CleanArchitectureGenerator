package com.mitteloupe.cag.cli

import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.GenerateFeatureRequestBuilder
import com.mitteloupe.cag.core.GenerateUseCaseRequest
import com.mitteloupe.cag.core.Generator
import com.mitteloupe.cag.core.findGradleProjectRoot
import java.io.File
import java.nio.file.Paths

fun main(arguments: Array<String>) {
    val argumentProcessor = AppArgumentProcessor()
    val projectRoot = findGradleProjectRoot(Paths.get("").toAbsolutePath().toFile()) ?: Paths.get("").toAbsolutePath().toFile()
    val projectModel = FilesystemProjectModel(projectRoot)
    val basePackage = BasePackageResolver().determineBasePackage(projectModel)

    if (argumentProcessor.isHelpRequested(arguments)) {
        println(
            """
            usage: cag [--new-feature=FeatureName [--package=PackageName]]... [--new-datasource=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case=UseCaseName [--path=TargetPath]]...

            Options:
              --new-feature=FeatureName | --new-feature FeatureName | -nf=FeatureName | -nf FeatureName | -nfFeatureName
                Generate a new feature named FeatureName
              --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
                Override the feature package for the preceding feature
              --new-datasource=Name | --new-datasource Name | -nds=Name | -nds Name | -ndsName
                Generate a new data source named NameDataSource
              --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
                Attach dependencies to the preceding new data source
              --new-use-case=UseCaseName | --new-use-case UseCaseName | -nuc=UseCaseName | -nuc UseCaseName | -nucUseCaseName
                Generate a new use case named UseCaseName
              --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
                Specify the target directory for the preceding use case
              --help, -h
                Show this help message and exit
            """.trimIndent()
        )
        return
    }

    val featureRequests = argumentProcessor.getNewFeatures(arguments)
    val dataSourceRequests = argumentProcessor.getNewDataSources(arguments)
    val useCaseRequests = argumentProcessor.getNewUseCases(arguments)
    if (featureRequests.isEmpty() && dataSourceRequests.isEmpty() && useCaseRequests.isEmpty()) {
        println(
            """
            usage: cag [--new-feature=FeatureName [--package=PackageName]]... [--new-datasource=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case=UseCaseName [--path=TargetPath]]...
            Run with --help or -h for more options.
            """.trimIndent()
        )
        return
    }

    val generator = Generator()
    val destinationRootDir = projectModel.selectedModuleRootDir() ?: projectRoot
    val projectNamespace = basePackage ?: "com.unknown.app."

    featureRequests.forEach { requestFeature ->
        val packageName =
            requestFeature.packageName ?: basePackage?.let { "$it${requestFeature.featureName.lowercase()}" }

        val request =
            GenerateFeatureRequestBuilder(
                destinationRootDir = destinationRootDir,
                projectNamespace = projectNamespace,
                featureName = requestFeature.featureName
            ).featurePackageName(packageName)
                .enableCompose(true)
                .build()
        val result = generator.generateFeature(request)
        println(result)
    }

    dataSourceRequests.forEach { request ->
        val result =
            generator.generateDataSource(
                destinationRootDirectory = destinationRootDir,
                dataSourceName = request.dataSourceName,
                projectNamespace = projectNamespace,
                useKtor = request.useKtor,
                useRetrofit = request.useRetrofit
            )
        println(result)
    }

    useCaseRequests.forEach { request ->
        val targetDirectory =
            if (request.targetPath != null) {
                val path = Paths.get(request.targetPath)
                if (path.isAbsolute) {
                    path.toFile()
                } else {
                    File(destinationRootDir, request.targetPath)
                }
            } else {
                Paths.get("").toAbsolutePath().toFile()
            }

        val useCaseRequest =
            GenerateUseCaseRequest.Builder(
                destinationDirectory = targetDirectory,
                useCaseName = request.useCaseName
            ).build()

        val result = generator.generateUseCase(useCaseRequest)
        println(result)
    }
}
