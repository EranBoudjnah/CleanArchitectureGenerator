package com.mitteloupe.cag.core.content

const val PRESENTATION_MODEL_NAME = "StubPresentationModel"

fun buildPresentationModelKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.presentation.model

data class $PRESENTATION_MODEL_NAME(
    val id: String
)
"""
