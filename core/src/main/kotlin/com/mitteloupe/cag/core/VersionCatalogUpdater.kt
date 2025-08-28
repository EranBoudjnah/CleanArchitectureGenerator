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
                InsertPosition.START -> updatedLines.addAll(0, linesToAdd + "")
                InsertPosition.END -> {
                    if (updatedLines.isNotEmpty() && updatedLines.last().isNotEmpty()) {
                        updatedLines.add("")
                    }
                    updatedLines.addAll(linesToAdd)
                }
            }
        } else {
            val (_, end) = sectionBounds
            updatedLines.addAll(end, listOf("") + linesToAdd)
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
    ): Boolean = range.any { idx -> keyRegex.containsMatchIn(this[idx]) }
}
