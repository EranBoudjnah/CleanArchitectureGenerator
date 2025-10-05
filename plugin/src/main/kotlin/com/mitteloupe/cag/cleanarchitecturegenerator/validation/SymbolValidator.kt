package com.mitteloupe.cag.cleanarchitecturegenerator.validation

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.FileSystemWrapper
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.IntelliJFileSystemWrapper
import java.io.File

class SymbolValidator(
    private val fileSystemWrapper: FileSystemWrapper = IntelliJFileSystemWrapper()
) {
    fun isValidSymbolSyntax(type: String): Boolean {
        if (type.isBlank()) {
            return false
        }
        val normalizedType = type.trim()
        val baseType = normalizedType.substringBefore('<')
        return isValidKotlinIdentifier(baseType) &&
            (normalizedType == baseType || hasValidGenericSyntax(normalizedType))
    }

    fun isValidSymbolInContext(
        type: String,
        contextDirectory: File
    ): Boolean {
        if (!isValidSymbolSyntax(type)) {
            return false
        }

        val baseType = type.trim().substringBefore('<')

        if (PRIMITIVE_TYPES.contains(baseType)) {
            return true
        }

        if (COMMON_COLLECTION_TYPES.contains(baseType)) {
            return true
        }

        val entityName =
            if (baseType.contains('.')) {
                baseType.substringAfterLast('.')
            } else {
                baseType
            }

        return findTypeInModule(entityName, contextDirectory)
    }

    private fun isValidKotlinIdentifier(identifier: String): Boolean {
        val parts = identifier.split('.')
        parts.forEach { part ->
            if (part.isEmpty()) {
                return false
            }
            if (!part.first().isLetter() && part.first() != '_') {
                return false
            }
            if (!part.all { it.isLetterOrDigit() || it == '_' }) {
                return false
            }
        }
        return true
    }

    private fun hasValidGenericSyntax(type: String): Boolean {
        var depth = 0
        val bracketStack = mutableListOf<Int>()
        var lastCharacter = ' '

        for (i in type.indices) {
            when (val character = type[i]) {
                '<' -> {
                    if (lastCharacter == '<') {
                        return false
                    }
                    depth++
                    bracketStack.add(i)
                    lastCharacter = character
                }

                '>' -> {
                    if (bracketStack.isEmpty()) {
                        return false
                    }

                    val openBracketIndex = bracketStack.removeAt(bracketStack.size - 1)
                    val contentBetweenBrackets = type.substring(openBracketIndex + 1, i).trim()

                    if (contentBetweenBrackets.isEmpty() || contentBetweenBrackets == ",") {
                        return false
                    }

                    depth--
                    if (depth < 0) {
                        return false
                    }
                    lastCharacter = character
                }

                ',' -> {
                    if (depth == 0) {
                        return false
                    }
                    lastCharacter = character
                }
                else -> lastCharacter = character
            }
        }

        return depth == 0
    }

    private fun findTypeInModule(
        typeName: String,
        contextDirectory: File
    ): Boolean {
        val moduleRoot = findModuleRoot(contextDirectory) ?: return false
        val sourceDirectory = findSourceDirectory(moduleRoot) ?: return false
        val virtualSourceDirectory = fileSystemWrapper.findVirtualFile(sourceDirectory) ?: return false

        return searchForTypeInDirectory(typeName, virtualSourceDirectory)
    }

    private fun findModuleRoot(directory: File): File? {
        var current = directory
        while (current.parentFile != null) {
            if (File(current, "build.gradle.kts").exists() || File(current, "build.gradle").exists()) {
                return current
            }
            current = current.parentFile
        }
        return null
    }

    private fun findSourceDirectory(moduleRoot: File): File? =
        listOf("src/main/kotlin", "src/main/java", "src")
            .asSequence()
            .map { path -> File(moduleRoot, path) }
            .firstOrNull { file ->
                file.exists() && file.isDirectory
            }

    private fun searchForTypeInDirectory(
        typeName: String,
        directory: VirtualFile
    ): Boolean {
        var found = false

        fileSystemWrapper.visitChildrenRecursively(
            directory,
            object : VirtualFileVisitor<Any>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    if (!fileSystemWrapper.isDirectory(file) && fileSystemWrapper.getFileExtension(file) == "kt") {
                        val content = fileSystemWrapper.getFileContents(file)
                        if (containsTypeDefinition(content, typeName)) {
                            found = true
                            return false
                        }
                    }
                    return !found
                }
            }
        )

        return found
    }

    private fun containsTypeDefinition(
        content: String,
        typeName: String
    ): Boolean {
        val patterns =
            listOf(
                """(?:class|interface|object|enum class)\s+$typeName(?:\s|<|\(|$)""".toRegex(),
                """typealias\s+$typeName(?:\s|<|=)""".toRegex()
            )

        return patterns.any { it.containsMatchIn(content) }
    }

    companion object {
        private val PRIMITIVE_TYPES =
            setOf(
                "Unit", "Boolean", "Byte", "Char", "Double", "Float", "Int", "Long", "Short", "String"
            )

        private val COMMON_COLLECTION_TYPES =
            setOf(
                "List", "MutableList", "Set", "MutableSet", "Map", "MutableMap",
                "Collection", "MutableCollection", "Iterable", "MutableIterable",
                "Array", "IntArray", "LongArray", "FloatArray", "DoubleArray", "BooleanArray",
                "ByteArray", "CharArray", "ShortArray"
            )
    }
}
