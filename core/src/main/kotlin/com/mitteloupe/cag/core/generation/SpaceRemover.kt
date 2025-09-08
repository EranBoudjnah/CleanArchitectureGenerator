package com.mitteloupe.cag.core.generation

private val spaceRegex = "\\s".toRegex()

fun String.withoutSpaces() = trim().replace(spaceRegex, "")
