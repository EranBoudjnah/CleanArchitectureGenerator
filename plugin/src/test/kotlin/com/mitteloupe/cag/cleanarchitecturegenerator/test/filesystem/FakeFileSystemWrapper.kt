package com.mitteloupe.cag.cleanarchitecturegenerator.test.filesystem

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.FileSystemWrapper
import java.io.File
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

    fun createFakeFile(path: String): TemporaryFile {
        val fullPath = File(rootDirectory, path)
        return TemporaryFile(fullPath.absolutePath, this)
    }

    fun fileExists(path: String): Boolean {
        val fullPath = File(rootDirectory, path)
        return fullPath.exists()
    }

    fun isDirectory(path: String): Boolean {
        val fullPath = File(rootDirectory, path)
        return fullPath.isDirectory
    }

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
        val directoryFile = File(fakeDirectory.path)

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

class TemporaryFile(
    private val path: String,
    private val fileSystem: FakeFileSystemWrapper
) : File(path) {
    override fun exists(): Boolean = fileSystem.fileExists(path)

    override fun isDirectory(): Boolean = fileSystem.isDirectory(path)

    override fun getParentFile(): TemporaryFile? {
        val parentPath = File(path).parent
        return if (parentPath != null) {
            TemporaryFile(parentPath, fileSystem)
        } else {
            null
        }
    }
}

private class TemporaryVirtualFile(private val file: File) : VirtualFile() {
    override fun getName(): String = File(path).name

    override fun getPath(): String = file.absolutePath

    override fun isDirectory(): Boolean = false

    override fun isWritable(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getParent(): VirtualFile? = null

    override fun getChildren(): Array<VirtualFile> = emptyArray()

    override fun getInputStream(): java.io.InputStream = file.readText().byteInputStream()

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
