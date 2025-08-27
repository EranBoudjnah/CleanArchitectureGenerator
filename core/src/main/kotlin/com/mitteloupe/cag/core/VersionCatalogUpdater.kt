package com.mitteloupe.cag.core

import kotlin.text.trim

data class SectionRequirement(val keyRegex: Regex, val lineToAdd: String)

enum class InsertPosition { START, END }

class VersionCatalogUpdater(catalogContent: String) {
    private val lines = catalogContent.split('\n').toMutableList()

    fun asString() = lines.joinToString("\n")

    fun ensureSectionEntries(
        header: String,
        requirements: List<SectionRequirement>,
        insertPositionIfMissing: InsertPosition
    ) {
        val sectionBounds = findSectionBounds(header)
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
                if (needToAddSection) add("[$header]")
                for (req in requirements) {
                    val exists = !needToAddSection && hasKeyInRange(req.keyRegex, sectionLinesRange)
                    if (!exists) add(req.lineToAdd)
                }
            }

        if (linesToAdd.isEmpty()) {
            return
        }

        if (needToAddSection) {
            when (insertPositionIfMissing) {
                InsertPosition.START -> insertAt(0, linesToAdd + "")
                InsertPosition.END -> {
                    if (lines.isNotEmpty() && lines.last().isNotEmpty()) {
                        lines.add("")
                    }
                    lines.addAll(linesToAdd)
                }
            }
        } else {
            val (_, end) = sectionBounds
            insertAt(end, listOf("") + linesToAdd)
        }
    }

    private fun findSectionBounds(header: String): Pair<Int, Int>? {
        val startIndex = lines.indexOfFirst { it.trim() == "[$header]" }
        if (startIndex == -1) {
            return null
        }
        var endIndexExclusive = lines.size
        for (i in startIndex + 1 until lines.size) {
            val trimmedLine = lines[i].trim()
            if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                endIndexExclusive = i
                break
            }
        }
        return startIndex to endIndexExclusive
    }

    private fun hasKeyInRange(
        keyRegex: Regex,
        range: IntRange
    ): Boolean = range.any { idx -> keyRegex.containsMatchIn(lines[idx]) }

    private fun insertAt(
        index: Int,
        newLines: List<String>
    ) {
        lines.addAll(index, newLines)
    }
}
