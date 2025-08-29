package com.mitteloupe.cag.core.generation.versioncatalog

interface VersionCatalogReader {
    fun getResolvedPluginAliasFor(pluginId: String): String?

    fun getResolvedLibraryAliasForModule(module: String): String?
}
