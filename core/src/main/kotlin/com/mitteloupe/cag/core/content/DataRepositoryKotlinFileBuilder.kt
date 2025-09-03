package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.optimizeImports

fun buildDataRepositoryKotlinFile(
    featurePackageName: String,
    featureName: String
): String =
    """package $featurePackageName.data.repository

${
        """
import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
import $featurePackageName.domain.repository.PerformExampleRepository
""".optimizeImports()
    }
class ${featureName.capitalized}Repository(
    /* Include data sources and data <-> domain mappers */
) : PerformExampleRepository {
    override fun perform(input: $DOMAIN_MODEL_NAME): $DOMAIN_MODEL_NAME = input
}
"""
