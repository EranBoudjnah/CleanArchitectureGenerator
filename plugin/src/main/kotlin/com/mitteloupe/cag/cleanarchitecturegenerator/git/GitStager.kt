package com.mitteloupe.cag.cleanarchitecturegenerator.git

import java.io.File

internal class GitStager(private val executor: ProcessExecutor = ProcessExecutor()) {
    fun stage(
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

    fun stageAll(projectRoot: File) {
        executor.run(projectRoot, listOf("git", "add", "-A"))
    }
}
