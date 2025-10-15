package com.mitteloupe.cag.cli.flag

data class SecondaryFlag(
    val option: FlagOption,
    val isMandatory: Boolean = false,
    val missingErrorMessage: String = "",
    val isBoolean: Boolean = false
) {
    val long = option.long
    val short = option.short

    fun value(longForm: Boolean) = if (longForm) long else short
}

data class FlagOption(
    val long: String,
    val short: String
)
