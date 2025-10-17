package com.mitteloupe.cag.cli.request

import com.mitteloupe.cag.core.option.DependencyInjection

data class FeatureRequest(
    val featureName: String,
    val packageName: String?,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val enableGit: Boolean = false,
    val dependencyInjection: DependencyInjection?
)
