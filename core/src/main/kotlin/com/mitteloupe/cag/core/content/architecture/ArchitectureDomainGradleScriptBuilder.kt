package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitectureDomainGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)
}

dependencies {
    implementation(projects.coroutine)
    implementation(libs.kotlinx.coroutines.core)
}
"""
}
