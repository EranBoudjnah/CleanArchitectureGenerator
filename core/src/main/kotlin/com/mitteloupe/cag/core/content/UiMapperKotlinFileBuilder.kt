package com.mitteloupe.cag.core.content

fun buildPresentationToUiMapperKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.ui.mapper

import $featurePackageName.presentation.model.$PRESENTATION_MODEL_NAME
import $featurePackageName.ui.model.$UI_MODEL_NAME

class StubUiMapper {
    fun toUi(stub: $PRESENTATION_MODEL_NAME): $UI_MODEL_NAME =
        $UI_MODEL_NAME(id = stub.id)
}
"""
