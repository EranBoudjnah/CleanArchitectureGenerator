package com.mitteloupe.cag.cli.request

import com.mitteloupe.cag.core.option.DependencyInjection
import java.io.File

data class ArchitectureRequest(
    val appModuleDirectory: File?,
    val dependencyInjection: DependencyInjection?,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val enableGit: Boolean
)
