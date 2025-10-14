package com.mitteloupe.cag.core.request

import java.io.File

data class GenerateArchitectureRequest(
    val destinationRootDirectory: File,
    val architecturePackageName: String,
    val enableHilt: Boolean,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean
)
