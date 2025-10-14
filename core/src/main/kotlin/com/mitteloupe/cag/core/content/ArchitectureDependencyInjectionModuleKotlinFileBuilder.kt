package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.format.optimizeImports
import com.mitteloupe.cag.core.option.DependencyInjection

fun buildArchitectureDependencyInjectionModuleKotlinFile(
    projectNamespace: String,
    dependencyInjection: DependencyInjection
): String {
    val commonImports =
        """import $projectNamespace.architecture.domain.UseCaseExecutor
"""
    return when (dependencyInjection) {
        DependencyInjection.Hilt -> {
            """package $projectNamespace.di

${
                """
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
$commonImports
""".optimizeImports()
            }
@Module
@InstallIn(SingletonComponent::class)
object ArchitectureModule {
    @Provides
    fun providesUseCaseExecutor(): UseCaseExecutor = UseCaseExecutor()
}
"""
        }
        DependencyInjection.Koin -> {
            """package $projectNamespace.di

${
                """import org.koin.dsl.module
$commonImports
""".optimizeImports()
            }
val architectureModule = module {
    single { UseCaseExecutor() }
}
"""
        }
        DependencyInjection.None -> error("Unexpected dependency injection option: $dependencyInjection")
    }
}
