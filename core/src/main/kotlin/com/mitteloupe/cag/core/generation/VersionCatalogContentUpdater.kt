package com.mitteloupe.cag.core.generation

class VersionCatalogContentUpdater {
    fun updateCatalogText(
        catalogText: String,
        sectionTransactions: List<SectionTransaction>
    ): String {
        val contentLines = catalogText.split('\n')
        return sectionTransactions.fold(contentLines) { currentLines, sectionTransaction ->
            ensureSectionEntries(currentLines, sectionTransaction)
        }.joinToString("\n")
    }

    private fun ensureSectionEntries(
        catalogContentLines: List<String>,
        sectionTransaction: SectionTransaction
    ): List<String> {
        val updatedLines = catalogContentLines.toMutableList()
        val sectionBounds = findSectionBounds(catalogContentLines, sectionTransaction.sectionHeader)
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
                    if (!exists) {
                        add(req.lineToAdd)
                    }
                }
            }

        if (linesToAdd.isEmpty()) {
            return updatedLines
        }

        if (needToAddSection) {
            val modifiedLines = sectionTransaction.insertPositionIfMissing.insertAtMissingSection(updatedLines, linesToAdd)
            if (modifiedLines != updatedLines) {
                updatedLines.clear()
                updatedLines.addAll(modifiedLines)
            }
        } else {
            val (start, endExclusive) = sectionBounds
            var insertionIndex = endExclusive
            while (insertionIndex - 1 > start && updatedLines[insertionIndex - 1].isEmpty()) {
                insertionIndex--
            }
            updatedLines.addAll(insertionIndex, linesToAdd)

            val afterInsertionIndex = insertionIndex + linesToAdd.size
            if (afterInsertionIndex < updatedLines.size) {
                if (updatedLines[afterInsertionIndex].isNotEmpty()) {
                    updatedLines.add(afterInsertionIndex, "")
                } else {
                    val blankLineIndex = afterInsertionIndex + 1
                    while (blankLineIndex < updatedLines.size && updatedLines[blankLineIndex].isEmpty()) {
                        updatedLines.removeAt(blankLineIndex)
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
