package com.mitteloupe.cag.cli.request

data class ArchitectureRequest(
    val enableCompose: Boolean = true,
    val enableKtlint: Boolean = false,
    val enableDetekt: Boolean = false
)
