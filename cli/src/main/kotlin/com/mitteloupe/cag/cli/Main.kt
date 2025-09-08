package com.mitteloupe.cag.cli

import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.GenerateArchitectureRequest
import com.mitteloupe.cag.core.GenerateFeatureRequestBuilder
import com.mitteloupe.cag.core.GenerateProjectTemplateRequest
import com.mitteloupe.cag.core.GenerateUseCaseRequest
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.Generator
import com.mitteloupe.cag.core.findGradleProjectRoot
import java.io.File
import java.nio.file.Paths
import java.util.UUID
import kotlin.system.exitProcess

fun main(arguments: Array<String>) {
    val argumentProcessor = AppArgumentProcessor()
    val projectRoot = findGradleProjectRoot(Paths.get("").toAbsolutePath().toFile()) ?: Paths.get("").toAbsolutePath().toFile()
    val projectModel = FilesystemProjectModel(projectRoot)
    val basePackage = BasePackageResolver().determineBasePackage(projectModel)

    if (argumentProcessor.isHelpRequested(arguments)) {
        printHelpMessage()
        return
    }

    try {
        argumentProcessor.validateNoUnknownFlags(arguments)
    } catch (exception: IllegalArgumentException) {
        println("Error: ${exception.message}")
        printUsageMessage()
        exitProcess(1)
    }

    val projectTemplateRequests = argumentProcessor.getNewProjectTemplate(arguments)
    val architectureRequests = argumentProcessor.getNewArchitecture(arguments)
    val featureRequests = argumentProcessor.getNewFeatures(arguments)
    val dataSourceRequests = argumentProcessor.getNewDataSources(arguments)
    val useCaseRequests = argumentProcessor.getNewUseCases(arguments)
    if (projectTemplateRequests.isEmpty() &&
        architectureRequests.isEmpty() &&
        featureRequests.isEmpty() &&
        dataSourceRequests.isEmpty() &&
        useCaseRequests.isEmpty()
    ) {
        printUsageMessage()
        return
    }

    val generator = Generator()
    val destinationRootDir = projectModel.selectedModuleRootDir() ?: projectRoot
    val projectNamespace = basePackage ?: "com.unknown.app."

    projectTemplateRequests.forEach { request ->
        val projectTemplateDestinationDirectory =
            if (projectModel.selectedModuleRootDir() != null) {
                projectRoot
            } else {
                destinationRootDir
            }
        val projectTemplateRequest =
            GenerateProjectTemplateRequest(
                requestId = UUID.randomUUID().toString(),
                destinationRootDirectory = projectTemplateDestinationDirectory,
                projectName = request.projectName,
                packageName = request.packageName,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt,
                enableKtor = request.enableKtor,
                enableRetrofit = request.enableRetrofit
            )
        executeAndReport {
            generator.generateProjectTemplate(projectTemplateRequest)
        }
    }

    architectureRequests.forEach { request ->
        val architecturePackageName = basePackage?.let { it.trimEnd('.') + ".architecture" } ?: "com.unknown.app.architecture"
        val architectureRequest =
            GenerateArchitectureRequest(
                destinationRootDirectory = destinationRootDir,
                architecturePackageName = architecturePackageName,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt
            )
        executeAndReport {
            generator.generateArchitecture(architectureRequest)
        }
    }

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
        executeAndReport {
            generator.generateFeature(request)
        }
    }

    dataSourceRequests.forEach { request ->
        executeAndReport {
            generator.generateDataSource(
                destinationRootDirectory = destinationRootDir,
                dataSourceName = request.dataSourceName,
                projectNamespace = projectNamespace,
                useKtor = request.useKtor,
                useRetrofit = request.useRetrofit
            )
        }
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
            )
                .inputDataType(request.inputDataType)
                .outputDataType(request.outputDataType)
                .build()

        executeAndReport {
            generator.generateUseCase(useCaseRequest)
        }
    }
}

private fun printUsageMessage() {
    println(
        """
        usage: cag [--new-project --name=ProjectName --package=PackageName [--no-compose] [--ktlint] [--detekt] [--ktor] [--retrofit]]... [--new-architecture [--no-compose] [--ktlint] [--detekt]]... [--new-feature --name=FeatureName [--package=PackageName]]... [--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case --name=UseCaseName [--path=TargetPath]]...

        Run with --help or -h for more options.
        """.trimIndent()
    )
}

private fun printHelpMessage() {
    println(
        """
        usage: cag [--new-project --name=ProjectName --package=PackageName [--no-compose] [--ktlint] [--detekt] [--ktor] [--retrofit]]... [--new-architecture [--no-compose] [--ktlint] [--detekt]]... [--new-feature --name=FeatureName [--package=PackageName]]... [--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit]]... [--new-use-case --name=UseCaseName [--path=TargetPath]]...

        Note: You must use either long form (--flag) or short form (-f) arguments consistently throughout your command. Mixing both forms is not allowed.

        Options:
          --new-project | -np
              Generate a complete Clean Architecture project template
            --name=ProjectName | -n=ProjectName | -n ProjectName | -nProjectName
                Specify the project name (required)
            --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
                Specify the package name (required)
            --no-compose | -nc
              Disable Compose support for the project
            --ktlint | -kl
              Enable ktlint for the project
            --detekt | -d
              Enable detekt for the project
            --ktor | -kt
              Enable Ktor for data sources
            --retrofit | -rt
              Enable Retrofit for data sources
          --new-architecture | -na
              Generate a new Clean Architecture package with domain, presentation, and UI layers
            --no-compose | -nc
              Disable Compose support for the preceding architecture package
            --ktlint | -kl
              Enable ktlint for the preceding architecture package
            --detekt | -d
              Enable detekt for the preceding architecture package
          --new-feature | -nf
              Generate a new feature
            --name=FeatureName | -n=FeatureName | -n FeatureName | -nFeatureName
                Specify the feature name (required)
            --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
                Override the feature package for the preceding feature
          --new-datasource | -nds
              Generate a new data source
            --name=DataSourceName | -n=DataSourceName | -n DataSourceName | -nDataSourceName
                Specify the data source name (required, DataSource suffix will be added automatically)
            --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
                Attach dependencies to the preceding new data source
          --new-use-case | -nuc
              Generate a new use case
            --name=UseCaseName | -n=UseCaseName | -n UseCaseName | -nUseCaseName
                Specify the use case name (required)
            --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
                Specify the target directory for the preceding use case
          --help, -h
              Show this help message and exit
        """.trimIndent()
    )
}

private fun executeAndReport(operation: () -> Unit) =
    try {
        operation()
        println("Done!")
    } catch (exception: GenerationException) {
        println("Error: ${exception.message}")
        exitProcess(1)
    }
