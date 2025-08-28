package com.mitteloupe.cag.core.content

const val DOMAIN_MODEL_NAME = "StubDomainModel"

fun buildDomainModelKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.domain.model

data class $DOMAIN_MODEL_NAME(
    val id: String
)
"""
