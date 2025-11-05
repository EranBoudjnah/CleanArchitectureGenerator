package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.content.gradle.GradleFileExtender
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitecturePresentationGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasKotlinJvm = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM).asAccessor
    val libraryAliasCoroutinesCore = catalog.getResolvedLibraryAliasForModule(LibraryConstants.KOTLINX_COROUTINES_CORE).asAccessor

    val gradleFileExtender = GradleFileExtender()
    val ktlintPluginLine = gradleFileExtender.buildKtlintPluginLine(catalog)
    val detektPluginLine = gradleFileExtender.buildDetektPluginLine(catalog)
    val ktlintConfiguration = gradleFileExtender.buildKtlintConfiguration(catalog)
    val detektConfiguration = gradleFileExtender.buildDetektConfiguration(catalog)
    val aliasTestJunit = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_JUNIT).asAccessor

    val configurations = "$ktlintConfiguration$detektConfiguration".trimIndent()
    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)$ktlintPluginLine$detektPluginLine
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}
${ if (configurations.isEmpty()) {
        ""
    } else {
        "\n$configurations\n"
    }
    }
dependencies {
    implementation(projects.architecture.domain)
    implementation(libs.$libraryAliasCoroutinesCore)
    testImplementation(libs.$aliasTestJunit)
}
"""
}
