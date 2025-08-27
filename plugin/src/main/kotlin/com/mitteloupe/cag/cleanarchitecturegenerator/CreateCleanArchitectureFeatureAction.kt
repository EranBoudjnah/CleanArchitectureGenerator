package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.DefaultGenerator
import com.mitteloupe.cag.core.GenerateFeatureRequestBuilder
import java.io.File

class CreateCleanArchitectureFeatureAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val projectModel = IntellijProjectModel(event)
        val defaultPrefix = BasePackageResolver().determineBasePackage(projectModel)
        val dialog = CreateCleanArchitectureFeatureDialog(project, defaultPrefix)
        if (dialog.showAndGet()) {
            val featureName = dialog.featureName
            val featurePackageName = dialog.featurePackageName
            val generator = DefaultGenerator()
            val projectRootDir = event.project?.basePath?.let { File(it) }
            val request =
                GenerateFeatureRequestBuilder(
                    destinationRootDir = projectRootDir ?: File("."),
                    featureName = featureName
                ).featurePackageName(featurePackageName)
                    .build()
            val result = generator.generateFeature(request)
            Messages.showInfoMessage(
                project,
                CleanArchitectureGeneratorBundle.message(
                    "info.feature.generator.confirmation",
                    featureName,
                    result
                ),
                CleanArchitectureGeneratorBundle.message("info.feature.generator.title")
            )
        }
    }
}
