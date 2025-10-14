package com.mitteloupe.cag.core.request

import com.mitteloupe.cag.core.option.DependencyInjection
import java.io.File

data class GenerateArchitectureRequest(
    val projectNamespace: String,
    val destinationRootDirectory: File,
    val appModuleDirectory: File?,
    val architecturePackageName: String,
    val dependencyInjection: DependencyInjection,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean
)
