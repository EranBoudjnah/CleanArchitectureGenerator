package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildDataSourceSourceGradleScript(catalog: VersionCatalogReader): String {
    val aliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

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
    val aliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    val ktorDependencies =
        if (useKtor) {
            """
            implementation("io.ktor:ktor-client-core:3.0.3")
            implementation("io.ktor:ktor-client-okhttp:3.0.3")
            """.trimIndent()
        } else {
            ""
        }

    val retrofitDependencies =
        if (useRetrofit) {
            """
            implementation("com.squareup.retrofit2:retrofit:2.11.0")
            implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
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
