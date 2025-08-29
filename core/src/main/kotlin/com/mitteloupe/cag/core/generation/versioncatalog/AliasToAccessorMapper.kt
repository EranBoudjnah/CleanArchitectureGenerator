package com.mitteloupe.cag.core.generation.versioncatalog

val String.asAccessor: String
    get() = replace('-', '.')
