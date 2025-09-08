package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildCoroutineGradleScript(catalog: VersionCatalogReader): String {
    val aliasKotlinJvm = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM).asAccessor

    val aliasKotlinxCoroutinesCore = catalog.getResolvedLibraryAliasForModule(LibraryConstants.KOTLINX_COROUTINES_CORE).asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$aliasKotlinJvm)
}

dependencies {
    implementation(libs.$aliasKotlinxCoroutinesCore)
}
"""
}
