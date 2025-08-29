package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.mitteloupe.cag.core.Generator
import java.io.File

class CreateDataSourceAction : AnAction() {
    private val ideBridge = IdeBridge()

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val projectRootDir = event.project?.basePath?.let { File(it) } ?: File(".")

        val result = Generator().generateDatasource(projectRootDir)
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
