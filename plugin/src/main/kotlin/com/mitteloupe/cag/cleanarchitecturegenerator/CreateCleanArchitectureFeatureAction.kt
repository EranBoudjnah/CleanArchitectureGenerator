package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.DefaultGenerator

class CreateCleanArchitectureFeatureAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val projectModel = IntellijProjectModel(event)
        val defaultPrefix = BasePackageResolver().determineBasePackage(projectModel)
        val dialog = CreateCleanArchitectureFeatureDialog(project, defaultPrefix)
        if (dialog.showAndGet()) {
            val featureName = dialog.featureName
            val generator = DefaultGenerator()
            val result = generator.generateFeature(featureName)
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
