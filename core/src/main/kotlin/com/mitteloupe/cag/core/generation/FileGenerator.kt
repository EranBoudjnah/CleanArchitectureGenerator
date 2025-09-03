package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import java.io.File

fun generateFileIfMissing(
    packageDirectory: File,
    relativePath: String,
    content: String,
    errorMessage: String
) {
    val file = File(packageDirectory, relativePath)
    if (file.exists()) {
        return
    }

    runCatching {
        file.parentFile.mkdirs()
        file.writeText(content)
    }.onFailure {
        throw GenerationException("Failed to generate $errorMessage: ${it.message}")
    }
}
