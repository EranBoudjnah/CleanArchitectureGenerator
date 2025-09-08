package com.mitteloupe.cag.core.generation.versioncatalog

import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.LibraryRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.PluginRequirement

interface VersionCatalogReader {
    fun getResolvedPluginAliasFor(requirement: PluginRequirement): String

    fun isPluginAvailable(requirement: PluginRequirement): Boolean

    fun getResolvedLibraryAliasForModule(requirement: LibraryRequirement): String
}
