package com.mitteloupe.cag.cleanarchitecturegenerator.git

import com.intellij.openapi.diagnostic.Logger
import java.io.File

internal class ProcessExecutor {
    private val logger = Logger.getInstance(ProcessExecutor::class.java)

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
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                logger.warn("Command failed: '${args.joinToString(" ")}' in '${directory.absolutePath}' (exit=$exitCode)\nOutput:\n$output")
            }
        } catch (e: Exception) {
            logger.warn("Process execution failed for '${args.joinToString(" ")}' in '${directory.absolutePath}'", e)
        }
    }
}
