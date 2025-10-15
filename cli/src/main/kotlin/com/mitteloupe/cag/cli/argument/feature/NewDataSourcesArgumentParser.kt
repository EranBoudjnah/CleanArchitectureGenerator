package com.mitteloupe.cag.cli.argument.feature

import com.mitteloupe.cag.cli.argument.ArgumentParser
import com.mitteloupe.cag.cli.argument.parseWithNameFlag
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewDataSourcePrimary
import com.mitteloupe.cag.cli.flag.SecondaryFlagOptions
import com.mitteloupe.cag.cli.request.DataSourceRequest

fun ArgumentParser.parseNewDataSourcesArgument(arguments: Array<String>): List<DataSourceRequest> =
    parseWithNameFlag(arguments = arguments, primaryFlag = NewDataSourcePrimary) { secondaries ->
        val rawName = secondaries[SecondaryFlagOptions.NAME].orEmpty()
        val name = ensureDataSourceSuffix(rawName)
        val libraries = parseLibraries(secondaries[SecondaryFlagOptions.WITH])
        DataSourceRequest(
            dataSourceName = name,
            useKtor = libraries.contains("ktor"),
            useRetrofit = libraries.contains("retrofit"),
            enableGit = secondaries.containsKey(SecondaryFlagOptions.GIT)
        )
    }

private fun parseLibraries(withValue: String?): Set<String> =
    (withValue.orEmpty())
        .lowercase()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

private fun ensureDataSourceSuffix(name: String): String {
    val trimmedName = name.trim()
    return if (trimmedName.endsWith("DataSource")) {
        trimmedName
    } else {
        "${trimmedName}DataSource"
    }
}
