package com.mitteloupe.cag.core.content

fun buildPresentationViewStateKotlinFile(
    featurePackageName: String,
    featureName: String
): String =
    """package $featurePackageName.presentation.model

sealed interface ${featureName}ViewState {
    data object Loading : ${featureName}ViewState

    data class Idle(val value: $PRESENTATION_MODEL_NAME) : ${featureName}ViewState

    data object Error : ${featureName}ViewState
}
"""
