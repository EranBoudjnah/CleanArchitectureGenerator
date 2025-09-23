package com.mitteloupe.cag.core.content.gradle

import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

class GradleFileExtender internal constructor() {
    fun buildComposePluginLine(catalog: VersionCatalogReader): String =
        if (catalog.isPluginAvailable(PluginConstants.COMPOSE_COMPILER)) {
            val aliasComposeCompiler = catalog.getResolvedPluginAliasFor(PluginConstants.COMPOSE_COMPILER).asAccessor
            "\n    alias(libs.plugins.$aliasComposeCompiler)"
        } else {
            ""
        }

    fun buildKtlintPluginLine(
        catalog: VersionCatalogReader,
        indentation: Int = 1
    ): String =
        if (catalog.isPluginAvailable(PluginConstants.KTLINT)) {
            val aliasKtlintCompiler = catalog.getResolvedPluginAliasFor(PluginConstants.KTLINT).asAccessor
            "\n${"    ".repeat(indentation)}alias(libs.plugins.$aliasKtlintCompiler)"
        } else {
            ""
        }

    fun buildDetektPluginLine(
        catalog: VersionCatalogReader,
        indentation: Int = 1
    ): String =
        if (catalog.isPluginAvailable(PluginConstants.DETEKT)) {
            val aliasDetektCompiler = catalog.getResolvedPluginAliasFor(PluginConstants.DETEKT).asAccessor
            "\n${"    ".repeat(indentation)}alias(libs.plugins.$aliasDetektCompiler)"
        } else {
            ""
        }

    fun buildKtlintConfiguration(catalog: VersionCatalogReader): String =
        if (catalog.isPluginAvailable(PluginConstants.KTLINT)) {
            """
    ktlint {
        version.set("1.7.1")
        android.set(true)
    }
    """
        } else {
            ""
        }

    fun buildDetektConfiguration(
        catalog: VersionCatalogReader,
        configRelativePathFromModule: String = "../../detekt.yml"
    ): String =
        if (catalog.isPluginAvailable(PluginConstants.DETEKT)) {
            """
    detekt {
        config.setFrom("${'$'}projectDir/$configRelativePathFromModule")
    }
    """
        } else {
            ""
        }
}
