package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class CreateCleanArchitectureFeatureAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        Messages.showInfoMessage(
            project,
            CleanArchitectureGeneratorBundle.message("info.feature.generator.body"),
            CleanArchitectureGeneratorBundle.message("info.feature.generator.title")
        )
    }
}
