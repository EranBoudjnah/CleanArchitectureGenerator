package com.mitteloupe.cag.git

import java.io.File

class Git(
    private val processExecutor: ProcessExecutor = ProcessExecutor()
) {
    fun initializeRepository(directory: File): Boolean {
        if (isGitRepository(directory)) {
            return false
        }

        return processExecutor.run(
            directory,
            listOf("git", "init")
        )
    }

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
        processExecutor.run(projectRoot, gitCommandWithArguments)
    }

    fun stageAll(directory: File): Boolean {
        if (!isGitRepository(directory)) {
            return false
        }

        return processExecutor.run(
            directory,
            listOf("git", "add", "-A")
        )
    }

    fun isGitRepository(directory: File): Boolean {
        val gitDirectory = File(directory, ".git")
        return gitDirectory.exists() && gitDirectory.isDirectory
    }
}
