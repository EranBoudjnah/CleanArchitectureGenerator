package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.HelpContent.USAGE_SYNTAX
import com.mitteloupe.cag.cli.configuration.ClientConfigurationLoader
import com.mitteloupe.cag.cli.filesystem.CliFileSystemBridge
import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.Generator
import com.mitteloupe.cag.core.NamespaceResolver
import com.mitteloupe.cag.core.findGradleProjectRoot
import com.mitteloupe.cag.core.generation.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.BuildSrcContentCreator
import com.mitteloupe.cag.core.generation.ConfigurationFileCreator
import com.mitteloupe.cag.core.generation.DataLayerContentGenerator
import com.mitteloupe.cag.core.generation.DataSourceImplementationCreator
import com.mitteloupe.cag.core.generation.DataSourceInterfaceCreator
import com.mitteloupe.cag.core.generation.DataSourceModuleCreator
import com.mitteloupe.cag.core.generation.DomainLayerContentGenerator
import com.mitteloupe.cag.core.generation.GradleFileCreator
import com.mitteloupe.cag.core.generation.GradlePropertiesFileCreator
import com.mitteloupe.cag.core.generation.GradleWrapperCreator
import com.mitteloupe.cag.core.generation.KotlinFileCreator
import com.mitteloupe.cag.core.generation.PresentationLayerContentGenerator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.UiLayerContentGenerator
import com.mitteloupe.cag.core.generation.architecture.ArchitectureModulesContentGenerator
import com.mitteloupe.cag.core.generation.architecture.CoroutineModuleContentGenerator
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogSettingsAccessor
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
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

    val git = Git()

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

        val shouldInitGit = request.enableGit || configuration.git.autoInitialize
        if (shouldInitGit) {
            val didInit = git.initializeRepository(projectTemplateDestinationDirectory)
            if (!didInit || configuration.git.autoStage) {
                runCatching { git.stageAll(projectTemplateDestinationDirectory) }
            }
        }
    }

    architectureRequests.forEach { request ->
        val architecturePackageName = basePackage?.let { "$it.architecture" } ?: "com.unknown.app.architecture"
        val architectureRequest =
            GenerateArchitectureRequest(
                destinationRootDirectory = destinationRootDirectory,
                architecturePackageName = architecturePackageName,
                enableCompose = request.enableCompose,
                enableKtlint = request.enableKtlint,
                enableDetekt = request.enableDetekt
            )
        executeAndReport {
            setVersionProvider(configuration.existingProjectVersions)
            generator.generateArchitecture(architectureRequest)
        }

        if (request.enableGit || configuration.git.autoStage) {
            val gitRoot = projectModel.selectedModuleRootDir() ?: projectRoot
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

        if (requestFeature.enableGit || configuration.git.autoStage) {
            val gitRoot = projectModel.selectedModuleRootDir() ?: projectRoot
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

        if (request.enableGit || configuration.git.autoStage) {
            val gitRoot = projectModel.selectedModuleRootDir() ?: projectRoot
            runCatching { git.stageAll(gitRoot) }
        }
    }

    useCaseRequests.forEach { request ->
        val targetDirectory = request.targetPath.toDirectory(destinationRootDirectory)

        val useCaseRequest =
            GenerateUseCaseRequest.Builder(
                destinationDirectory = targetDirectory,
                useCaseName = request.useCaseName
            )
                .inputDataType(request.inputDataType)
                .outputDataType(request.outputDataType)
                .build()

        executeAndReport {
            setVersionProvider(configuration.existingProjectVersions)
            generator.generateUseCase(useCaseRequest)
        }
    }

    viewModelRequests.forEach { request ->
        val targetDirectory = request.targetPath.toDirectory(destinationRootDirectory)

        val featurePackageName = basePackage ?: "com.example"

        val viewModelRequest =
            GenerateViewModelRequest.Builder(
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

        if (request.enableGit || configuration.git.autoStage) {
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

private fun printUsageMessage() {
    println(
        """
        $USAGE_SYNTAX

        Run with --help or -h for more options.
        """.trimIndent()
    )
}

private fun printHelpMessage() {
    println(
        """
        $USAGE_SYNTAX

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
            --ktlint | -kl
              Enable ktlint for the preceding feature (adds plugin and .editorconfig if missing)
            --detekt | -d
              Enable detekt for the preceding feature (adds plugin and detekt.yml if missing)
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
          --new-view-model | -nvm
              Generate a new ViewModel
            --name=ViewModelName | -n=ViewModelName | -n ViewModelName | -nViewModelName
                Specify the ViewModel name (required)
            --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
                Specify the target directory for the preceding ViewModel
          --help, -h
              Show this help message and exit
        """.trimIndent()
    )
}

private fun printHelpMessage(topic: String?) {
    val normalized = topic?.lowercase()?.trim()
    if (normalized.isNullOrEmpty() || normalized == "all" || normalized == "overview") {
        printHelpMessage()
        return
    }
    val sections = HelpContent.helpSections()
    val content = sections[normalized]
    if (content != null) {
        println(content)
    } else {
        println("Unknown help topic: $topic\nAvailable topics: ${sections.keys.sorted().joinToString(", ")}\n")
        printHelpMessage()
    }
}

private fun produceGenerator(): Generator {
    val fileCreator = FileCreator(CliFileSystemBridge())
    val directoryFinder = DirectoryFinder()
    val kotlinFileCreator = KotlinFileCreator(fileCreator)
    val gradleFileCreator = GradleFileCreator(fileCreator)
    val catalogUpdater = VersionCatalogUpdater(fileCreator)
    return Generator(
        GradleFileCreator(fileCreator),
        GradleWrapperCreator(fileCreator),
        AppModuleContentGenerator(fileCreator, directoryFinder),
        BuildSrcContentCreator(fileCreator),
        ConfigurationFileCreator(fileCreator),
        UiLayerContentGenerator(kotlinFileCreator),
        PresentationLayerContentGenerator(kotlinFileCreator, fileCreator),
        DomainLayerContentGenerator(kotlinFileCreator),
        DataLayerContentGenerator(kotlinFileCreator),
        DataSourceModuleCreator(fileCreator),
        DataSourceInterfaceCreator(fileCreator),
        DataSourceImplementationCreator(fileCreator),
        GradlePropertiesFileCreator(fileCreator),
        ArchitectureModulesContentGenerator(gradleFileCreator, catalogUpdater),
        CoroutineModuleContentGenerator(gradleFileCreator, catalogUpdater),
        VersionCatalogUpdater(fileCreator),
        SettingsFileUpdater(fileCreator)
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

private fun setVersionProvider(overrides: Map<String, String>) {
    VersionCatalogSettingsAccessor.setProvider { key, default ->
        overrides[key] ?: default
    }
}
