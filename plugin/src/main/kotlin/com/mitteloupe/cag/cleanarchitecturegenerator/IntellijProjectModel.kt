package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.mitteloupe.cag.core.ProjectModel
import java.io.File

class IntellijProjectModel(
    private val event: AnActionEvent
) : ProjectModel {
    override fun selectedModuleRootDir(): File? {
        val project = event.project ?: return null
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val module: Module = ModuleUtilCore.findModuleForFile(virtualFile, project) ?: return null
        val contentRoots = ModuleRootManager.getInstance(module).contentRoots
        val moduleDirectory = contentRoots.firstOrNull()?.path ?: return null
        return File(moduleDirectory)
    }

    override fun allModuleRootDirs(): List<File> {
        val project = event.project ?: return emptyList()
        val modules = ModuleManager.getInstance(project).modules
        return modules.mapNotNull { module ->
            val contentRoots = ModuleRootManager.getInstance(module).contentRoots
            val moduleDirectory = contentRoots.firstOrNull()?.path
            moduleDirectory?.let { File(it) }
        }
    }
}
