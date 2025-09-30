package com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog

import com.intellij.openapi.components.PersistentStateComponent
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants

class VersionCatalogState {
    val values: MutableMap<String, String> = linkedMapOf()
}

abstract class VersionCatalogSettingsService : PersistentStateComponent<VersionCatalogState> {
    private var state: VersionCatalogState = VersionCatalogState()

    override fun getState(): VersionCatalogState = state

    override fun loadState(state: VersionCatalogState) {
        this.state = state
    }

    fun initialize() {
        initializeValuesIfEmpty()
    }

    fun getCurrentValues(): Map<String, String> {
        initializeValuesIfEmpty()
        return state.values
    }

    private fun initializeValuesIfEmpty() {
        if (state.values.isEmpty()) {
            replaceAll(defaultValues())
        }
    }

    fun replaceAll(newValues: Map<String, String>) {
        state.values.clear()
        state.values.putAll(newValues)
    }

    protected open fun defaultValues(): Map<String, String> =
        buildMap {
            LibraryConstants.ALL_LIBRARIES
                .mapNotNull { it.version }
                .forEach { version -> put(version.key, version.version) }

            PluginConstants.ALL_PLUGINS
                .forEach { plugin -> put(plugin.version.key, plugin.version.version) }
        }
}
