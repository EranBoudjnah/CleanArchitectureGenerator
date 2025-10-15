package com.mitteloupe.cag.cli.request

data class UseCaseRequest(
    val useCaseName: String,
    val targetPath: String?,
    val inputDataType: String?,
    val outputDataType: String?,
    val enableGit: Boolean
)
