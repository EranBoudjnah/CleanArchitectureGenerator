package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.GeneratorProvider
import com.mitteloupe.cag.cleanarchitecturegenerator.git.GitAddQueueService
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.NamespaceResolver
import com.mitteloupe.cag.core.request.GenerateArchitectureRequest
import java.io.File

class CreateCleanArchitecturePackageAction : AnAction() {
    private val ideBridge = IdeBridge()
    private val generatorProvider = GeneratorProvider()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectModel = IntellijProjectModel(event)
        val basePackage = NamespaceResolver().determineBasePackage(projectModel)
        val dialog = CreateCleanArchitecturePackageDialog(project)
        if (dialog.showAndGet()) {
            val architecturePackageName =
                basePackage?.let {
                    "$it.architecture"
                } ?: "com.example.architecture"
            val generator = generatorProvider.prepare(project).generate()
            val projectRootDir = event.project?.basePath?.let { File(it) } ?: File(".")
            val request =
                GenerateArchitectureRequest(
                    destinationRootDirectory = projectRootDir,
                    architecturePackageName = architecturePackageName,
                    enableCompose = dialog.isComposeEnabled(),
                    enableKtlint = dialog.isKtlintEnabled(),
                    enableDetekt = dialog.isDetektEnabled()
                )
            try {
                generator.generateArchitecture(request)
                project.service<GitAddQueueService>().flush()
                ideBridge.refreshIde(projectRootDir)
                ideBridge.synchronizeGradle(project, projectRootDir)
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
