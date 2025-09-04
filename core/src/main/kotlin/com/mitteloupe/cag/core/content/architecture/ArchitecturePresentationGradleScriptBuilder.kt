package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.content.gradle.GradleFileExtender
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitecturePresentationGradleScript(
    catalog: VersionCatalogReader,
    enableKtlint: Boolean = false,
    enableDetekt: Boolean = false
): String {
    val pluginAliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    val gradleFileExtender = GradleFileExtender()
    val ktlintPluginLine = gradleFileExtender.buildKtlintPluginLine(enableKtlint)
    val detektPluginLine = gradleFileExtender.buildDetektPluginLine(enableDetekt)
    val ktlintConfiguration = gradleFileExtender.buildKtlintConfiguration(enableKtlint)
    val detektConfiguration = gradleFileExtender.buildDetektConfiguration(enableDetekt)

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)
$ktlintPluginLine$detektPluginLine}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}
$ktlintConfiguration$detektConfiguration
dependencies {
    implementation(projects.architecture.domain)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit4)
}
"""
}
