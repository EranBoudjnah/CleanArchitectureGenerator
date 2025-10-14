package com.mitteloupe.cag.core.generation.bulk

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.generation.BuildSrcContentCreator
import com.mitteloupe.cag.core.generation.ConfigurationFileCreator
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.app.AppModuleContentGenerator
import com.mitteloupe.cag.core.generation.architecture.ArchitectureModulesContentGenerator
import com.mitteloupe.cag.core.generation.architecture.CoroutineModuleContentGenerator
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import com.mitteloupe.cag.core.option.DependencyInjection
import java.io.File

class ArchitectureFilesGenerator(
    private val coroutineModuleContentGenerator: CoroutineModuleContentGenerator,
    private val architectureModulesContentGenerator: ArchitectureModulesContentGenerator,
    private val settingsFileUpdater: SettingsFileUpdater,
    private val buildSrcContentCreator: BuildSrcContentCreator,
    private val configurationFileCreator: ConfigurationFileCreator,
    private val appModuleContentGenerator: AppModuleContentGenerator
) {
    fun generateArchitecture(
        projectNamespace: String,
        destinationRootDirectory: File,
        appModuleDirectory: File?,
        architecturePackageName: String,
        dependencyInjection: DependencyInjection,
        enableCompose: Boolean,
        enableKtlint: Boolean,
        enableDetekt: Boolean
    ) {
        val architecturePackageName = architecturePackageName.trim()
        if (architecturePackageName.isEmpty()) {
            throw GenerationException("Architecture package name is missing.")
        }

        val pathSegments = architecturePackageName.toSegments()
        if (pathSegments.isEmpty()) {
            throw GenerationException("Architecture package name is invalid.")
        }

        val architectureRoot = File(destinationRootDirectory, "architecture")

        if (architectureRoot.exists()) {
            throw GenerationException(
                if (architectureRoot.isDirectory) {
                    "The architecture directory already exists."
                } else {
                    "A file with the architecture name exists where the architecture directory should be created."
                }
            )
        }

        if (!architectureRoot.mkdirs()) {
            throw GenerationException("Failed to create architecture directory.")
        }

        coroutineModuleContentGenerator
            .generate(
                projectRoot = destinationRootDirectory,
                coroutinePackageName = architecturePackageName.replaceAfterLast(".", "coroutine")
            )

        architectureModulesContentGenerator
            .generate(
                architectureRoot = architectureRoot,
                architecturePackageName = architecturePackageName,
                dependencyInjection = dependencyInjection,
                enableCompose = enableCompose,
                enableKtlint = enableKtlint,
                enableDetekt = enableDetekt
            )

        settingsFileUpdater.updateArchitectureSettingsIfPresent(destinationRootDirectory)

        buildSrcContentCreator.writeGradleFile(destinationRootDirectory)
        buildSrcContentCreator.writeSettingsGradleFile(destinationRootDirectory)
        buildSrcContentCreator.writeProjectJavaLibraryFile(destinationRootDirectory)

        if (enableDetekt) {
            configurationFileCreator.writeDetektConfigurationFile(destinationRootDirectory)
        }
        if (enableKtlint) {
            configurationFileCreator.writeEditorConfigFile(destinationRootDirectory)
        }

        appModuleContentGenerator.writeArchitectureDependencyInjectionModuleIfPossible(
            startDirectory = destinationRootDirectory,
            projectNamespace = projectNamespace,
            appModuleDirectory = appModuleDirectory,
            dependencyInjection = dependencyInjection
        )
    }
}
