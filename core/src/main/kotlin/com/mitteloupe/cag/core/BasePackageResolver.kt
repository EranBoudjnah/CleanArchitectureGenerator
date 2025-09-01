package com.mitteloupe.cag.core

import java.io.File

private val namespaceRegex = """(?s)android\s*\{[\n\r\s\S]*?namespace(?:\s*=\s*|\s+)['"]([^'"]+)['"]""".toRegex()

class BasePackageResolver() {
    fun determineBasePackage(projectModel: ProjectModel): String? {
        val selectedModule = projectModel.selectedModuleRootDir()
        val namespace = readAndroidNamespace(selectedModule)
        if (!namespace.isNullOrBlank()) return ensureTrailingDot(namespace)

        projectModel.allModuleRootDirs().forEach { moduleDir ->
            val moduleNamespace = readAndroidNamespace(moduleDir)
            if (!moduleNamespace.isNullOrBlank()) return ensureTrailingDot(moduleNamespace)
        }

        return null
    }

    private fun readAndroidNamespace(moduleDirectory: File?): String? {
        moduleDirectory ?: return null
        val gradleKtsFile = File(moduleDirectory, "build.gradle.kts")
        val gradleGroovyFile = File(moduleDirectory, "build.gradle")
        val fileContents =
            when {
                gradleKtsFile.exists() -> gradleKtsFile.readText()
                gradleGroovyFile.exists() -> gradleGroovyFile.readText()
                else -> return null
            }
        val projectRoot = findGradleProjectRoot(moduleDirectory) ?: moduleDirectory
        val isAppModule =
            AppModuleDirectoryFinder().findAndroidAppModuleDirectories(projectRoot)
                .any { it == moduleDirectory }
        if (!isAppModule) {
            return null
        }

        return namespaceRegex.find(fileContents)?.groupValues?.get(1)?.trim()
    }

    private fun ensureTrailingDot(name: String): String = if (name.endsWith('.')) name else "$name."
}
