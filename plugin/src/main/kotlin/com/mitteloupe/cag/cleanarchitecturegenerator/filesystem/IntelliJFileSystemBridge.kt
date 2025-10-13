package com.mitteloupe.cag.cleanarchitecturegenerator.filesystem

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.findOrCreateFile
import com.intellij.openapi.vfs.writeBytes
import com.intellij.util.ModalityUiUtil
import com.mitteloupe.cag.cleanarchitecturegenerator.git.GitAddQueueService
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.AppSettingsService
import com.mitteloupe.cag.core.filesystem.FileSystemBridge
import java.io.File

class IntelliJFileSystemBridge(
    private val project: Project?
) : FileSystemBridge {
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
            ModalityUiUtil.invokeLaterIfNeeded(ModalityState.defaultModalityState()) {
                performWriteAction {
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)?.let { virtualFile ->
                        val parent = virtualFile.parent
                        virtualFile.delete(this)
                        val newFile = parent.findOrCreateFile(file.name)
                        newFile.writeBytes(content)
                        enqueueForGitIfEnabled(file)
                    } ?: let {
                        file.delete()
                        file.createNewFile()
                        file.writeBytes(content)
                        updateAsync(file)
                        enqueueForGitIfEnabled(file)
                    }
                }
            }
        } else {
            file.writeBytes(content)
            updateAsync(file)
            enqueueForGitIfEnabled(file)
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
        enqueueForGitIfEnabled(targetFile)
    }

    private fun updateAsync(file: File) {
        val command = {
            LocalFileSystem.getInstance().refreshIoFiles(listOf(file), false, false, null)
        }
        ModalityUiUtil.invokeLaterIfNeeded(ModalityState.defaultModalityState()) {
            performWriteAction(command)
        }
    }

    private fun enqueueForGitIfEnabled(file: File) {
        val project = project ?: return
        if (!AppSettingsService.getInstance().autoAddGeneratedFilesToGit) {
            return
        }
        try {
            project.service<GitAddQueueService>().enqueue(file)
        } catch (_: Throwable) {
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
