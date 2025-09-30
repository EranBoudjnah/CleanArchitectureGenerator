package com.mitteloupe.cag.cleanarchitecturegenerator.git

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

@Service(Service.Level.PROJECT)
class GitAddQueueService(private val project: Project) {
    private val queue = ConcurrentSkipListSet<String>()

    fun enqueue(file: File) {
        val path = file.absolutePath
        queue.add(path)
    }

    fun flush() {
        val basePath = project.basePath ?: return
        val projectRoot = File(basePath)
        if (!projectRoot.exists()) {
            return
        }

        val filePathsToAdd = queue.toSet()
        if (filePathsToAdd.isEmpty()) {
            return
        }

        val gitCommandWithArguments =
            listOf("git", "add", "--") +
                filePathsToAdd.map { absolutePath ->
                    val file = File(absolutePath)
                    file.relativeToOrNull(projectRoot)?.path ?: absolutePath
                }

        try {
            val process =
                ProcessBuilder(gitCommandWithArguments)
                    .directory(projectRoot)
                    .redirectErrorStream(true)
                    .start()
            process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
        } catch (_: Exception) {
        } finally {
            queue.removeAll(filePathsToAdd)
        }
    }
}
