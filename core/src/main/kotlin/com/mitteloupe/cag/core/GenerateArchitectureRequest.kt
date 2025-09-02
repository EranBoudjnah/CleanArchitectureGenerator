package com.mitteloupe.cag.core

import java.io.File

data class GenerateArchitectureRequest(
    val destinationRootDirectory: File,
    val architecturePackageName: String,
    val enableCompose: Boolean = true
)
