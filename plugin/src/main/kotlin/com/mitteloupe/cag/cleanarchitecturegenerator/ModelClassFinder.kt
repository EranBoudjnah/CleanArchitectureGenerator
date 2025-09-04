package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.FileSystemWrapper
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.IntelliJFileSystemWrapper
import java.io.File

class ModelClassFinder(
    private val fileSystemWrapper: FileSystemWrapper = IntelliJFileSystemWrapper()
) {
    fun findModelClasses(useCaseDirectory: File): List<String> {
        val modelDirectory = findModelDirectory(useCaseDirectory) ?: return emptyList()
        val virtualModelDirectory = fileSystemWrapper.findVirtualFile(modelDirectory) ?: return emptyList()

        return findClassesInDirectory(virtualModelDirectory)
    }

    private fun findModelDirectory(useCaseDirectory: File): File? {
        var currentDirectory = useCaseDirectory

        while (currentDirectory.parentFile != null) {
            val modelDirectoryInCurrent = File(currentDirectory, "model")
            if (modelDirectoryInCurrent.exists() && modelDirectoryInCurrent.isDirectory) {
                return modelDirectoryInCurrent
            }

            val modelDirectory = File(currentDirectory.parentFile, "model")
            if (modelDirectory.exists() && modelDirectory.isDirectory) {
                return modelDirectory
            }

            val srcMainKotlinModelDirectory = findModelDirectoryInSrcMainKotlin(currentDirectory)
            if (srcMainKotlinModelDirectory != null) {
                return srcMainKotlinModelDirectory
            }

            currentDirectory = currentDirectory.parentFile
        }

        return null
    }

    private fun findModelDirectoryInSrcMainKotlin(directory: File): File? {
        var currentDirectory = directory
        while (currentDirectory.parentFile != null) {
            val srcDirectory = File(currentDirectory, "src")
            if (srcDirectory.exists() && srcDirectory.isDirectory) {
                val mainDirectory = File(srcDirectory, "main")
                if (mainDirectory.exists() && mainDirectory.isDirectory) {
                    val kotlinDirectory = File(mainDirectory, "kotlin")
                    val javaDirectory = File(mainDirectory, "java")
                    if (javaDirectory.exists() && javaDirectory.isDirectory) {
                        val modelDirectory = findModelDirectoryRecursively(javaDirectory)
                        if (modelDirectory != null) {
                            return modelDirectory
                        }
                    }
                    if (kotlinDirectory.exists() && kotlinDirectory.isDirectory) {
                        val modelDirectory = findModelDirectoryRecursively(kotlinDirectory)
                        if (modelDirectory != null) {
                            return modelDirectory
                        }
                    }
                }
            }
            currentDirectory = currentDirectory.parentFile
        }
        return null
    }

    private fun findModelDirectoryRecursively(directory: File): File? {
        if (!directory.exists() || !directory.isDirectory) {
            return null
        }

        if (directory.name == "model") {
            return directory
        }

        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                val result = findModelDirectoryRecursively(file)
                if (result != null) {
                    return result
                }
            }
        }

        return null
    }

    private fun findClassesInDirectory(directory: VirtualFile): List<String> {
        val classes = mutableListOf<String>()

        fileSystemWrapper.visitChildrenRecursively(
            directory,
            object : VirtualFileVisitor<Any>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    if (!fileSystemWrapper.isDirectory(file) && fileSystemWrapper.getFileExtension(file) == "kt") {
                        val content = fileSystemWrapper.getFileContents(file)
                        classes.addAll(extractClassesFromContent(content))
                    }
                    return true
                }
            }
        )

        return classes
    }

    internal fun extractClassesFromContent(content: String): List<String> {
        val classes = mutableListOf<String>()

        val packagePattern = Regex("""package\s+([a-zA-Z_][a-zA-Z0-9_.]*)""")
        val packageMatch = packagePattern.find(content)
        val packageName = packageMatch?.groupValues?.get(1)

        val classPattern = Regex("""(?:class|interface)\s+(\w+)""")
        val matches = classPattern.findAll(content)

        matches.forEach { matchResult ->
            val className = matchResult.groupValues[1]
            if (className.isNotEmpty()) {
                val fullyQualifiedName =
                    if (packageName != null) {
                        "$packageName.$className"
                    } else {
                        className
                    }
                classes.add(fullyQualifiedName)
            }
        }

        return classes
    }

    companion object {
        val PRIMITIVE_TYPES =
            listOf(
                "Unit",
                "Boolean",
                "Byte",
                "Char",
                "Double",
                "Float",
                "Int",
                "Long",
                "Short",
                "String"
            )
    }
}
