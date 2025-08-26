package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import java.io.File

private val namespaceRegex = """(?s)android\s*\{[\n\r\s\S]*?namespace(?:\s*=\s*|\s+)['"]([^'"]+)['"]""".toRegex()

class BasePackageResolver {
    fun determineBasePackage(event: AnActionEvent): String? {
        val project = event.project ?: return null

        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val module = virtualFile?.let { ModuleUtilCore.findModuleForFile(it, project) }
        val namespace = readAndroidNamespace(module)
        if (!namespace.isNullOrBlank()) {
            return ensureTrailingDot(namespace)
        }

        val modules = ModuleManager.getInstance(project).modules
        for (m in modules) {
            val moduleNamespace = readAndroidNamespace(m)
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

        val match = namespaceRegex.find(fileContents)
        return match?.groupValues?.get(1)?.trim()
    }

    private fun ensureTrailingDot(name: String): String =
        if (name.endsWith('.')) {
            name
        } else {
            "$name."
        }
}
