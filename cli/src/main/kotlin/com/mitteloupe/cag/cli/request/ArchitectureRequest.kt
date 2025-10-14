package com.mitteloupe.cag.cli.request

data class ArchitectureRequest(
    val enableHilt: Boolean,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val enableGit: Boolean
)
