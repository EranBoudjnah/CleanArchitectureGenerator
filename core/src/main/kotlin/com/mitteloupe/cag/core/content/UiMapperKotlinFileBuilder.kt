package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.format.optimizeImports

fun buildPresentationToUiMapperKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.ui.mapper

${
        """
import $featurePackageName.presentation.model.$PRESENTATION_MODEL_NAME
import $featurePackageName.ui.model.$UI_MODEL_NAME
""".optimizeImports()
    }
class StubUiMapper {
    fun toUi(stub: $PRESENTATION_MODEL_NAME): $UI_MODEL_NAME =
        $UI_MODEL_NAME(id = stub.id)
}
"""
