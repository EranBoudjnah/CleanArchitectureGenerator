package com.mitteloupe.cag.settings.versioncatalog.template

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.mitteloupe.cag.settings.versioncatalog.VersionCatalogSettingsService

@State(
    name = "CagVersionCatalogAppSettings",
    storages = [Storage("cagVersionCatalogAppSettings.xml")]
)
@Service(Service.Level.APP)
class VersionCatalogAppSettingsService : VersionCatalogSettingsService() {
    companion object {
        fun getInstance(): VersionCatalogAppSettingsService = service()
    }
}
