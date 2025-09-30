package com.mitteloupe.cag.cleanarchitecturegenerator.git

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

@Service(Service.Level.PROJECT)
class GitAddQueueService(private val project: Project) {
    private val queue = ConcurrentSkipListSet<String>()
    private val gitStager = GitStager(ProcessExecutor())

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

        val items = queue.toSet()
        if (items.isEmpty()) {
            return
        }

        gitStager.stageAll(projectRoot, items.map(::File))
        queue.removeAll(items)
    }
}

internal class GitStager(private val executor: ProcessExecutor) {
    fun stageAll(
        projectRoot: File,
        files: Collection<File>
    ) {
        if (files.isEmpty()) {
            return
        }
        val gitCommandWithArguments =
            listOf("git", "add", "--") +
                files.map { file ->
                    val absolutePath = file.absolutePath
                    val file = File(absolutePath)
                    file.relativeToOrNull(projectRoot)?.path ?: absolutePath
                }
        executor.run(projectRoot, gitCommandWithArguments)
    }
}

internal class ProcessExecutor {
    fun run(
        directory: File,
        args: List<String>
    ) {
        try {
            val process =
                ProcessBuilder(args)
                    .directory(directory)
                    .redirectErrorStream(true)
                    .start()
            process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
        } catch (_: Exception) {
        }
    }
}
