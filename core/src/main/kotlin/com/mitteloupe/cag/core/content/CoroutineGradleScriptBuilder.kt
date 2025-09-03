package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildCoroutineGradleScript(catalog: VersionCatalogReader): String {
    val aliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    val aliasKotlinxCoroutinesCore =
        (catalog.getResolvedLibraryAliasForModule("org.jetbrains.kotlinx:kotlinx-coroutines-core") ?: "kotlinx-coroutines-core").asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$aliasKotlinJvm)
}

dependencies {
    implementation(libs.$aliasKotlinxCoroutinesCore)
}
"""
}
