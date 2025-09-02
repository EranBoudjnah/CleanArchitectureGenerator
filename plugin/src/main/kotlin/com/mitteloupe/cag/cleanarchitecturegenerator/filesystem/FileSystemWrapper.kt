package com.mitteloupe.cag.cleanarchitecturegenerator.filesystem

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import java.io.File

interface FileSystemWrapper {
    fun findVirtualFile(file: File): VirtualFile?

    fun visitChildrenRecursively(
        directory: VirtualFile,
        visitor: VirtualFileVisitor<Any>
    ): VirtualFileVisitor.Result

    fun getFileContents(file: VirtualFile): String

    fun isDirectory(file: VirtualFile): Boolean

    fun getFileExtension(file: VirtualFile): String?
}

class IntelliJFileSystemWrapper : FileSystemWrapper {
    override fun findVirtualFile(file: File): VirtualFile? = LocalFileSystem.getInstance().findFileByIoFile(file)

    override fun visitChildrenRecursively(
        directory: VirtualFile,
        visitor: VirtualFileVisitor<Any>
    ): VirtualFileVisitor.Result = VfsUtil.visitChildrenRecursively(directory, visitor)

    override fun getFileContents(file: VirtualFile): String = String(file.contentsToByteArray())

    override fun isDirectory(file: VirtualFile): Boolean = file.isDirectory

    override fun getFileExtension(file: VirtualFile): String? = file.extension
}
