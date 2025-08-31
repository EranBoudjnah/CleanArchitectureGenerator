package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class CreateUseCaseAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val isVisible = project != null && file != null && file.isDirectory && isInDomainContext(project, file)
        event.presentation.isEnabledAndVisible = isVisible
    }

    private fun isInDomainContext(
        project: Project,
        directory: VirtualFile
    ): Boolean {
        val module = ModuleUtilCore.findModuleForFile(directory, project)
        if (module?.isNonArchitectureDomain == true) {
            return true
        }
        var current: VirtualFile? = directory
        while (current != null && current != project.projectFile) {
            if (current.name == "domain") {
                return true
            }
            println("Checking: $current")
            current = current.parent
        }
        return false
    }

    override fun actionPerformed(event: AnActionEvent) {
    }
}

private val Module.isNonArchitectureDomain: Boolean
    get() = name.endsWith(".domain") && !name.contains(".architecture.")
