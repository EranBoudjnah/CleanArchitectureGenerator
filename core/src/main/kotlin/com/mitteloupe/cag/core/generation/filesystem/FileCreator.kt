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

    fun copyResourceDirectoryIfNotExists(
        targetDirectory: File,
        resourcePath: String,
        classLoader: ClassLoader
    ) {
        createDirectoryIfNotExists(targetDirectory)

        classLoader.getResource(resourcePath)
            ?: throw GenerationException("Resource directory not found: $resourcePath")

        try {
            copyResourceDirectoryContents(targetDirectory, resourcePath, classLoader)
        } catch (exception: Exception) {
            throw GenerationException("Failed to copy resource directory $resourcePath: ${exception.message}")
        }
    }

    private fun copyResourceDirectoryContents(
        targetDirectory: File,
        resourcePath: String,
        classLoader: ClassLoader
    ) {
        val resourceUrl = classLoader.getResource(resourcePath)
        if (resourceUrl?.protocol == "jar") {
            copyJarResourceDirectory(targetDirectory, resourcePath, classLoader)
        } else {
            copyFileSystemResourceDirectory(targetDirectory, resourcePath, classLoader)
        }
    }

    private fun copyJarResourceDirectory(
        targetDirectory: File,
        resourcePath: String,
        classLoader: ClassLoader
    ) {
        val jarConnection = classLoader.getResource(resourcePath)?.openConnection()
        if (jarConnection is java.net.JarURLConnection) {
            val jarFile = jarConnection.jarFile
            val entries = jarFile.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.startsWith(resourcePath) && !entry.isDirectory) {
                    val relativePath = entry.name.substring(resourcePath.length + 1)
                    val targetFile = File(targetDirectory, relativePath)

                    if (!targetFile.exists()) {
                        createDirectoryIfNotExists(targetFile.parentFile)

                        jarFile.getInputStream(entry).use { inputStream ->
                            targetFile.writeBytes(inputStream.readBytes())
                        }
                    }
                }
            }
        }
    }

    private fun copyFileSystemResourceDirectory(
        targetDirectory: File,
        resourcePath: String,
        classLoader: ClassLoader
    ) {
        val resourceUri = requireNotNull(classLoader.getResource(resourcePath)?.toURI())
        val resourceFile = File(resourceUri)
        if (resourceFile.isDirectory) {
            resourceFile.listFiles()?.forEach { sourceFile ->
                val targetFile = File(targetDirectory, sourceFile.name)
                if (sourceFile.isDirectory) {
                    copyResourceDirectoryIfNotExists(targetFile, "$resourcePath/${sourceFile.name}", classLoader)
                } else if (!targetFile.exists()) {
                    sourceFile.copyTo(targetFile, overwrite = false)
                }
            }
        }
    }
}
