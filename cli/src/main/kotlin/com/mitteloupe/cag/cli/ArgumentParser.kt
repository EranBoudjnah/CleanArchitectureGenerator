package com.mitteloupe.cag.cli

class ArgumentParser {
    fun parseFeatureNames(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val features = mutableListOf<String>()
        args.forEach { arg ->
            when {
                arg.startsWith("--new-feature=") -> {
                    val value = arg.substringAfter("--new-feature=").trim()
                    if (value.isNotEmpty()) features.add(value)
                }
                arg.startsWith("-nf=") -> {
                    val value = arg.substringAfter("-nf=").trim()
                    if (value.isNotEmpty()) features.add(value)
                }
            }
        }
        return features
    }

    fun parseDataSourceNames(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val dataSources = mutableListOf<String>()
        args.forEach { arg ->
            when {
                arg.startsWith("--new-datasource=") -> {
                    val value = arg.substringAfter("--new-datasource=").trim()
                    if (value.isNotEmpty()) dataSources.add(value)
                }
                arg.startsWith("-nds=") -> {
                    val value = arg.substringAfter("-nds=").trim()
                    if (value.isNotEmpty()) dataSources.add(value)
                }
            }
        }
        return dataSources.map(::ensureDataSourceSuffix)
    }

    private fun ensureDataSourceSuffix(name: String): String {
        val trimmed = name.trim()
        return if (trimmed.endsWith("DataSource")) trimmed else "${trimmed}DataSource"
    }
}
