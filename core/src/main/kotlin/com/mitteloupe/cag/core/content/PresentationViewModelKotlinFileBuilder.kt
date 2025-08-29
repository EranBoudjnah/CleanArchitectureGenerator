package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.optimizeImports

fun buildPresentationViewModelKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    featureName: String
): String =
    """package $featurePackageName.presentation.viewmodel

${
        """
import ${projectNamespace}architecture.domain.UseCaseExecutor
import ${projectNamespace}architecture.presentation.notification.PresentationNotification
import ${projectNamespace}architecture.presentation.viewmodel.BaseViewModel
import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
import $featurePackageName.domain.usecase.PerformActionUseCase
import $featurePackageName.presentation.mapper.StubDomainMapper
import $featurePackageName.presentation.mapper.StubPresentationMapper
import $featurePackageName.presentation.model.${featureName}ViewState
import $featurePackageName.presentation.model.${featureName}ViewState.Idle
import $featurePackageName.presentation.model.${featureName}ViewState.Loading
import $featurePackageName.presentation.model.$PRESENTATION_MODEL_NAME
""".optimizeImports()
    }
class ${featureName}ViewModel(
    private val performActionUseCase: PerformActionUseCase,
    private val stubDomainMapper: StubDomainMapper,
    private val stubPresentationMapper: StubPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<${featureName}ViewState, PresentationNotification>(useCaseExecutor) {
    fun onEnter(stub: $PRESENTATION_MODEL_NAME) {
        updateViewState(Loading)
        performAction(stub)
    }

    private fun performAction(stub: $PRESENTATION_MODEL_NAME) {
        val domainStub = stubDomainMapper.toDomain(stub)
        performActionUseCase(
            value = domainStub,
            onResult = { result ->
                presentStub(result)
            },
            onException = { exception ->
                presentError()
            }
        )
    }

    private fun presentStub(stub: $DOMAIN_MODEL_NAME) {
        val presentationStub = stubPresentationMapper.toPresentation(stub)
        updateViewState(Idle(presentationStub))
    }

    private fun presentError() {
        updateViewState(${featureName}ViewState.Error)
    }
}
"""
