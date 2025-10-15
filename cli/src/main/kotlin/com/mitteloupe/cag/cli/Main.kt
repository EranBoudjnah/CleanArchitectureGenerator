package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.argument.AppArgumentProcessor
import com.mitteloupe.cag.cli.configuration.ClientConfigurationLoader
import com.mitteloupe.cag.cli.filesystem.CliFileSystemBridge
import com.mitteloupe.cag.cli.help.printHelpMessage
import com.mitteloupe.cag.cli.help.printUsageMessage
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.Generator
import com.mitteloupe.cag.core.GeneratorFactory
import com.mitteloupe.cag.core.NamespaceResolver
import com.mitteloupe.cag.core.findGradleProjectRoot
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogSettingsAccessor
import com.mitteloupe.cag.core.request.GenerateArchitectureRequest
import com.mitteloupe.cag.core.request.GenerateFeatureRequestBuilder
import com.mitteloupe.cag.core.request.GenerateProjectTemplateRequest
import com.mitteloupe.cag.core.request.GenerateUseCaseRequest
import com.mitteloupe.cag.core.request.GenerateViewModelRequest
import com.mitteloupe.cag.git.Git
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(arguments: Array<String>) {
    val argumentProcessor = AppArgumentProcessor()
    val projectRoot = findGradleProjectRoot(Paths.get("").toAbsolutePath().toFile()) ?: Paths.get("").toAbsolutePath().toFile()
    val projectModel = FilesystemProjectModel(projectRoot)
    val basePackage = NamespaceResolver().determineBasePackage(projectModel)
    val configuration = ClientConfigurationLoader().load(projectRoot)

    if (argumentProcessor.isHelpRequested(arguments)) {
        val helpOptions = argumentProcessor.getHelpOptions(arguments)
        when {
            helpOptions?.format?.lowercase() == "man" -> ManPagePrinter.printManPage(helpOptions.topic)
            helpOptions?.topic != null -> printHelpMessage(helpOptions.topic)
            else -> printHelpMessage()
        }
        return
    }

    if (argumentProcessor.isVersionRequested(arguments)) {
        val version = AppArgumentProcessor::class.java.`package`?.implementationVersion ?: "Unknown"
        println(version)
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
    val viewModelRequests = argumentProcessor.getNewViewModels(arguments)
    if (projectTemplateRequests.isEmpty() &&
        architectureRequests.isEmpty() &&
        featureRequests.isEmpty() &&
        dataSourceRequests.isEmpty() &&
        useCaseRequests.isEmpty() &&
        viewModelRequests.isEmpty()
    ) {
        printUsageMessage()
        return
    }

    val generator = produceGenerator()
    val destinationRootDirectory = projectModel.selectedModuleRootDir() ?: projectRoot
    val projectNamespace = basePackage ?: "com.unknown.app."

    val git = Git(gitBinaryPath = configuration.git.path)

    projectTemplateRequests.forEach { request ->
        val projectTemplateDestinationDirectory =
            if (projectModel.selectedModuleRootDir() != null) {
                projectRoot
            } else {
                destinationRootDirectory
            }
        val projectTemplateRequest =
            GenerateProjectTemplateRequest(
                destinationRootDirectory = projectTemplateDestinationDirectory,
                projectName = request.projectName,
                packageName = request.packageName,
                overrideMinimumAndroidSdk = null,
                overrideAndroidGradlePluginVersion = null,
                dependencyInjection = request.dependencyInjection,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt,
                enableKtor = request.enableKtor,
                enableRetrofit = request.enableRetrofit
            )
        executeAndReport {
            setVersionProvider(configuration.newProjectVersions)
            generator.generateProjectTemplate(projectTemplateRequest)
        }

        val shouldInitGit = request.enableGit || configuration.git.autoInitialize == true
        if (shouldInitGit) {
            if (!git.isAvailable(projectTemplateDestinationDirectory)) {
                println("Warning: Git is not available. Configure [git].path in .cagrc or install git.")
            }
            val didInit = git.initializeRepository(projectTemplateDestinationDirectory)
            if (!didInit || configuration.git.autoStage == true) {
                runCatching { git.stageAll(projectTemplateDestinationDirectory) }
            }
        }
    }

    architectureRequests.forEach { request ->
        val architecturePackageName = basePackage?.let { "$it.architecture" } ?: "com.unknown.app.architecture"
        val architectureRequest =
            GenerateArchitectureRequest(
                projectNamespace = projectNamespace,
                destinationRootDirectory = destinationRootDirectory,
                appModuleDirectory = request.appModuleDirectory,
                architecturePackageName = architecturePackageName,
                dependencyInjection = request.dependencyInjection,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt
            )
        executeAndReport {
            setVersionProvider(configuration.existingProjectVersions)
            generator.generateArchitecture(architectureRequest)
        }

        if (request.enableGit || configuration.git.autoStage == true) {
            val gitRoot = projectModel.selectedModuleRootDir() ?: projectRoot
            if (!git.isAvailable(gitRoot)) {
                println("Warning: Git is not available. Configure [git].path in .cagrc or install git.")
            }
            runCatching { git.stageAll(gitRoot) }
        }
    }

    featureRequests.forEach { requestFeature ->
        val packageName = requestFeature.packageName ?: basePackage?.let { "$it${requestFeature.featureName.lowercase()}" }

        val request =
            GenerateFeatureRequestBuilder(
                destinationRootDir = destinationRootDirectory,
                projectNamespace = projectNamespace,
                featureName = requestFeature.featureName
            ).featurePackageName(packageName)
                .enableCompose(true)
                .enableKtlint(requestFeature.enableKtlint)
                .enableDetekt(requestFeature.enableDetekt)
                .build()
        executeAndReport {
            setVersionProvider(configuration.existingProjectVersions)
            generator.generateFeature(request)
        }

        if (requestFeature.enableGit || configuration.git.autoStage == true) {
            val gitRoot = projectModel.selectedModuleRootDir() ?: projectRoot
            if (!git.isAvailable(gitRoot)) {
                println("Warning: Git is not available. Configure [git].path in .cagrc or install git.")
            }
            runCatching { git.stageAll(gitRoot) }
        }
    }

    dataSourceRequests.forEach { request ->
        executeAndReport {
            setVersionProvider(configuration.existingProjectVersions)
            generator.generateDataSource(
                destinationRootDirectory = destinationRootDirectory,
                dataSourceName = request.dataSourceName,
                projectNamespace = projectNamespace,
                useKtor = request.useKtor,
                useRetrofit = request.useRetrofit
            )
        }

        if (request.enableGit || configuration.git.autoStage == true) {
            val gitRoot = projectModel.selectedModuleRootDir() ?: projectRoot
            if (!git.isAvailable(gitRoot)) {
                println("Warning: Git is not available. Configure [git].path in .cagrc or install git.")
            }
            runCatching { git.stageAll(gitRoot) }
        }
    }

    useCaseRequests.forEach { request ->
        val targetDirectory = request.targetPath.toDirectory(destinationRootDirectory)

        val useCaseRequest =
            GenerateUseCaseRequest
                .Builder(
                    destinationDirectory = targetDirectory,
                    useCaseName = request.useCaseName
                ).inputDataType(request.inputDataType)
                .outputDataType(request.outputDataType)
                .build()

        executeAndReport {
            setVersionProvider(configuration.existingProjectVersions)
            generator.generateUseCase(useCaseRequest)
        }

        if (request.enableGit || configuration.git.autoStage == true) {
            val gitRoot = projectModel.selectedModuleRootDir() ?: projectRoot
            if (!git.isAvailable(gitRoot)) {
                println("Warning: Git is not available. Configure [git].path in .cagrc or install git.")
            }
            runCatching { git.stageAll(gitRoot) }
        }
    }

    viewModelRequests.forEach { request ->
        val targetDirectory = request.targetPath.toDirectory(destinationRootDirectory)

        val featurePackageName = basePackage ?: "com.example"

        val viewModelRequest =
            GenerateViewModelRequest
                .Builder(
                    destinationDirectory = targetDirectory,
                    viewModelName = request.viewModelName,
                    viewModelPackageName = "$featurePackageName.presentation.viewmodel",
                    featurePackageName = featurePackageName,
                    projectNamespace = projectNamespace
                ).build()

        executeAndReport {
            setVersionProvider(configuration.existingProjectVersions)
            generator.generateViewModel(viewModelRequest)
        }

        if (request.enableGit || configuration.git.autoStage == true) {
            val gitRoot = projectModel.selectedModuleRootDir() ?: projectRoot
            runCatching { git.stageAll(gitRoot) }
        }
    }
}

private fun String?.toDirectory(destinationRootDirectory: File) =
    if (this == null) {
        Paths.get("").toAbsolutePath().toFile()
    } else {
        val path = Paths.get(this)
        if (path.isAbsolute) {
            path.toFile()
        } else {
            File(destinationRootDirectory, this)
        }
    }

private fun produceGenerator(): Generator = GeneratorFactory(CliFileSystemBridge()).create()

private fun executeAndReport(operation: () -> Unit) =
    try {
        operation()
        println("Done!")
    } catch (exception: GenerationException) {
        println("Error: ${exception.message}")
        exitProcess(1)
    }

private fun setVersionProvider(overrides: Map<String, String>) {
    VersionCatalogSettingsAccessor.setProvider { key, default ->
        overrides[key] ?: default
    }
}
