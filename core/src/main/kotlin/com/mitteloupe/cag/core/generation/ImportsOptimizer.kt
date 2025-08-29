package com.mitteloupe.cag.core.generation

fun String.optimizeImports(): String =
    lineSequence()
        .map { it.trim() }
        .filter { it.startsWith("import ") }
        .distinct()
        .sorted()
        .joinToString(separator = "\n", postfix = "\n")
