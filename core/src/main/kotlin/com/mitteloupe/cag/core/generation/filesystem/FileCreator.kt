package com.mitteloupe.cag.core.generation.filesystem

import com.mitteloupe.cag.core.GenerationException
import java.io.File

object FileCreator {
    fun createDirectoryIfNotExists(directory: File) {
        if (!directory.exists()) {
            runCatching { directory.mkdirs() }.getOrElse { GenerationException("Failed to create directory: ${directory.absolutePath}") }
        }
    }

    fun createFileIfNotExists(
        file: File,
        contentProvider: () -> String
    ) {
        if (file.exists()) {
            return
        }

        val content = contentProvider()
        runCatching { file.writeText(content) }
            .onFailure {
                val absolutePath = file.absolutePath
                throw GenerationException("Failed to create file: $absolutePath: ${it.message}")
            }
    }

    fun createBinaryFileIfNotExists(
        file: File,
        contentProvider: () -> ByteArray
    ) {
        if (file.exists()) {
            return
        }

        val content = contentProvider()
        runCatching { file.writeBytes(content) }
            .onFailure {
                val absolutePath = file.absolutePath
                throw GenerationException("Failed to create file: $absolutePath: ${it.message}")
            }
    }
}
