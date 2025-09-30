package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.GeneratorProvider
import com.mitteloupe.cag.cleanarchitecturegenerator.git.GitAddQueueService
import com.mitteloupe.cag.core.AppModuleDirectoryFinder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.NamespaceResolver
import com.mitteloupe.cag.core.request.GenerateFeatureRequestBuilder
import java.io.File

class CreateCleanArchitectureFeatureAction : AnAction() {
    private val ideBridge = IdeBridge()
    private val appModuleDirectoryFinder = AppModuleDirectoryFinder()
    private val generatorProvider = GeneratorProvider()

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectModel = IntellijProjectModel(event)
        val defaultNamespace = NamespaceResolver().determineBasePackage(projectModel)
        val projectRootDirectory = project.basePath?.let { File(it) } ?: File(".")
        val appModuleDirectories = appModuleDirectoryFinder.findAndroidAppModuleDirectories(projectRootDirectory)
        val dialog = CreateCleanArchitectureFeatureDialog(project, defaultNamespace, appModuleDirectories)
        if (dialog.showAndGet()) {
            val featureName = dialog.featureName
            val featurePackageName = dialog.featurePackageName
            val generator = generatorProvider.prepare(project).generate()
            val selectedAppModule = dialog.selectedAppModuleDirectory
            val enableKtlint = dialog.enableKtlint
            val enableDetekt = dialog.enableDetekt
            val request =
                GenerateFeatureRequestBuilder(
                    destinationRootDir = projectRootDirectory,
                    projectNamespace = defaultNamespace ?: "com.unknown.app",
                    featureName = featureName
                ).featurePackageName(featurePackageName)
                    .enableCompose(true)
                    .appModuleDirectory(selectedAppModule)
                    .enableKtlint(enableKtlint)
                    .enableDetekt(enableDetekt)
                    .build()
            try {
                generator.generateFeature(request)
                project.service<GitAddQueueService>().flush()
                ideBridge.refreshIde(projectRootDirectory)
                ideBridge.synchronizeGradle(project, projectRootDirectory)
                Messages.showInfoMessage(
                    project,
                    CleanArchitectureGeneratorBundle.message(
                        "info.feature.generator.confirmation",
                        featureName,
                        "Success!"
                    ),
                    CleanArchitectureGeneratorBundle.message("info.feature.generator.title")
                )
            } catch (e: GenerationException) {
                Messages.showErrorDialog(
                    project,
                    e.message ?: "Unknown error occurred",
                    CleanArchitectureGeneratorBundle.message("info.feature.generator.title")
                )
            }
        }
    }
}
