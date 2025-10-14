package com.mitteloupe.cag.core.request

import java.io.File

data class GenerateProjectTemplateRequest(
    val destinationRootDirectory: File,
    val projectName: String,
    val packageName: String,
    val overrideMinimumAndroidSdk: Int?,
    val overrideAndroidGradlePluginVersion: String?,
    val enableHilt: Boolean,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val enableKtor: Boolean,
    val enableRetrofit: Boolean
)
