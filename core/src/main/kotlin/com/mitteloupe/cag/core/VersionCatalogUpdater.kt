package com.mitteloupe.cag.core

import java.io.File
import kotlin.text.trim

data class SectionTransaction(
    val sectionHeader: String,
    val insertPositionIfMissing: InsertPosition,
    val requirements: List<SectionRequirement>
)

data class SectionRequirement(val keyRegex: Regex, val lineToAdd: String)

enum class InsertPosition { START, END }

class VersionCatalogUpdater() {
    fun updateVersionCatalogIfPresent(
        projectRootDir: File,
        sectionRequirements: List<SectionTransaction>
    ): String? {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (!catalogFile.exists()) {
            return null
        }

        val catalogContent =
            runCatching { catalogFile.readText() }
                .getOrElse { return "${ERROR_PREFIX}Failed to read version catalog: ${it.message}" }
        val catalogContentLines = catalogContent.split('\n')

        val updatedContent = updateCatalogText(catalogContentLines, sectionRequirements)
        if (updatedContent == catalogContent) {
            return null
        }

        return runCatching { catalogFile.writeText(updatedContent) }
            .exceptionOrNull()
            ?.let { "${ERROR_PREFIX}Failed to update version catalog: ${it.message}" }
    }

    private fun updateCatalogText(
        contentLines: List<String>,
        sectionTransactions: List<SectionTransaction>
    ): String =
        sectionTransactions.fold(contentLines) { currentLines, sectionTransaction ->
            ensureSectionEntries(currentLines, sectionTransaction)
        }.joinToString("\n")

    private fun ensureSectionEntries(
        catalogContentLines: List<String>,
        sectionTransaction: SectionTransaction
    ): List<String> {
        val updatedLines = catalogContentLines.toMutableList()
        val sectionBounds =
            findSectionBounds(catalogContentLines, sectionTransaction.sectionHeader)
        val needToAddSection = sectionBounds == null
        val sectionLinesRange: IntRange =
            if (sectionBounds == null) {
                IntRange(0, -1)
            } else {
                val (start, end) = sectionBounds
                (start + 1) until end
            }

        val linesToAdd =
            buildList {
                if (needToAddSection) add("[${sectionTransaction.sectionHeader}]")
                for (req in sectionTransaction.requirements) {
                    val exists = !needToAddSection && updatedLines.hasKeyInRange(req.keyRegex, sectionLinesRange)
                    if (!exists) add(req.lineToAdd)
                }
            }

        if (linesToAdd.isEmpty()) {
            return updatedLines
        }

        if (needToAddSection) {
            when (sectionTransaction.insertPositionIfMissing) {
                InsertPosition.START -> {
                    updatedLines.addAll(0, linesToAdd + "")
                    // Collapse multiple empty lines immediately following the newly added section to a single one
                    val afterIndex = linesToAdd.size
                    var i = afterIndex + 1
                    while (i < updatedLines.size && updatedLines[i].isEmpty()) {
                        updatedLines.removeAt(i)
                    }
                }
                InsertPosition.END -> {
                    if (updatedLines.isNotEmpty() && updatedLines.last().isNotEmpty()) {
                        // Ensure a single blank line between the previous section and this new one
                        updatedLines.add("")
                    }
                    updatedLines.addAll(linesToAdd)
                    // Ensure exactly one empty line after the newly added section
                    if (updatedLines.isNotEmpty()) {
                        if (updatedLines.last().isNotEmpty()) {
                            updatedLines.add("")
                        } else {
                            // Collapse multiple trailing empty lines to a single one
                            while (updatedLines.size >= 2 && updatedLines[updatedLines.lastIndex - 1].isEmpty()) {
                                updatedLines.removeAt(updatedLines.lastIndex)
                            }
                        }
                    }
                }
            }
        } else {
            val (start, endExclusive) = sectionBounds
            // Insert new entries just before any trailing blank lines that precede the next section header
            var insertionIndex = endExclusive
            while (insertionIndex - 1 > start && updatedLines[insertionIndex - 1].isEmpty()) {
                insertionIndex--
            }
            updatedLines.addAll(insertionIndex, linesToAdd)

            // Ensure there is exactly one empty line separating the section from the next header/content
            val afterIndex = insertionIndex + linesToAdd.size
            if (afterIndex < updatedLines.size) {
                if (updatedLines[afterIndex].isNotEmpty()) {
                    updatedLines.add(afterIndex, "")
                } else {
                    // Collapse multiple empty lines to a single one
                    var i = afterIndex + 1
                    while (i < updatedLines.size && updatedLines[i].isEmpty()) {
                        updatedLines.removeAt(i)
                    }
                }
            }
        }

        return updatedLines
    }

    private fun findSectionBounds(
        contentLines: List<String>,
        header: String
    ): Pair<Int, Int>? {
        val startIndex = contentLines.indexOfFirst { it.trim() == "[$header]" }
        if (startIndex == -1) {
            return null
        }
        var endIndexExclusive = contentLines.size
        for (index in startIndex + 1 until contentLines.size) {
            val trimmedLine = contentLines[index].trim()
            if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                endIndexExclusive = index
                break
            }
        }
        return startIndex to endIndexExclusive
    }

    private fun List<String>.hasKeyInRange(
        keyRegex: Regex,
        range: IntRange
    ): Boolean = range.any { index -> keyRegex.containsMatchIn(this[index]) }
}
