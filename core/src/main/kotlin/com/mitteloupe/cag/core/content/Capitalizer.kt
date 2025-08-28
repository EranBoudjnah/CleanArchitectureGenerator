package com.mitteloupe.cag.core.content

val String.capitalized: String
    get() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
