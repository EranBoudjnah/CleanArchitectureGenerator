package com.mitteloupe.cag.cli.request

import com.mitteloupe.cag.core.option.DependencyInjection

data class ArchitectureRequest(
    val dependencyInjection: DependencyInjection,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val enableGit: Boolean
)
