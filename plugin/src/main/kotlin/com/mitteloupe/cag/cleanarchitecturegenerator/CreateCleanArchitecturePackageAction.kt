package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.mitteloupe.cag.core.BasePackageResolver

class CreateCleanArchitecturePackageAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectModel = IntellijProjectModel(event)
        val defaultPrefix = BasePackageResolver().determineBasePackage(projectModel)
        val dialog = CreateCleanArchitecturePackageDialog(project, defaultPrefix ?: "com.example")
        dialog.show()
    }

    override fun update(event: AnActionEvent) {
        val project = event.project
        event.presentation.isEnabledAndVisible = project != null
    }
}
