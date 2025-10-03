package com.mitteloupe.cag.cli.request

data class FeatureRequest(
    val featureName: String,
    val packageName: String?,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val enableGit: Boolean = false
)
