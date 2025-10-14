package com.mitteloupe.cag.core.request

import com.mitteloupe.cag.core.option.DependencyInjection
import java.io.File

data class GenerateArchitectureRequest(
    val destinationRootDirectory: File,
    val architecturePackageName: String,
    val dependencyInjection: DependencyInjection,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean
)
