package com.mitteloupe.cag.cleanarchitecturegenerator.filesystem

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.writeBytes
import com.mitteloupe.cag.core.filesystem.FileSystemBridge
import java.io.File

class IntelliJFileSystemBridge(private val project: Project?) : FileSystemBridge {
    override fun createDirectoryIfNotExists(directory: File) {
        directory.mkdirs()
        updateAsync(directory)
    }

    override fun exists(file: File): Boolean = file.exists()

    override fun delete(file: File) {
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)?.delete(this) ?: let {
            file.delete()
            updateAsync(file)
        }
    }

    override fun writeToFile(
        file: File,
        content: String
    ) {
        writeToFileInternal(file, content.toByteArray())
    }

    override fun writeToFile(
        file: File,
        content: ByteArray
    ) {
        writeToFileInternal(file, content)
    }

    private fun writeToFileInternal(
        file: File,
        content: ByteArray
    ) {
        if (file.exists()) {
            performWriteAction {
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)?.writeBytes(content) ?: let {
                    file.writeBytes(content)
                    updateAsync(file)
                }
            }
        } else {
            file.writeBytes(content)
            updateAsync(file)
        }
    }

    override fun copyFile(
        sourceFile: File,
        targetFile: File,
        overwrite: Boolean
    ) {
        if (!sourceFile.exists()) {
            return
        }

        if (targetFile.exists() && !overwrite) {
            return
        }

        sourceFile.copyTo(targetFile, overwrite = overwrite)
        updateAsync(targetFile)
    }

    private fun updateAsync(file: File) {
        val command = {
            LocalFileSystem.getInstance().refreshIoFiles(listOf(file), false, false, null)
        }
        ApplicationManager.getApplication().invokeLater {
            performWriteAction(command)
        }
    }

    private fun performWriteAction(command: () -> Unit) {
        if (project == null) {
            ApplicationManager.getApplication().runWriteAction(command)
        } else {
            WriteCommandAction.runWriteCommandAction(project, command)
        }
    }
}
