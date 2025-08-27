package com.mitteloupe.cag.core.syntax

fun String.toSegments() = split('.').filter { it.isNotBlank() }
