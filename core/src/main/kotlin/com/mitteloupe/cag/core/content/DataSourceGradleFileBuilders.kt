package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildDataSourceSourceGradleScript(catalog: VersionCatalogReader): String {
    val aliasKotlinJvm = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM).asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$aliasKotlinJvm)
}

dependencies {
}
"""
}

fun buildDataSourceImplementationGradleScript(
    catalog: VersionCatalogReader,
    useKtor: Boolean,
    useRetrofit: Boolean
): String {
    val aliasKotlinJvm = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM).asAccessor

    val ktorDependencies =
        if (useKtor) {
            val aliasKtorClientCore = catalog.getResolvedLibraryAliasForModule(LibraryConstants.KTOR_CLIENT_CORE).asAccessor
            val aliasKtorClientOkhttp = catalog.getResolvedLibraryAliasForModule(LibraryConstants.KTOR_CLIENT_OKHTTP).asAccessor
            """
            implementation(libs.$aliasKtorClientCore)
            implementation(libs.$aliasKtorClientOkhttp)
            """.trimIndent()
        } else {
            ""
        }

    val retrofitDependencies =
        if (useRetrofit) {
            val aliasRetrofit = catalog.getResolvedLibraryAliasForModule(LibraryConstants.RETROFIT).asAccessor
            val aliasOkhttp3LoggingInterceptor =
                catalog
                    .getResolvedLibraryAliasForModule(
                        LibraryConstants.OKHTTP3_LOGGING_INTERCEPTOR
                    ).asAccessor
            """
            implementation(libs.$aliasRetrofit)
            implementation(libs.$aliasOkhttp3LoggingInterceptor)
            """.trimIndent()
        } else {
            ""
        }

    val optionalDependencies =
        listOf(ktorDependencies, retrofitDependencies)
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n")

    val dependenciesBlockBody =
        buildString {
            appendLine("implementation(projects.datasource.source)")
            if (optionalDependencies.isNotEmpty()) {
                append(optionalDependencies)
            }
        }.trimEnd()

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$aliasKotlinJvm)
}

dependencies {
    $dependenciesBlockBody
}
"""
}
