package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.GenerateArchitectureRequest
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.Generator
import java.io.File

class CreateCleanArchitecturePackageAction : AnAction() {
    private val ideBridge = IdeBridge()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectModel = IntellijProjectModel(event)
        val basePackage = BasePackageResolver().determineBasePackage(projectModel)
        val dialog = CreateCleanArchitecturePackageDialog(project)
        if (dialog.showAndGet()) {
            val architecturePackageName =
                basePackage?.let {
                    it.trimEnd('.') + ".architecture"
                } ?: "com.example.architecture"
            val generator = Generator()
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
        val project = event.project
        event.presentation.isEnabledAndVisible = project != null
    }
}
