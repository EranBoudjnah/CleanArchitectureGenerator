package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.format.optimizeImports

fun buildDataSourceModuleKotlinFile(
    appPackageName: String,
    dataSourcePackageName: String,
    dataSourceName: String
): String =
    """package $appPackageName.di

${
        """
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import $dataSourcePackageName.$dataSourceName
import $dataSourcePackageName.${dataSourceName}Impl
""".optimizeImports()
    }
@Module
@InstallIn(SingletonComponent::class)
internal object ${dataSourceName}Module {
    @Provides
    fun provides$dataSourceName(): $dataSourceName = ${dataSourceName}Impl()
}
"""
