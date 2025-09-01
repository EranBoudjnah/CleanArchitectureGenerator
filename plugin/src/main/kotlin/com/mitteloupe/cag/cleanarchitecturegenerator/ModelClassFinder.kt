package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import java.io.File

class ModelClassFinder {
    fun findModelClasses(useCaseDirectory: File): List<String> {
        val modelDirectory = findModelDirectory(useCaseDirectory) ?: return emptyList()
        val virtualModelDirectory = findVirtualFile(modelDirectory) ?: return emptyList()

        return findClassesInDirectory(virtualModelDirectory)
    }

    private fun findModelDirectory(useCaseDirectory: File): File? {
        var currentDirectory = useCaseDirectory

        while (currentDirectory.parentFile != null) {
            val modelDirectory = File(currentDirectory.parentFile, "model")
            if (modelDirectory.exists() && modelDirectory.isDirectory) {
                return modelDirectory
            }
            currentDirectory = currentDirectory.parentFile
        }

        return null
    }

    private fun findVirtualFile(file: File): VirtualFile? = LocalFileSystem.getInstance().findFileByIoFile(file)

    private fun findClassesInDirectory(directory: VirtualFile): List<String> {
        val classes = mutableListOf<String>()

        VfsUtil.visitChildrenRecursively(
            directory,
            object : VirtualFileVisitor<Any>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    if (!file.isDirectory && file.extension == "kt") {
                        val content = String(file.contentsToByteArray())
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
