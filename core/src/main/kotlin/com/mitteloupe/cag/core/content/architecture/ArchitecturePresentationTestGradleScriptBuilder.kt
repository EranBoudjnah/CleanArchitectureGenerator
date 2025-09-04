package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitecturePresentationTestGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    val aliasTestJunit = (catalog.getResolvedLibraryAliasForModule("junit:junit") ?: "test-junit").asAccessor
    val aliasTestMockitoKotlin =
        (catalog.getResolvedLibraryAliasForModule("org.mockito.kotlin:mockito-kotlin") ?: "test-mockito-kotlin").asAccessor
    val aliasTestKotlinxCoroutines =
        (catalog.getResolvedLibraryAliasForModule("org.jetbrains.kotlinx:kotlinx-coroutines-test") ?: "test-kotlinx-coroutines").asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)
}

dependencies {
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.$aliasTestJunit)
    implementation(libs.$aliasTestMockitoKotlin)
    implementation(libs.$aliasTestKotlinxCoroutines)
    implementation(projects.coroutineTest)
}
"""
}
