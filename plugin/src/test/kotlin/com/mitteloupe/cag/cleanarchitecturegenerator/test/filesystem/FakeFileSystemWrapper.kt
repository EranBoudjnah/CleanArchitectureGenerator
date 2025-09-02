package com.mitteloupe.cag.cleanarchitecturegenerator.test.filesystem

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.FileSystemWrapper
import java.io.File

class FakeFileSystemWrapper(
    private val rootDirectory: File
) : FileSystemWrapper {
    private val files = mutableMapOf<String, FakeVirtualFile>()
    private val directories = mutableMapOf<String, FakeVirtualDirectory>()

    fun createFile(
        path: String,
        content: String,
        extension: String? = null
    ) {
        val fullPath = File(rootDirectory, path)
        fullPath.parentFile?.mkdirs()
        fullPath.writeText(content)

        val file = FakeVirtualFile(fullPath.absolutePath, content, extension)
        files[fullPath.absolutePath] = file
    }

    fun createDirectory(path: String) {
        val fullPath = File(rootDirectory, path)
        fullPath.mkdirs()

        val directory = FakeVirtualDirectory(fullPath.absolutePath)
        directories[fullPath.absolutePath] = directory
    }

    fun createFakeFile(path: String): FakeFile {
        val fullPath = File(rootDirectory, path)
        return FakeFile(fullPath.absolutePath, this)
    }

    fun reset() {
        rootDirectory.deleteRecursively()
        rootDirectory.mkdirs()
        files.clear()
        directories.clear()
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
        val path = file.absolutePath
        return files[path] ?: directories[path]
    }

    override fun visitChildrenRecursively(
        directory: VirtualFile,
        visitor: VirtualFileVisitor<Any>
    ): VirtualFileVisitor.Result {
        val fakeDirectory = directory as? FakeVirtualDirectory ?: return VirtualFileVisitor.SKIP_CHILDREN

        for (file in files.values) {
            if (file.path.startsWith(fakeDirectory.path + "/")) {
                val shouldContinue = visitor.visitFile(file)
                if (!shouldContinue) {
                    return VirtualFileVisitor.SKIP_CHILDREN
                }
            }
        }

        return VirtualFileVisitor.CONTINUE
    }

    override fun getFileContents(file: VirtualFile): String {
        val fakeFile = file as? FakeVirtualFile ?: return ""
        return fakeFile.content
    }

    override fun isDirectory(file: VirtualFile): Boolean {
        return file is FakeVirtualDirectory
    }

    override fun getFileExtension(file: VirtualFile): String? {
        val fakeFile = file as? FakeVirtualFile ?: return null
        return fakeFile.extension
    }
}

class FakeFile(
    private val path: String,
    private val fileSystem: FakeFileSystemWrapper
) : File(path) {
    override fun exists(): Boolean = fileSystem.fileExists(path)

    override fun isDirectory(): Boolean = fileSystem.isDirectory(path)

    override fun getParentFile(): FakeFile? {
        val parentPath = File(path).parent
        return if (parentPath != null) FakeFile(parentPath, fileSystem) else null
    }
}

private class FakeVirtualFile(
    private val path: String,
    val content: String,
    private val extension: String?
) : VirtualFile() {
    override fun getName(): String = File(path).name

    override fun getPath(): String = path

    override fun isDirectory(): Boolean = false

    override fun isWritable(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getParent(): VirtualFile? = null

    override fun getChildren(): Array<VirtualFile> = emptyArray()

    override fun getInputStream(): java.io.InputStream = content.byteInputStream()

    override fun getOutputStream(
        requestor: Any?,
        newModificationStamp: Long,
        newTimeStamp: Long
    ): java.io.OutputStream = throw UnsupportedOperationException()

    override fun contentsToByteArray(): ByteArray = content.toByteArray()

    override fun getTimeStamp(): Long = 0

    override fun getModificationStamp(): Long = 0

    override fun getExtension(): String? = extension

    override fun getFileSystem(): VirtualFileSystem = throw UnsupportedOperationException()

    override fun getLength(): Long = content.length.toLong()

    override fun refresh(
        asynchronous: Boolean,
        recursive: Boolean,
        postRunnable: Runnable?
    ): Unit = Unit
}

private class FakeVirtualDirectory(
    private val path: String
) : VirtualFile() {
    override fun getName(): String = File(path).name

    override fun getPath(): String = path

    override fun isDirectory(): Boolean = true

    override fun isWritable(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getParent(): VirtualFile? = null

    override fun getChildren(): Array<VirtualFile> = emptyArray()

    override fun getInputStream(): java.io.InputStream = throw UnsupportedOperationException()

    override fun getOutputStream(
        requestor: Any?,
        newModificationStamp: Long,
        newTimeStamp: Long
    ): java.io.OutputStream = throw UnsupportedOperationException()

    override fun contentsToByteArray(): ByteArray = throw UnsupportedOperationException()

    override fun getTimeStamp(): Long = 0

    override fun getModificationStamp(): Long = 0

    override fun getExtension(): String? = null

    override fun getFileSystem(): VirtualFileSystem = throw UnsupportedOperationException()

    override fun getLength(): Long = 0

    override fun refresh(
        asynchronous: Boolean,
        recursive: Boolean,
        postRunnable: Runnable?
    ): Unit = Unit
}
