package com.mitteloupe.cag.cli.configuration

data class ClientConfiguration(
    val newProjectVersions: Map<String, String> = emptyMap(),
    val existingProjectVersions: Map<String, String> = emptyMap()
) {
    companion object {
        val EMPTY = ClientConfiguration()
    }
}
