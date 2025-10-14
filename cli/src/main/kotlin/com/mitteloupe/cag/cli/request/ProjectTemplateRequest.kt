package com.mitteloupe.cag.cli.request

data class ProjectTemplateRequest(
    val projectName: String,
    val packageName: String,
    val enableHilt: Boolean,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val enableKtor: Boolean,
    val enableRetrofit: Boolean,
    val enableGit: Boolean
)
