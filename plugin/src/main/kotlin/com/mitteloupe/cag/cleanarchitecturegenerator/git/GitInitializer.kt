package com.mitteloupe.cag.cleanarchitecturegenerator.git

import java.io.File

internal class GitInitializer(private val executor: ProcessExecutor = ProcessExecutor()) {
    fun initialize(projectRoot: File) {
        executor.run(projectRoot, listOf("git", "init"))
    }
}
