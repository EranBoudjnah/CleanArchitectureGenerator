package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.GeneratorProvider
import com.mitteloupe.cag.cleanarchitecturegenerator.git.GitAddQueueService
import com.mitteloupe.cag.core.AppModuleDirectoryFinder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.NamespaceResolver
import com.mitteloupe.cag.core.option.DependencyInjection
import com.mitteloupe.cag.core.request.GenerateArchitectureRequest
import java.io.File

class CreateCleanArchitecturePackageAction : AnAction() {
    private val ideBridge = IdeBridge()
    private val generatorProvider = GeneratorProvider()
    private val appModuleDirectoryFinder = AppModuleDirectoryFinder()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectModel = IntellijProjectModel(event)
        val basePackage = NamespaceResolver().determineBasePackage(projectModel)
        val projectRootDirectory = project.basePath?.let { File(it) } ?: File(".")
        val appModuleDirectories = appModuleDirectoryFinder.findAndroidAppModuleDirectories(projectRootDirectory)
        val dialog = CreateCleanArchitecturePackageDialog(project, appModuleDirectories)
        if (dialog.showAndGet()) {
            val defaultBasePackage = "com.example"
            val architecturePackageName =
                (basePackage ?: defaultBasePackage) + ".architecture"
            val generator = generatorProvider.prepare(project).generate()
            val projectRootDirectory = event.project?.basePath?.let { File(it) } ?: File(".")
            val request =
                GenerateArchitectureRequest(
                    projectNamespace = basePackage ?: defaultBasePackage,
                    destinationRootDirectory = projectRootDirectory,
                    appModuleDirectory = dialog.selectedAppModuleDirectory,
                    architecturePackageName = architecturePackageName,
                    enableCompose = dialog.isComposeEnabled(),
                    dependencyInjection = DependencyInjection.Hilt,
                    enableKtlint = dialog.isKtlintEnabled(),
                    enableDetekt = dialog.isDetektEnabled()
                )
            try {
                generator.generateArchitecture(request)
                project.service<GitAddQueueService>().flush()
                ideBridge.refreshIde(projectRootDirectory)
                ideBridge.synchronizeGradle(project, projectRootDirectory)
                Messages.showInfoMessage(
                    project,
                    CleanArchitectureGeneratorBundle.message(
                        "info.architecture.generator.confirmation",
                        architecturePackageName,
                        "Success!"
                    ),
                    CleanArchitectureGeneratorBundle.message("info.architecture.generator.title")
                )
            } catch (e: GenerationException) {
                Messages.showErrorDialog(
                    project,
                    e.message ?: "Unknown error occurred",
                    CleanArchitectureGeneratorBundle.message("info.architecture.generator.title")
                )
            }
        }
    }

    override fun update(event: AnActionEvent) {
        val hasArchitectureModule =
            event.project?.basePath?.let { basePath ->
                architectureModuleExists(File(basePath))
            }
        event.presentation.isEnabledAndVisible = hasArchitectureModule == false
    }

    private fun architectureModuleExists(projectRoot: File): Boolean {
        val architectureDirectory = File(projectRoot, "architecture")
        return architectureDirectory.exists() && architectureDirectory.isDirectory
    }
}
