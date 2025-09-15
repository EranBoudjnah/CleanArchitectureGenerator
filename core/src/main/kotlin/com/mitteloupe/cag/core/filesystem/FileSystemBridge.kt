package com.mitteloupe.cag.core.filesystem

import java.io.File

interface FileSystemBridge {
    fun createDirectoryIfNotExists(directory: File)

    fun exists(file: File): Boolean

    fun delete(file: File)

    fun writeToFile(
        file: File,
        content: String
    )

    fun writeToFile(
        file: File,
        content: ByteArray
    )

    fun copyFile(
        sourceFile: File,
        targetFile: File,
        overwrite: Boolean
    )
}
