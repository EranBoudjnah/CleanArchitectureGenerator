package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class CreateCleanArchitectureFeatureAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val defaultPrefix = BasePackageResolver().determineBasePackage(event)
        val dialog = CreateCleanArchitectureFeatureDialog(project, defaultPrefix)
        if (dialog.showAndGet()) {
            val featureName = dialog.featureName
            Messages.showInfoMessage(
                project,
                CleanArchitectureGeneratorBundle.message(
                    "info.feature.generator.confirmation",
                    featureName
                ),
                CleanArchitectureGeneratorBundle.message("info.feature.generator.title")
            )
        }
    }
}
