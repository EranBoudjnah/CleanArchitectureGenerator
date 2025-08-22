package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import java.io.File

class CreateCleanArchitectureFeatureAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val defaultPrefix = determineBasePackage(event)
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

    private fun determineBasePackage(event: AnActionEvent): String? {
        val project = event.project ?: return null

        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val module = virtualFile?.let { ModuleUtilCore.findModuleForFile(it, project) }
        val namespace = readAndroidNamespace(module)
        if (!namespace.isNullOrBlank()) {
            return ensureTrailingDot(namespace)
        }

        val modules = ModuleManager.getInstance(project).modules
        for (module in modules) {
            val moduleNamespace = readAndroidNamespace(module)
            if (!moduleNamespace.isNullOrBlank()) {
                return ensureTrailingDot(moduleNamespace)
            }
        }

        return null
    }

    private fun readAndroidNamespace(module: Module?): String? {
        if (module == null) return null
        val contentRoots = ModuleRootManager.getInstance(module).contentRoots
        val moduleDirectory = contentRoots.firstOrNull()?.path ?: return null
        val gradleKtsFile = File(moduleDirectory, "build.gradle.kts")
        val gradleGroovyFile = File(moduleDirectory, "build.gradle")
        val fileContents =
            when {
                gradleKtsFile.exists() -> gradleKtsFile.readText()
                gradleGroovyFile.exists() -> gradleGroovyFile.readText()
                else -> return null
            }

        val regexes =
            listOf(
                Regex("""(?s)android\s*\{[\n\r\s\S]*?namespace\s*=\s*['"]([^'"]+)['"]"""),
                Regex("""(?s)android\s*\{[\n\r\s\S]*?namespace\s+['"]([^'"]+)['"]""")
            )
        val match = regexes.asSequence().mapNotNull { it.find(fileContents) }.firstOrNull()
        return match?.groupValues?.get(1)?.trim()
    }

    private fun ensureTrailingDot(name: String): String =
        if (name.endsWith('.')) {
            name
        } else {
            "$name."
        }
}
