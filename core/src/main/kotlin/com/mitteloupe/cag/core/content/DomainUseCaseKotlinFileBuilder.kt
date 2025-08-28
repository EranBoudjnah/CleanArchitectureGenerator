package com.mitteloupe.cag.core.content

fun buildDomainUseCaseKotlinFile(
    projectNamespace: String,
    featurePackageName: String
): String =
    """package $featurePackageName.domain.usecase

import ${projectNamespace}architecture.domain.usecase.UseCase
import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
import $featurePackageName.domain.repository.PerformExampleRepository

class PerformActionUseCase(
    private val performExampleRepository: PerformExampleRepository
) : UseCase<$DOMAIN_MODEL_NAME, $DOMAIN_MODEL_NAME> {
    override fun execute(input: $DOMAIN_MODEL_NAME, onResult: ($DOMAIN_MODEL_NAME) -> Unit) {
        onResult(performExampleRepository.perform(input))
    }
}
"""
