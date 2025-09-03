package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.AppModuleDirectoryFinder
import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.findGradleProjectRoot
import java.io.File

private val FEATURE_LAYERS = listOf("ui", "presentation", "domain", "data")

class AppModuleGradleUpdater(private val directoryFinder: DirectoryFinder = DirectoryFinder()) {
    fun updateAppModuleDependenciesIfPresent(
        startDirectory: File,
        featureNameLowerCase: String
    ) {
        val projectRoot = findGradleProjectRoot(startDirectory, directoryFinder) ?: startDirectory
        val appModuleDirectory =
            AppModuleDirectoryFinder(directoryFinder)
                .findAndroidAppModuleDirectories(projectRoot)
                .firstOrNull() ?: return

        val kotlinDslFile = File(appModuleDirectory, "build.gradle.kts")
        val groovyDslFile = File(appModuleDirectory, "build.gradle")

        when {
            kotlinDslFile.exists() -> updateDslFile(kotlinDslFile, featureNameLowerCase, isKotlinDsl = true)
            groovyDslFile.exists() -> updateDslFile(groovyDslFile, featureNameLowerCase, isKotlinDsl = false)
        }
    }

    private fun updateDslFile(
        file: File,
        featureNameLowerCase: String,
        isKotlinDsl: Boolean
    ) {
        val desiredLines =
            if (isKotlinDsl) {
                FEATURE_LAYERS.map { layer -> "implementation(projects.features.$featureNameLowerCase.$layer)" }
            } else {
                FEATURE_LAYERS.map { layer -> "implementation(project(\":features:$featureNameLowerCase:$layer\"))" }
            }
        updateFileWithDesiredLines(file, desiredLines)
    }

    private fun updateFileWithDesiredLines(
        file: File,
        desiredLines: List<String>
    ) {
        val original = runCatching { file.readText() }.getOrElse { throw errorForFileRead(file.name, it) }

        val updated = insertIntoDependenciesBlock(original, desiredLines)
        if (updated == null) {
            val indent = original.preferredIndentation()
            val newline = original.preferredEndOfLine()
            val block =
                buildString {
                    append(newline)
                    append("dependencies {").append(newline)
                    desiredLines.forEach { line ->
                        append(indent).append(line).append(newline)
                    }
                    append("}").append(newline)
                }
            val finalContent = original + block
            writeFile(file, finalContent)
            return
        }
        if (updated == original) {
            return
        }
        writeFile(file, updated)
    }

    private fun insertIntoDependenciesBlock(
        original: String,
        desiredLines: List<String>
    ): String? {
        val block = findDependenciesBlock(original) ?: return null
        val newline = original.preferredEndOfLine()

        val blockInner = original.substring(block.openBraceIndex + 1, block.endIndex)
        val existingLines = blockInner.lines().map { it.trim() }
        val missingLines = desiredLines.filter { desired -> existingLines.none { it.contains(desired) } }
        if (missingLines.isEmpty()) {
            return original
        }

        val indent = determineIndentation(blockInner) ?: original.preferredIndentation()

        val prefix = original.take(block.endIndex)
        val suffix = original.substring(block.endIndex)

        val needsNewline = prefix.isNotEmpty() && !prefix.endsWith("\n") && !prefix.endsWith("\r\n")
        val insertion =
            buildString {
                if (needsNewline) {
                    append(newline)
                }
                missingLines.forEach { line ->
                    append(indent).append(line).append(newline)
                }
            }

        return prefix + insertion + suffix
    }

    private fun determineIndentation(blockContent: String): String? {
        val lines = blockContent.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                continue
            }
            return line.substringBefore(trimmed)
        }
        return null
    }

    private fun findDependenciesBlock(text: String): BlockRange? {
        var index = 0
        val length = text.length
        while (index < length) {
            while (index < length && text[index].isWhitespace()) {
                index++
            }
            if (index >= length) {
                break
            }

            if (isWordAt(text, index, "dependencies")) {
                val keywordStart = index
                index += "dependencies".length
                val noCodeIndex = skipWhitespaceAndComments(text, index)
                if (noCodeIndex < length && text[noCodeIndex] == '{') {
                    val endIndex = findMatchingBrace(text, noCodeIndex)
                    if (endIndex != -1) {
                        return BlockRange(noCodeIndex, endIndex)
                    }
                }
                index = keywordStart + 1
            } else {
                index++
            }
        }
        return null
    }

    private fun isWordAt(
        text: String,
        index: Int,
        word: String
    ): Boolean {
        if (index + word.length > text.length) {
            return false
        }
        if (!text.regionMatches(index, word, 0, word.length)) {
            return false
        }
        val beforeOk = index == 0 || !text[index - 1].isLetterOrDigit() && text[index - 1] != '_'
        val afterIndex = index + word.length
        val afterOk = afterIndex >= text.length || !text[afterIndex].isLetterOrDigit() && text[afterIndex] != '_'
        return beforeOk && afterOk
    }

    private fun skipWhitespaceAndComments(
        text: String,
        start: Int
    ): Int {
        var i = start
        val length = text.length
        loop@ while (i < length) {
            when (text[i]) {
                ' ', '\t', '\r', '\n' -> i++
                '/' -> {
                    if (i + 1 < length) {
                        val next = text[i + 1]
                        if (next == '/') {
                            i += 2
                            while (i < length && text[i] != '\n') {
                                i++
                            }
                        } else {
                            if (next == '*') {
                                i += 2
                                while (i + 1 < length && !(text[i] == '*' && text[i + 1] == '/')) {
                                    i++
                                }
                                if (i + 1 < length) {
                                    i += 2
                                }
                            } else {
                                break@loop
                            }
                        }
                    } else {
                        break@loop
                    }
                }
                else -> break@loop
            }
        }
        return i
    }

    private fun findMatchingBrace(
        text: String,
        openIndex: Int
    ): Int {
        var currentIndex = openIndex
        val length = text.length
        var depth = 0
        var inSingleQuotes = false
        var inDoubleQuotes = false
        var inLineComment = false
        var inBlockComment = false
        var escape = false
        while (currentIndex < length) {
            val c = text[currentIndex]
            if (inLineComment) {
                if (c == '\n') inLineComment = false
                currentIndex++
                continue
            }
            if (inBlockComment) {
                if (c == '*' && currentIndex + 1 < length && text[currentIndex + 1] == '/') {
                    inBlockComment = false
                    currentIndex += 2
                    continue
                }
                currentIndex++
                continue
            }
            if (inSingleQuotes) {
                if (!escape && c == '\\') {
                    escape = true
                    currentIndex++
                    continue
                }
                if (!escape && c == '\'') {
                    inSingleQuotes = false
                }
                escape = false
                currentIndex++
                continue
            }
            if (inDoubleQuotes) {
                if (!escape && c == '\\') {
                    escape = true
                    currentIndex++
                    continue
                }
                if (!escape && c == '"') {
                    inDoubleQuotes = false
                }
                escape = false
                currentIndex++
                continue
            }
            if (c == '/' && currentIndex + 1 < length) {
                val next = text[currentIndex + 1]
                if (next == '/') {
                    inLineComment = true
                    currentIndex += 2
                    continue
                } else if (next == '*') {
                    inBlockComment = true
                    currentIndex += 2
                    continue
                }
            }
            when (c) {
                '\'' -> inSingleQuotes = true
                '"' -> inDoubleQuotes = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return currentIndex
                }
            }
            currentIndex++
        }
        return -1
    }

    private fun writeFile(
        file: File,
        content: String
    ) {
        runCatching { file.writeText(content) }
            .exceptionOrNull()
            ?.let { throw GenerationException("Failed to update ${file.name}: ${it.message}") }
    }

    private fun errorForFileRead(
        fileName: String,
        throwable: Throwable
    ): GenerationException = GenerationException("Failed to read $fileName: ${throwable.message}")

    private data class BlockRange(val openBraceIndex: Int, val endIndex: Int)
}
