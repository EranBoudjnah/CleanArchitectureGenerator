package com.mitteloupe.cag.core.content

fun buildDomainRepositoryKotlinFile(
    featurePackageName: String,
    repositoryName: String
): String =
    """package $featurePackageName.domain.repository

import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME

interface $repositoryName {
    fun perform(input: $DOMAIN_MODEL_NAME): $DOMAIN_MODEL_NAME
}
"""
