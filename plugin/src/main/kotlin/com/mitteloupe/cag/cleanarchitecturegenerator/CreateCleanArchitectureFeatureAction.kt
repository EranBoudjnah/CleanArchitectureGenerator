package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.ERROR_PREFIX
import com.mitteloupe.cag.core.GenerateFeatureRequestBuilder
import com.mitteloupe.cag.core.Generator
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
            val result = generator.generateFeature(request)
            refreshIde(projectRootDir)
            synchronizeGradle(project, result, projectRootDir)
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

    private fun refreshIde(projectRootDirectory: File) {
        val virtualRoot = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(projectRootDirectory)
        if (virtualRoot == null) {
            VirtualFileManager.getInstance().asyncRefresh(null)
        } else {
            VfsUtil.markDirtyAndRefresh(true, true, true, virtualRoot)
        }
    }

    private fun synchronizeGradle(
        project: Project?,
        result: String,
        projectRootDir: File
    ) {
        if (project != null && !result.startsWith(ERROR_PREFIX)) {
            ExternalSystemUtil.refreshProject(
                projectRootDir.absolutePath,
                ImportSpecBuilder(project, ProjectSystemId("GRADLE")).build()
            )
        }
    }
}
