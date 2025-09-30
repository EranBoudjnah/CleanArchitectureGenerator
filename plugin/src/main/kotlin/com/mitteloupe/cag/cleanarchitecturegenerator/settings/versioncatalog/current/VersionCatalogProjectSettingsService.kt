package com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog.current

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog.VersionCatalogSettingsService

@State(
    name = "CagVersionCatalogSettings",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
@Service(Service.Level.PROJECT)
class VersionCatalogProjectSettingsService : VersionCatalogSettingsService() {
    companion object {
        fun getInstance(project: Project): VersionCatalogProjectSettingsService = project.service()
    }
}
