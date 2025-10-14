package com.mitteloupe.cag.core.generation.versioncatalog

sealed interface CatalogInsertPosition {
    fun insertAtMissingSection(
        contentLines: List<String>,
        linesToAdd: List<String>
    ): List<String>

    data object Start : CatalogInsertPosition {
        override fun insertAtMissingSection(
            contentLines: List<String>,
            linesToAdd: List<String>
        ): List<String> {
            val updatedLines = contentLines.toMutableList()
            updatedLines.addAll(0, linesToAdd + "")
            val blankLineIndex = linesToAdd.size + 1
            while (blankLineIndex < updatedLines.size && updatedLines[blankLineIndex].isEmpty()) {
                updatedLines.removeAt(blankLineIndex)
            }
            return updatedLines
        }
    }

    data object End : CatalogInsertPosition {
        override fun insertAtMissingSection(
            contentLines: List<String>,
            linesToAdd: List<String>
        ): List<String> {
            val updatedLines = contentLines.toMutableList()
            if (updatedLines.isNotEmpty() && updatedLines.last().isNotEmpty()) {
                updatedLines.add("")
            }
            updatedLines.addAll(linesToAdd)
            if (updatedLines.isNotEmpty()) {
                if (updatedLines.last().isNotEmpty()) {
                    updatedLines.add("")
                } else {
                    while (updatedLines.size >= 2 && updatedLines[updatedLines.lastIndex - 1].isEmpty()) {
                        updatedLines.removeAt(updatedLines.lastIndex)
                    }
                }
            }
            return updatedLines
        }
    }
}
