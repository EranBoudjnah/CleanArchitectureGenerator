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
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.GeneratorProvider
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.NamespaceResolver
import com.mitteloupe.cag.core.generation.structure.PackageNameDeriver
import com.mitteloupe.cag.core.request.GenerateViewModelRequest
import java.io.File

class CreateViewModelAction : AnAction() {
    private val ideBridge = IdeBridge()
    private val generatorProvider = GeneratorProvider()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val isVisible = project != null && file != null && file.isDirectory && isInPresentationContext(project, file)
        event.presentation.isEnabledAndVisible = isVisible
    }

    private fun isInPresentationContext(
        project: Project,
        directory: VirtualFile
    ): Boolean {
        val module = ModuleUtilCore.findModuleForFile(directory, project)
        if (module?.isNonArchitecturePresentation == true) {
            return true
        }
        var current: VirtualFile? = directory
        while (current != null && current != project.projectFile) {
            if (current.name == "presentation") {
                return true
            }
            current = current.parent
        }
        return false
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val projectRootDirectory = project.basePath?.let { File(it) } ?: File(".")

        val selectedDirectory = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val suggestedDirectory = suggestViewModelDirectory(project, selectedDirectory)

        val dialog = CreateViewModelDialog(project, suggestedDirectory)
        if (!dialog.showAndGet()) {
            return
        }

        val destination = dialog.destinationDirectory ?: suggestedDirectory ?: File(projectRootDirectory, "")
        val viewModelName = dialog.viewModelNameWithSuffix

        val projectModel = IntellijProjectModel(event)
        val projectNamespace = NamespaceResolver().determineBasePackage(projectModel)

        val viewModelPackageName = PackageNameDeriver.derivePackageNameForDirectory(destination).orEmpty()
        val featurePackageName = viewModelPackageName.substringBeforeLast(".presentation")
        val request =
            GenerateViewModelRequest.Builder(
                destinationDirectory = destination,
                viewModelName = viewModelName,
                featurePackageName = featurePackageName,
                viewModelPackageName = viewModelPackageName,
                projectNamespace = projectNamespace ?: "com.unknown.app"
            ).build()

        try {
            generatorProvider.prepare(project).generate().generateViewModel(request)
            ideBridge.refreshIde(projectRootDirectory)
            ideBridge.synchronizeGradle(project, projectRootDirectory)
            Messages.showInfoMessage(
                project,
                CleanArchitectureGeneratorBundle.message("info.viewmodel.generator.confirmation"),
                CleanArchitectureGeneratorBundle.message("info.viewmodel.generator.title")
            )
        } catch (e: GenerationException) {
            Messages.showErrorDialog(
                project,
                e.message ?: "Unknown error occurred",
                CleanArchitectureGeneratorBundle.message("info.viewmodel.generator.title")
            )
        }
    }
}

private val Module.isNonArchitecturePresentation: Boolean
    get() = name.endsWith(".presentation") && !name.contains(".architecture.")

private fun suggestViewModelDirectory(
    project: Project?,
    virtualFile: VirtualFile?
): File? {
    val startingDir = if (virtualFile?.isDirectory == true) virtualFile else virtualFile?.parent

    val byCurrent = findViewModelDirectoryIn(startingDir)
    if (byCurrent != null) {
        return File(byCurrent.path)
    }

    val byParent = startingDir?.parent?.let { findViewModelDirectoryIn(it) }
    if (byParent != null) {
        return File(byParent.path)
    }

    val module =
        if (startingDir != null && project != null) {
            ModuleUtilCore.findModuleForFile(startingDir, project)
        } else {
            null
        }
    if (module == null) {
        return null
    }
    val contentRoots = ModuleRootManager.getInstance(module).contentRoots
    contentRoots.forEach { root ->
        val found = findViewModelDirectoryIn(root)
        if (found != null) {
            return File(found.path)
        }
    }
    return null
}

private fun findViewModelDirectoryIn(directory: VirtualFile?): VirtualFile? {
    if (directory == null || !directory.isDirectory) {
        return null
    }

    var result: VirtualFile? = null

    VfsUtilCore.visitChildrenRecursively(
        directory,
        object : VirtualFileVisitor<Any>() {
            override fun visitFile(file: VirtualFile): Boolean {
                return when {
                    file.isDirectory && file.name.equals("viewmodel", ignoreCase = true) -> {
                        result = file
                        false
                    }
                    !file.isDirectory && file.name.endsWith("ViewModel.kt") -> {
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
