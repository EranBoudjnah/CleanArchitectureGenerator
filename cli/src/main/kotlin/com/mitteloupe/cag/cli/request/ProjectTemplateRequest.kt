package com.mitteloupe.cag.cli.request

import com.mitteloupe.cag.core.option.DependencyInjection

data class ProjectTemplateRequest(
    val projectName: String,
    val packageName: String,
    val dependencyInjection: DependencyInjection?,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val enableKtor: Boolean,
    val enableRetrofit: Boolean,
    val enableGit: Boolean
)
