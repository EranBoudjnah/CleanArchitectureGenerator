package com.mitteloupe.cag.cli.configuration

data class GitConfiguration(
    val autoInitialize: Boolean = false,
    val autoStage: Boolean = false
)

data class ClientConfiguration(
    val newProjectVersions: Map<String, String> = emptyMap(),
    val existingProjectVersions: Map<String, String> = emptyMap(),
    val git: GitConfiguration = GitConfiguration()
) {
    companion object {
        val EMPTY = ClientConfiguration()
    }
}
