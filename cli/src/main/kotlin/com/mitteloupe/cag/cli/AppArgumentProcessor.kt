package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.request.ArchitectureRequest
import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import com.mitteloupe.cag.cli.request.UseCaseRequest

class AppArgumentProcessor(private val argumentParser: ArgumentParser = ArgumentParser()) {
    fun isHelpRequested(arguments: Array<String>): Boolean = arguments.any { it == "--help" || it == "-h" }

    fun getNewFeatures(arguments: Array<String>): List<FeatureRequest> =
        argumentParser.parsePrimaryWithSecondaries(
            arguments = arguments,
            primaryLong = "--new-feature",
            primaryShort = "-nf",
            secondaryFlags =
                listOf(
                    SecondaryFlag(
                        long = "--name",
                        short = "-n",
                        isMandatory = true,
                        missingErrorMessage = "Feature name is required. Use --name=FeatureName or -n=FeatureName"
                    ),
                    SecondaryFlag(long = "--package", short = "-p")
                )
        ).map { secondaries ->
            val featureName = secondaries["--name"] ?: ""
            FeatureRequest(
                featureName = featureName,
                packageName = secondaries["--package"]
            )
        }.filter { it.featureName.isNotEmpty() }

    fun getNewDataSources(arguments: Array<String>): List<DataSourceRequest> =
        argumentParser.parsePrimaryWithSecondaries(
            arguments = arguments,
            primaryLong = "--new-datasource",
            primaryShort = "-nds",
            secondaryFlags =
                listOf(
                    SecondaryFlag(
                        long = "--name",
                        short = "-n",
                        isMandatory = true,
                        missingErrorMessage = "Data source name is required. Use --name=DataSourceName or -n=DataSourceName"
                    ),
                    SecondaryFlag(long = "--with", short = "-w")
                )
        ).map { secondaries ->
            val rawName = secondaries["--name"] ?: ""
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
        }.filter { it.dataSourceName.isNotEmpty() }

    fun getNewUseCases(arguments: Array<String>): List<UseCaseRequest> =
        argumentParser.parsePrimaryWithSecondaries(
            arguments = arguments,
            primaryLong = "--new-use-case",
            primaryShort = "-nuc",
            secondaryFlags =
                listOf(
                    SecondaryFlag(
                        long = "--name",
                        short = "-n",
                        isMandatory = true,
                        missingErrorMessage = "Use case name is required. Use --name=UseCaseName or -n=UseCaseName"
                    ),
                    SecondaryFlag(long = "--path", short = "-p"),
                    SecondaryFlag(long = "--input-type", short = "-it"),
                    SecondaryFlag(long = "--output-type", short = "-ot")
                )
        ).map { secondaries ->
            val useCaseName = secondaries["--name"] ?: ""
            UseCaseRequest(
                useCaseName = useCaseName,
                targetPath = secondaries["--path"],
                inputDataType = secondaries["--input-type"],
                outputDataType = secondaries["--output-type"]
            )
        }.filter { it.useCaseName.isNotEmpty() }

    fun getNewArchitecture(arguments: Array<String>): List<ArchitectureRequest> {
        val results = mutableListOf<ArchitectureRequest>()
        var currentEnableCompose = true

        var index = 0
        while (index < arguments.size) {
            val token = arguments[index]
            when {
                token == "--new-architecture" || token == "-na" -> {
                    results.add(ArchitectureRequest(enableCompose = currentEnableCompose))
                    currentEnableCompose = true
                }
                token == "--no-compose" || token == "-nc" -> {
                    if (results.isNotEmpty()) {
                        val lastIndex = results.lastIndex
                        results[lastIndex] = results[lastIndex].copy(enableCompose = false)
                    } else {
                        currentEnableCompose = false
                    }
                }
            }
            index++
        }

        return results
    }

    private fun ensureDataSourceSuffix(name: String): String {
        val trimmedName = name.trim()
        return if (trimmedName.endsWith("DataSource")) trimmedName else "${trimmedName}DataSource"
    }
}
