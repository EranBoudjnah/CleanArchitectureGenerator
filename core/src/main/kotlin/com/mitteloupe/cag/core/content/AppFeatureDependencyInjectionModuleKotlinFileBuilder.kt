package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.format.optimizeImports
import com.mitteloupe.cag.core.option.DependencyInjection

fun buildAppFeatureDependencyInjectionModuleKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    featureName: String,
    dependencyInjection: DependencyInjection
): String {
    val className = featureName.capitalized
    val variableName = className.replaceFirstChar { it.lowercase() }
    val providedImports = """import $featurePackageName.data.repository.${className}Repository
import $featurePackageName.domain.repository.PerformActionRepository
import $featurePackageName.domain.usecase.PerformActionUseCase
import $featurePackageName.presentation.mapper.StubDomainMapper
import $featurePackageName.presentation.mapper.StubPresentationMapper
import $featurePackageName.presentation.viewmodel.${className}ViewModel
import $featurePackageName.ui.di.${className}Dependencies
import $featurePackageName.ui.mapper.StubUiMapper
"""
    return when (dependencyInjection) {
        DependencyInjection.Hilt -> {
            """package $projectNamespace.di

${
                """import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import $projectNamespace.architecture.domain.UseCaseExecutor
import $projectNamespace.architecture.presentation.notification.PresentationNotification
import $projectNamespace.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import $projectNamespace.architecture.ui.notification.mapper.NotificationUiMapper
import $featurePackageName.presentation.navigation.${className}PresentationNavigationEvent
$providedImports""".optimizeImports()
            }
@Module
@InstallIn(SingletonComponent::class)
internal object ${className}Module {
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
        DependencyInjection.Koin -> {
            """package $projectNamespace.di

${
                """import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
$providedImports""".optimizeImports()
            }
val ${className.first().lowercase() + className.substring(1)}Module = module {
    factory { StubDomainMapper() }
    factory {  StubPresentationMapper() }
    factory<PerformActionRepository> { ${className}Repository() }
    factoryOf(::PerformActionUseCase)
    factoryOf(::${className}ViewModel)
    factory { StubUiMapper() }
    factoryOf(::${className}Dependencies)
}
"""
        }
        DependencyInjection.None -> error("Unexpected dependency injection option: $dependencyInjection")
    }
}
