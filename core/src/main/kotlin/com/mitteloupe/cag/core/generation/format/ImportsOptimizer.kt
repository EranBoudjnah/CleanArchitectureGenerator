package com.mitteloupe.cag.core.generation.format

fun String.optimizeImports(): String =
    lineSequence()
        .map { it.trim() }
        .filter { it.startsWith("import ") }
        .map { it.replace(Regex("import\\s+"), "import ") }
        .distinct()
        .sorted()
        .joinToString(separator = "\n", postfix = "\n")
