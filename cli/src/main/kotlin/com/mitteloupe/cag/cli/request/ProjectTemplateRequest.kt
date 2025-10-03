package com.mitteloupe.cag.cli.request

data class ProjectTemplateRequest(
    val projectName: String,
    val packageName: String,
    val enableCompose: Boolean = true,
    val enableKtlint: Boolean = false,
    val enableDetekt: Boolean = false,
    val enableKtor: Boolean = false,
    val enableRetrofit: Boolean = false,
    val enableGit: Boolean = false
)
