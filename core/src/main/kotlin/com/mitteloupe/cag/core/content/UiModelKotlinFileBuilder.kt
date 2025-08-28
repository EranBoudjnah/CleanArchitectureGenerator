package com.mitteloupe.cag.core.content

const val UI_MODEL_NAME = "StubUiModel"

fun buildUiModelKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.ui.model

data class $UI_MODEL_NAME(
    val id: String
)
"""
