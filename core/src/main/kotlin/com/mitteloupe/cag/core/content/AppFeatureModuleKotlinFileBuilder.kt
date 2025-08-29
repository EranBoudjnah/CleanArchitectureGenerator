package com.mitteloupe.cag.core.content

fun buildAppFeatureModuleKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    featureName: String
): String {
    val className = featureName.capitalized
    val appPackage = projectNamespace.trimEnd('.')
    return """package $appPackage.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import $featurePackageName.data.repository.${className}Repository
import $featurePackageName.domain.repository.PerformExampleRepository
import $featurePackageName.domain.usecase.PerformActionUseCase
import $featurePackageName.presentation.mapper.StubDomainMapper
import $featurePackageName.presentation.mapper.StubPresentationMapper
import $featurePackageName.presentation.viewmodel.${className}ViewModel
import ${projectNamespace}architecture.domain.UseCaseExecutor

@Module
@InstallIn(SingletonComponent::class)
object ${className}Module {
    @Provides
    fun providesStubDomainMapper(): StubDomainMapper = StubDomainMapper()

    @Provides
    fun providesStubPresentationMapper(): StubPresentationMapper = StubPresentationMapper()

    @Provides
    fun providesPerformExampleRepository(): PerformExampleRepository = ${className}Repository()

    @Provides
    fun providesPerformActionUseCase(
        repository: PerformExampleRepository
    ): PerformActionUseCase = PerformActionUseCase(repository)

    @Provides
    fun provides${className}ViewModel(
        useCaseExecutor: UseCaseExecutor,
        performActionUseCase: PerformActionUseCase,
        stubDomainMapper: StubDomainMapper,
        stubPresentationMapper: StubPresentationMapper
    ): ${className}ViewModel = ${className}ViewModel(
        performActionUseCase = performActionUseCase,
        stubDomainMapper = stubDomainMapper,
        stubPresentationMapper = stubPresentationMapper,
        useCaseExecutor = useCaseExecutor
    )
}
"""
}
