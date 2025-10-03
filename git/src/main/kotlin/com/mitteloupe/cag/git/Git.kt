package com.mitteloupe.cag.git

import java.io.File

class Git(
    private val gitBinaryPath: String?,
    private val processExecutor: ProcessExecutor = ProcessExecutor()
) {
    private fun command(vararg args: String): List<String> = listOf(gitBinaryPath ?: "git") + args

    fun isAvailable(workingDirectory: File): Boolean = processExecutor.run(workingDirectory, command("--version"))

    fun initializeRepository(directory: File): Boolean {
        if (isGitRepository(directory)) {
            return false
        }

        return processExecutor.run(directory, command("init"))
    }

    fun stage(
        projectRoot: File,
        files: Collection<File>
    ) {
        if (files.isEmpty()) {
            return
        }
        val gitCommandWithArguments =
            command("add", "--") +
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
            command("add", "-A")
        )
    }

    fun isGitRepository(directory: File): Boolean =
        File(directory, ".git").let { gitDirectory ->
            gitDirectory.exists() && gitDirectory.isDirectory
        }
}
