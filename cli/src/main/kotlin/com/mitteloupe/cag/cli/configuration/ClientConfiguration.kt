package com.mitteloupe.cag.cli.configuration

import com.mitteloupe.cag.cli.configuration.model.DependencyInjection

data class ClientConfiguration(
    val newProjectVersions: Map<String, String> = emptyMap(),
    val existingProjectVersions: Map<String, String> = emptyMap(),
    val git: GitConfiguration = GitConfiguration(),
    val dependencyInjection: DependencyInjectionConfiguration = DependencyInjectionConfiguration()
) {
    companion object {
        val EMPTY = ClientConfiguration()
    }
}

data class GitConfiguration(
    val autoInitialize: Boolean? = null,
    val autoStage: Boolean? = null,
    val path: String? = null
)

data class DependencyInjectionConfiguration(
    val library: DependencyInjection? = null
)
