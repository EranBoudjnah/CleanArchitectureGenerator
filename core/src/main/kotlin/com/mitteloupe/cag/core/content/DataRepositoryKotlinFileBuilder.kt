package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.format.optimizeImports

fun buildDataRepositoryKotlinFile(
    featurePackageName: String,
    featureName: String,
    repositoryName: String = "PerformActionRepository"
): String =
    """package $featurePackageName.data.repository

${
        """
import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
import $featurePackageName.domain.repository.$repositoryName
""".optimizeImports()
    }
class ${featureName.capitalized}Repository(
    /* Include data sources and data <-> domain mappers */
) : $repositoryName {
    override fun perform(input: $DOMAIN_MODEL_NAME): $DOMAIN_MODEL_NAME = input
}
"""
