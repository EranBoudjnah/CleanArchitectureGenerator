package com.mitteloupe.cag.cli.request

data class ViewModelRequest(
    val viewModelName: String,
    val targetPath: String?
)
