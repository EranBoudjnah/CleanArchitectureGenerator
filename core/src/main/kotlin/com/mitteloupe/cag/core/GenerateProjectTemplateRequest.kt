package com.mitteloupe.cag.core

import java.io.File

data class GenerateProjectTemplateRequest(
    val destinationRootDirectory: File,
    val projectName: String,
    val packageName: String,
    val enableCompose: Boolean = true,
    val enableKtlint: Boolean = false,
    val enableDetekt: Boolean = false,
    val enableKtor: Boolean = false,
    val enableRetrofit: Boolean = false
)
