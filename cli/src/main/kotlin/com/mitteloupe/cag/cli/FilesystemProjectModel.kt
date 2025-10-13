package com.mitteloupe.cag.cli

import com.mitteloupe.cag.core.ProjectModel
import java.io.File

class FilesystemProjectModel(
    private val projectRoot: File,
    private val selectedModuleDir: File? = null
) : ProjectModel {
    override fun selectedModuleRootDir(): File? = selectedModuleDir ?: inferSingleModuleRoot()

    override fun allModuleRootDirs(): List<File> = findGradleModuleDirectories(projectRoot)

    private fun inferSingleModuleRoot(): File? {
        val moduleDirs = findGradleModuleDirectories(projectRoot)
        return when (moduleDirs.size) {
            0 -> null
            1 -> moduleDirs.first()
            else -> null
        }
    }

    private fun findGradleModuleDirectories(root: File): List<File> {
        if (!root.isDirectory) return emptyList()
        val queue = ArrayDeque<File>()
        val result = mutableListOf<File>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val directory = queue.removeFirst()
            val hasGradle = File(directory, "build.gradle.kts").exists() || File(directory, "build.gradle").exists()
            if (hasGradle) {
                result.add(directory)
            }
            if (!hasGradle || directory == root) {
                directory
                    .listFiles()
                    ?.filter { it.isDirectory && !it.name.startsWith('.') && it.name != "build" }
                    ?.forEach(queue::add)
            }
        }
        return result
    }
}
