package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.content.gradle.GradleFileExtender
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitecturePresentationTestGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasKotlinJvm = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM).asAccessor

    val aliasTestJunit = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_JUNIT).asAccessor
    val aliasTestKotlinxCoroutines =
        catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_KOTLINX_COROUTINES).asAccessor
    val aliasTestMockitoCore =
        catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_MOCKITO_CORE).asAccessor
    val aliasTestMockitoKotlin =
        catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_MOCKITO_KOTLIN).asAccessor
    val aliasTestMockitoAndroid =
        catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_MOCKITO_ANDROID).asAccessor

    val gradleFileExtender = GradleFileExtender()
    val ktlintPluginLine = gradleFileExtender.buildKtlintPluginLine(catalog)
    val detektPluginLine = gradleFileExtender.buildDetektPluginLine(catalog)
    val ktlintConfiguration = gradleFileExtender.buildKtlintConfiguration(catalog)
    val detektConfiguration = gradleFileExtender.buildDetektConfiguration(catalog)

    val configurations = "$ktlintConfiguration$detektConfiguration".trimIndent()
    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)$ktlintPluginLine$detektPluginLine
}
${ if (configurations.isEmpty()) {
        ""
    } else {
        "\n$configurations\n"
    }
    }
dependencies {
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.$aliasTestJunit)
    implementation(libs.$aliasTestKotlinxCoroutines)
    implementation(libs.$aliasTestMockitoCore)
    implementation(libs.$aliasTestMockitoKotlin)
    implementation(libs.$aliasTestMockitoAndroid)
    implementation(projects.coroutine)
}
"""
}
