package com.mitteloupe.cag.cli.flag

data class SecondaryFlag(
    val long: String,
    val short: String,
    val isMandatory: Boolean = false,
    val missingErrorMessage: String = "",
    val isBoolean: Boolean = false
)
