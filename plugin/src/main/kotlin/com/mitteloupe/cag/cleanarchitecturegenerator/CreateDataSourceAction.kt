package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.core.BasePackageResolver
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.Generator
import java.io.File

class CreateDataSourceAction : AnAction() {
    private val ideBridge = IdeBridge()

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val projectRootDir = event.project?.basePath?.let { File(it) } ?: File(".")
        val projectModel = IntellijProjectModel(event)
        val defaultPrefix = BasePackageResolver().determineBasePackage(projectModel) ?: "com.unknown.app."

        val dialog = CreateDataSourceDialog(project)
        if (!dialog.showAndGet()) return
        val dataSourceName = dialog.dataSourceNameWithSuffix

        try {
            Generator().generateDataSource(
                destinationRootDirectory = projectRootDir,
                dataSourceName = dataSourceName,
                projectNamespace = defaultPrefix,
                useKtor = dialog.useKtor,
                useRetrofit = dialog.useRetrofit
            )
            ideBridge.refreshIde(projectRootDir)
            ideBridge.synchronizeGradle(project, null, projectRootDir)
            Messages.showInfoMessage(
                project,
                CleanArchitectureGeneratorBundle.message(
                    "info.datasource.generator.confirmation",
                    "Success!"
                ),
                CleanArchitectureGeneratorBundle.message("info.datasource.generator.title")
            )
        } catch (e: GenerationException) {
            Messages.showErrorDialog(
                project,
                e.message ?: "Unknown error occurred",
                CleanArchitectureGeneratorBundle.message("info.datasource.generator.title")
            )
        }
    }
}
