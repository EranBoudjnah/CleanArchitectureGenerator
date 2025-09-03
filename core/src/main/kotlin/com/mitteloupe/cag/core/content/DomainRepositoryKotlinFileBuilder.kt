package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.optimizeImports

fun buildDomainRepositoryKotlinFile(
    featurePackageName: String,
    repositoryName: String
): String =
    """package $featurePackageName.domain.repository

${
        """
import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
""".optimizeImports()
    }
interface $repositoryName {
    fun perform(input: $DOMAIN_MODEL_NAME): $DOMAIN_MODEL_NAME
}
"""
