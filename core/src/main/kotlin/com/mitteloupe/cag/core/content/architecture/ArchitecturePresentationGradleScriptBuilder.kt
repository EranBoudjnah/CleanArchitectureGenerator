package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitecturePresentationGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

dependencies {
    implementation(projects.architecture.domain)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit4)
}
"""
}
