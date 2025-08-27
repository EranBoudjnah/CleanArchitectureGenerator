package com.mitteloupe.cag.core.content

fun buildDomainUseCaseKotlinFile(
    projectNamespace: String,
    featurePackageName: String
): String =
    """package $featurePackageName.domain.usecase

import ${projectNamespace}architecture.domain.usecase.UseCase
import $featurePackageName.domain.repository.PerformExampleRepository

class PerformActionUseCase(
    private val performExampleRepository: PerformExampleRepository
) : UseCase<Unit, Unit>(
    coroutineContextProvider
) {
    override fun execute(input: Unit, onResult: (Unit) -> Unit) {
        onResult(performExampleRepository.perform(input))
    }
}
"""
