package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.content.gradle.GradleFileExtender
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitecturePresentationTestGradleScript(
    catalog: VersionCatalogReader,
    enableKtlint: Boolean = false,
    enableDetekt: Boolean = false
): String {
    val pluginAliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    val aliasTestJunit = (catalog.getResolvedLibraryAliasForModule("junit:junit") ?: "test-junit").asAccessor
    val aliasTestMockitoKotlin =
        (catalog.getResolvedLibraryAliasForModule("org.mockito.kotlin:mockito-kotlin") ?: "test-mockito-kotlin").asAccessor
    val aliasTestKotlinxCoroutines =
        (catalog.getResolvedLibraryAliasForModule("org.jetbrains.kotlinx:kotlinx-coroutines-test") ?: "test-kotlinx-coroutines").asAccessor

    val gradleFileExtender = GradleFileExtender()
    val ktlintPluginLine = gradleFileExtender.buildKtlintPluginLine(enableKtlint)
    val detektPluginLine = gradleFileExtender.buildDetektPluginLine(enableDetekt)
    val ktlintConfiguration = gradleFileExtender.buildKtlintConfiguration(enableKtlint)
    val detektConfiguration = gradleFileExtender.buildDetektConfiguration(enableDetekt)

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)
$ktlintPluginLine$detektPluginLine}
$ktlintConfiguration$detektConfiguration
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
