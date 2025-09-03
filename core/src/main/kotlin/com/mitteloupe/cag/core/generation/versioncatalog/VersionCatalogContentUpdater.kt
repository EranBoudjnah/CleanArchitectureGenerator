package com.mitteloupe.cag.core.generation.versioncatalog

private val VERSION_CATALOG_PLUGIN_ENTRY_REGEX =
    """(?m)^\s*([A-Za-z0-9_.\-]+)\s*=\s*\{[^}]*?\bid\s*=\s*['"]([^'"]+)['"][^}]*}.*$""".toRegex()

private val VERSION_CATALOG_LIBRARY_ENTRY_WITH_MODULE_REGEX =
    """(?m)^\s*([A-Za-z0-9_.\-]+)\s*=\s*\{[^}]*?\bmodule\s*=\s*['"]([^'"]+)['"][^}]*}.*$""".toRegex()

class VersionCatalogContentUpdater {
    fun <SECTION_TYPE : SectionEntryRequirement> updateCatalogText(
        catalogText: String,
        sectionTransactions: List<SectionTransaction<SECTION_TYPE>>
    ): String {
        val contentLines = catalogText.split('\n')
        return sectionTransactions.fold(contentLines) { currentLines, sectionTransaction ->
            ensureSectionEntries(currentLines, sectionTransaction)
        }.joinToString("\n")
    }

    private fun <SECTION_TYPE : SectionEntryRequirement> ensureSectionEntries(
        catalogContentLines: List<String>,
        sectionTransaction: SectionTransaction<SECTION_TYPE>
    ): List<String> {
        val updatedLines = catalogContentLines.toMutableList()
        if (sectionTransaction.requirements.isEmpty()) {
            return updatedLines
        }

        val header = sectionTransaction.requirements.first().header
        val sectionBounds = findSectionBounds(catalogContentLines, header)
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
                for (req in sectionTransaction.requirements) {
                    val keyRegex = buildKeyRegex(req.key)
                    val exists = !needToAddSection && updatedLines.hasKeyInRange(keyRegex, sectionLinesRange)
                    if (!exists) add(formatRequirementLine(req))
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

    private fun buildKeyRegex(key: String): Regex = ("""(?m)^\s*${Regex.escape(key)}\s*=""").toRegex()

    private fun formatRequirementLine(req: SectionEntryRequirement): String =
        when (req) {
            is SectionEntryRequirement.VersionRequirement -> {
                "${req.key} = \"${req.version}\""
            }
            is SectionEntryRequirement.LibraryRequirement -> {
                val parts = mutableListOf("module = \"${req.module}\"")
                when {
                    req.versionRefKey != null -> parts.add("version.ref = \"${req.versionRefKey}\"")
                    req.versionLiteral != null -> parts.add("version = \"${req.versionLiteral}\"")
                }
                "${req.key} = { ${parts.joinToString(", ")} }"
            }
            is SectionEntryRequirement.BundleRequirement -> {
                val members = req.members.joinToString(", ") { "\"$it\"" }
                "${req.key} = [ $members ]"
            }
            is SectionEntryRequirement.PluginRequirement -> {
                "${req.key} = { id = \"${req.id}\", version.ref = \"${req.versionRefKey}\" }"
            }
        }

    fun parseExistingPluginIdToAlias(catalogText: String): Map<String, String> {
        val contentLines = catalogText.split('\n')
        val sectionBounds = findSectionBounds(contentLines, "plugins") ?: return emptyMap()
        val (start, endExclusive) = sectionBounds
        val sectionText = contentLines.subList(start + 1, endExclusive).joinToString("\n")
        val aliasToId =
            VERSION_CATALOG_PLUGIN_ENTRY_REGEX.findAll(sectionText).associate { match ->
                val alias = match.groupValues[1].trim()
                val id = match.groupValues[2].trim()
                alias to id
            }
        return aliasToId.entries.associate { (alias, id) -> id to alias }
    }

    fun parseExistingPluginAliasToId(catalogText: String): Map<String, String> {
        val contentLines = catalogText.split('\n')
        val sectionBounds = findSectionBounds(contentLines, "plugins") ?: return emptyMap()
        val (start, endExclusive) = sectionBounds
        val sectionText = contentLines.subList(start + 1, endExclusive).joinToString("\n")
        return VERSION_CATALOG_PLUGIN_ENTRY_REGEX.findAll(sectionText).associate { match ->
            val alias = match.groupValues[1].trim()
            val id = match.groupValues[2].trim()
            alias to id
        }
    }

    fun parseExistingLibraryAliasToModule(catalogText: String): Map<String, String> {
        val contentLines = catalogText.split('\n')
        val sectionBounds = findSectionBounds(contentLines, "libraries") ?: return emptyMap()
        val (start, endExclusive) = sectionBounds
        val sectionText = contentLines.subList(start + 1, endExclusive).joinToString("\n")
        return VERSION_CATALOG_LIBRARY_ENTRY_WITH_MODULE_REGEX.findAll(sectionText).associate { match ->
            val alias = match.groupValues[1].trim()
            val module = match.groupValues[2].trim()
            alias to module
        }
    }
}
