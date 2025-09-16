package com.mitteloupe.cag.cli.filesystem

import com.mitteloupe.cag.core.filesystem.FileSystemBridge
import java.io.File

class CliFileSystemBridge : FileSystemBridge {
    override fun createDirectoryIfNotExists(directory: File) {
        directory.mkdirs()
    }

    override fun exists(file: File): Boolean = file.exists()

    override fun delete(file: File) {
        file.delete()
    }

    override fun writeToFile(
        file: File,
        content: String
    ) {
        if (file.exists()) {
            delete(file)
        }
        file.createNewFile()
        file.writeText(content)
    }

    override fun writeToFile(
        file: File,
        content: ByteArray
    ) {
        file.writeBytes(content)
    }

    override fun copyFile(
        sourceFile: File,
        targetFile: File,
        overwrite: Boolean
    ) {
        sourceFile.copyTo(targetFile, overwrite = overwrite)
    }
}
