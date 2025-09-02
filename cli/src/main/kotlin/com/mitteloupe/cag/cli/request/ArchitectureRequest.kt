package com.mitteloupe.cag.cli.request

data class ArchitectureRequest(
    val packageName: String?,
    val enableCompose: Boolean = true
)
