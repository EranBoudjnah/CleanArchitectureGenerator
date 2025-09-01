package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.mitteloupe.cag.core.GenerateUseCaseRequest
import com.mitteloupe.cag.core.Generator
import java.io.File

class CreateUseCaseAction : AnAction() {
    private val ideBridge = IdeBridge()

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
            current = current.parent
        }
        return false
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val projectRootDir = event.project?.basePath?.let { File(it) } ?: File(".")

        val selectedDirectory = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val suggestedDirectory = suggestUseCaseDirectory(project, selectedDirectory)

        val dialog = CreateUseCaseDialog(project, suggestedDirectory)
        if (!dialog.showAndGet()) {
            return
        }

        val destination = dialog.destinationDirectory ?: suggestedDirectory
        val useCaseName = dialog.useCaseNameWithSuffix

        val request =
            GenerateUseCaseRequest.Builder(
                destinationDirectory = destination ?: File(projectRootDir, ""),
                useCaseName = useCaseName
            )
                .inputDataType(dialog.inputDataType)
                .outputDataType(dialog.outputDataType)
                .build()

        val result = Generator().generateUseCase(request)
        ideBridge.refreshIde(projectRootDir)
        ideBridge.synchronizeGradle(project, result, projectRootDir)
        Messages.showInfoMessage(
            project,
            CleanArchitectureGeneratorBundle.message(
                "info.datasource.generator.confirmation",
                result
            ),
            CleanArchitectureGeneratorBundle.message("info.datasource.generator.title")
        )
    }
}

private val Module.isNonArchitectureDomain: Boolean
    get() = name.endsWith(".domain") && !name.contains(".architecture.")

private fun suggestUseCaseDirectory(
    project: Project?,
    virtualFile: VirtualFile?
): File? {
    if (project == null || virtualFile == null) return null
    val startingDir = if (virtualFile.isDirectory) virtualFile else virtualFile.parent
    val byCurrent = findUseCaseDirectoryIn(startingDir)
    if (byCurrent != null) {
        return File(byCurrent.path)
    }

    val module = ModuleUtilCore.findModuleForFile(startingDir, project) ?: return null
    val contentRoots = ModuleRootManager.getInstance(module).contentRoots
    contentRoots.forEach { root ->
        val found = findUseCaseDirectoryIn(root)
        if (found != null) {
            return File(found.path)
        }
    }
    return null
}

private fun findUseCaseDirectoryIn(directory: VirtualFile?): VirtualFile? {
    if (directory == null || !directory.isDirectory) {
        return null
    }

    var result: VirtualFile? = null

    VfsUtilCore.visitChildrenRecursively(
        directory,
        object : VirtualFileVisitor<Any>() {
            override fun visitFile(file: VirtualFile): Boolean {
                return when {
                    file.isDirectory && file.name.equals("usecase", ignoreCase = true) -> {
                        result = file
                        false
                    }
                    !file.isDirectory && file.name.endsWith("UseCase.kt") -> {
                        result = file.parent
                        false
                    }
                    else -> true
                }
            }
        }
    )

    return result
}
