package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.format.optimizeImports

fun buildAppFeatureModuleKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    featureName: String
): String {
    val className = featureName.capitalized
    val variableName = className.replaceFirstChar { it.lowercase() }
    return """package $projectNamespace.di

${
        """
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import $featurePackageName.data.repository.${className}Repository
import $featurePackageName.domain.repository.PerformActionRepository
import $featurePackageName.domain.usecase.PerformActionUseCase
import $featurePackageName.presentation.mapper.StubDomainMapper
import $featurePackageName.presentation.mapper.StubPresentationMapper
import $featurePackageName.presentation.viewmodel.${className}ViewModel
import $featurePackageName.presentation.navigation.${className}PresentationNavigationEvent
import $featurePackageName.ui.di.${className}Dependencies
import $featurePackageName.ui.mapper.StubUiMapper
import $projectNamespace.architecture.domain.UseCaseExecutor
import $projectNamespace.architecture.presentation.notification.PresentationNotification
import $projectNamespace.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import $projectNamespace.architecture.ui.notification.mapper.NotificationUiMapper
""".optimizeImports()
    }
@Module
@InstallIn(SingletonComponent::class)
object ${className}Module {
    @Provides
    fun providesStubDomainMapper(): StubDomainMapper = StubDomainMapper()

    @Provides
    fun providesStubPresentationMapper(): StubPresentationMapper = StubPresentationMapper()

    @Provides
    fun providesPerformActionRepository(): PerformActionRepository = ${className}Repository()

    @Provides
    fun providesPerformActionUseCase(
        repository: PerformActionRepository
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

    @Provides
    fun provides${className}Dependencies(
        ${variableName}ViewModel: ${className}ViewModel,
        ${variableName}NavigationMapper: NavigationEventDestinationMapper<${className}PresentationNavigationEvent>,
        ${variableName}NotificationMapper: NotificationUiMapper<PresentationNotification>
    ): ${className}Dependencies = ${className}Dependencies(
        ${variableName}ViewModel = ${variableName}ViewModel,
        ${variableName}NavigationMapper = ${variableName}NavigationMapper,
        ${variableName}NotificationMapper = ${variableName}NotificationMapper,
        stubUiMapper = StubUiMapper()
    )
}
"""
}
