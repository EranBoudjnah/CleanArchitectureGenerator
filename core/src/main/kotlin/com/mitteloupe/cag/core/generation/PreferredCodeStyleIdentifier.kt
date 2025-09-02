package com.mitteloupe.cag.core.generation

private const val CURSOR_RETURN_LINE_FEED = "\r\n"
private const val DEFAULT_INDENTATION = "    "

fun String.preferredIndentation(): String =
    lineSequence().firstNotNullOfOrNull { line ->
        if (line.isBlank()) {
            null
        } else {
            line
                .takeWhile { it == ' ' || it == '\t' }
                .ifEmpty { null }
        }
    } ?: DEFAULT_INDENTATION

fun String.preferredEndOfLine(): String {
    return if (this.contains(CURSOR_RETURN_LINE_FEED)) {
        CURSOR_RETURN_LINE_FEED
    } else {
        "\n"
    }
}
