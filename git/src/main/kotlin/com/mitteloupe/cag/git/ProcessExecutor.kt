package com.mitteloupe.cag.git

import java.io.File

class ProcessExecutor {
    fun run(
        workingDirectory: File,
        command: List<String>
    ): Boolean =
        try {
            val process =
                try {
                    ProcessBuilder(command)
                        .directory(workingDirectory)
                        .redirectErrorStream(true)
                        .start()
                } catch (_: Exception) {
                    null
                }
            val output = process?.inputStream?.bufferedReader().use { it?.readText() }
            val exitCode = process?.waitFor()
            if (exitCode == 0) {
                true
            } else {
                println(
                    "Command failed: '${command.joinToString(" ")}' in '${workingDirectory.absolutePath}' (exit=$exitCode)\n" +
                        "Output:\n$output"
                )
                false
            }
        } catch (exception: Exception) {
            println("Process execution failed for '${command.joinToString(" ")}' in '${workingDirectory.absolutePath}'")
            println(exception.stackTraceToString())
            false
        }
}
