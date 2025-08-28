package com.mitteloupe.cag.core.content

fun buildPresentationMapperKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.presentation.mapper

import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
import $featurePackageName.presentation.model.$PRESENTATION_MODEL_NAME

class StubDomainToPresentationMapper {
    fun toPresentation(stub: $DOMAIN_MODEL_NAME): $PRESENTATION_MODEL_NAME =
        $PRESENTATION_MODEL_NAME(id = stub.id)
}
"""
