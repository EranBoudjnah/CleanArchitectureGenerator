package com.mitteloupe.cag.cli

data class FeatureRequest(val featureName: String, val packageName: String?)

data class DataSourceRequest(
    val dataSourceName: String,
    val useKtor: Boolean,
    val useRetrofit: Boolean
)

class AppArgumentProcessor(private val argumentParser: ArgumentParser = ArgumentParser()) {
    fun isHelpRequested(arguments: Array<String>): Boolean = arguments.any { it == "--help" || it == "-h" }

    fun getNewFeatures(arguments: Array<String>): List<FeatureRequest> {
        val pairs =
            argumentParser.parsePrimaryWithSecondaries(
                arguments = arguments,
                primaryLong = "--new-feature",
                primaryShort = "-nf",
                secondaryFlags = listOf(SecondaryFlag(long = "--package", short = "-p"))
            )
        return pairs.map { (featureName, secondaries) ->
            FeatureRequest(
                featureName = featureName,
                packageName = secondaries["--package"]
            )
        }
    }

    fun getNewDataSources(arguments: Array<String>): List<DataSourceRequest> {
        val pairs =
            argumentParser.parsePrimaryWithSecondaries(
                arguments = arguments,
                primaryLong = "--new-datasource",
                primaryShort = "-nds",
                secondaryFlags = listOf(SecondaryFlag(long = "--with", short = "-w"))
            )
        return pairs.map { (rawName, secondaries) ->
            val name = ensureDataSourceSuffix(rawName)
            val libraries =
                (secondaries["--with"] ?: "").lowercase()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
            DataSourceRequest(
                dataSourceName = name,
                useKtor = libraries.contains("ktor"),
                useRetrofit = libraries.contains("retrofit")
            )
        }
    }

    private fun ensureDataSourceSuffix(name: String): String {
        val trimmedName = name.trim()
        return if (trimmedName.endsWith("DataSource")) {
            trimmedName
        } else {
            "${trimmedName}DataSource"
        }
    }
}
