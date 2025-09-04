package com.mitteloupe.cag.cleanarchitecturegenerator.test.filesystem

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.FileSystemWrapper
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class FakeFileSystemWrapper(
    private val rootDirectory: File
) : FileSystemWrapper {
    fun createFile(
        path: String,
        content: String
    ) {
        val fullPath = File(rootDirectory, path)
        fullPath.parentFile?.mkdirs()
        fullPath.writeText(content)
    }

    fun createDirectory(path: String) {
        val fullPath = File(rootDirectory, path)
        fullPath.mkdirs()
    }

    fun createFakeFile(path: String): File = File(rootDirectory, path)

    override fun findVirtualFile(file: File): VirtualFile? {
        if (!file.exists()) {
            return null
        }

        return TemporaryVirtualFile(file)
    }

    override fun visitChildrenRecursively(
        directory: VirtualFile,
        visitor: VirtualFileVisitor<Any>
    ): VirtualFileVisitor.Result {
        val fakeDirectory = directory as? TemporaryVirtualFile ?: return VirtualFileVisitor.SKIP_CHILDREN
        val directoryFile = fakeDirectory.file

        if (!directoryFile.exists() || !directoryFile.isDirectory) {
            return VirtualFileVisitor.SKIP_CHILDREN
        }

        directoryFile.walkTopDown().forEach { file ->
            if (file != directoryFile) {
                val virtualFile = findVirtualFile(file)
                if (virtualFile != null) {
                    if (!visitor.visitFile(virtualFile)) {
                        return VirtualFileVisitor.SKIP_CHILDREN
                    }
                }
            }
        }

        return VirtualFileVisitor.CONTINUE
    }

    override fun getFileContents(file: VirtualFile): String =
        (file as? TemporaryVirtualFile)?.contentsToByteArray()?.toString(Charsets.UTF_8).orEmpty()

    override fun isDirectory(file: VirtualFile): Boolean = (file as? TemporaryVirtualFile)?.isDirectory == true

    override fun getFileExtension(file: VirtualFile): String? = (file as? TemporaryVirtualFile)?.extension
}

private class TemporaryVirtualFile(val file: File) : VirtualFile() {
    override fun getName(): String = file.name

    override fun getPath(): String = file.absolutePath

    override fun isDirectory(): Boolean = file.isDirectory

    override fun isWritable(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getParent(): VirtualFile? = file.parentFile?.let { TemporaryVirtualFile(it) }

    override fun getChildren(): Array<out VirtualFile> =
        if (file.isDirectory) {
            file.listFiles()?.map { TemporaryVirtualFile(it) }?.toTypedArray().orEmpty()
        } else {
            emptyArray()
        }

    override fun getInputStream(): InputStream = file.readText().byteInputStream()

    override fun getOutputStream(
        requestor: Any?,
        newModificationStamp: Long,
        newTimeStamp: Long
    ): OutputStream = throw UnsupportedOperationException()

    override fun contentsToByteArray(): ByteArray = file.readText().toByteArray()

    override fun getTimeStamp(): Long = 0

    override fun getModificationStamp(): Long = 0

    override fun getExtension(): String? = file.extension.takeIf { it.isNotEmpty() }

    override fun getFileSystem(): VirtualFileSystem = throw UnsupportedOperationException()

    override fun getLength(): Long = contentsToByteArray().size.toLong()

    override fun refresh(
        asynchronous: Boolean,
        recursive: Boolean,
        postRunnable: Runnable?
    ): Unit = Unit
}
