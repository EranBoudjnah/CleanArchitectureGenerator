package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildDataGradleScript(
    featureNameLowerCase: String,
    catalog: VersionCatalogReader
): String {
    val aliasKotlinJvm = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM).asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$aliasKotlinJvm)
}

dependencies {
    implementation(projects.features.$featureNameLowerCase.domain)
    implementation(projects.architecture.domain)

    implementation(projects.datasource.source)
}
"""
}
