package com.mitteloupe.cag.core.content

fun buildDomainToPresentationMapperKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.presentation.mapper

import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
import $featurePackageName.presentation.model.$PRESENTATION_MODEL_NAME

class StubPresentationMapper {
    fun toPresentation(stub: $DOMAIN_MODEL_NAME): $PRESENTATION_MODEL_NAME =
        $PRESENTATION_MODEL_NAME(id = stub.id)
}
"""

fun buildPresentationToDomainMapperKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.presentation.mapper

import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
import $featurePackageName.presentation.model.$PRESENTATION_MODEL_NAME

class StubDomainMapper {
    fun toDomain(stub: $PRESENTATION_MODEL_NAME): $DOMAIN_MODEL_NAME =
        $DOMAIN_MODEL_NAME(id = stub.id)
}
"""
