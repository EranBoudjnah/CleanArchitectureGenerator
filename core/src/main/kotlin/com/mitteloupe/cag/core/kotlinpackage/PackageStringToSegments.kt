package com.mitteloupe.cag.core.kotlinpackage

fun String.toSegments() = split('.').filter { it.isNotBlank() }
