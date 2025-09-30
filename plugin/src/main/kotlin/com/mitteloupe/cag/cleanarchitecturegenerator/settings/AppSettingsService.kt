package com.mitteloupe.cag.cleanarchitecturegenerator.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@State(
    name = "CagAppSettings",
    storages = [Storage("cagAppSettings.xml")]
)
@Service(Service.Level.APP)
class AppSettingsService : PersistentStateComponent<AppSettingsService.State> {
    class State {
        var autoAddGeneratedFilesToGit: Boolean = false
    }

    private var state: State = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var autoAddGeneratedFilesToGit: Boolean
        get() = state.autoAddGeneratedFilesToGit
        set(value) {
            state.autoAddGeneratedFilesToGit = value
        }

    companion object {
        fun getInstance(): AppSettingsService = service()
    }
}
