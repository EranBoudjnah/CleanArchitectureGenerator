package com.mitteloupe.cag.core.generation.versioncatalog

object VersionCatalogSettingsAccessor {
    @Volatile
    private var provider: ((key: String, default: String) -> String) = { _, default -> default }

    fun setProvider(provider: (key: String, default: String) -> String) {
        this.provider = provider
    }

    fun getVersionForKey(
        key: String,
        default: String
    ): String = provider(key, default)
}
