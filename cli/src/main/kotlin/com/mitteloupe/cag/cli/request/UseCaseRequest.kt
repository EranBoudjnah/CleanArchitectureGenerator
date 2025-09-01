package com.mitteloupe.cag.cli.request

data class UseCaseRequest(
    val useCaseName: String,
    val targetPath: String?
)
