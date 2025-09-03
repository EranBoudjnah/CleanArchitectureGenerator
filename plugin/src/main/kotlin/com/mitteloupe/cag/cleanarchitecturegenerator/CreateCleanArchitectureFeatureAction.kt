package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.GenerateFeatureRequestBuilder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.Generator
import java.io.File

class CreateCleanArchitectureFeatureAction : AnAction() {
    private val ideBridge = IdeBridge()

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val projectModel = IntellijProjectModel(event)
        val defaultPrefix = BasePackageResolver().determineBasePackage(projectModel)
        val dialog = CreateCleanArchitectureFeatureDialog(project, defaultPrefix)
        if (dialog.showAndGet()) {
            val featureName = dialog.featureName
            val featurePackageName = dialog.featurePackageName
            val generator = Generator()
            val projectRootDir = event.project?.basePath?.let { File(it) } ?: File(".")
            val request =
                GenerateFeatureRequestBuilder(
                    destinationRootDir = projectRootDir,
                    projectNamespace = defaultPrefix ?: "com.unknown.app.",
                    featureName = featureName
                ).featurePackageName(featurePackageName)
                    .enableCompose(true)
                    .build()
            try {
                generator.generateFeature(request)
                ideBridge.refreshIde(projectRootDir)
                ideBridge.synchronizeGradle(project, projectRootDir)
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
