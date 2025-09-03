package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import java.io.File

class GradleFileCreator {
    fun writeGradleFileIfMissing(
        featureRoot: File,
        layer: String,
        content: String
    ) {
        val moduleDirectory = File(featureRoot, layer)
        val buildGradleFile = File(moduleDirectory, "build.gradle.kts")
        if (!buildGradleFile.exists()) {
            runCatching { buildGradleFile.writeText(content) }
                .onFailure { throw GenerationException("Failed to create $layer/build.gradle.kts: ${it.message}") }
        }
    }
}
