package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.request.ArchitectureRequest
import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import com.mitteloupe.cag.cli.request.UseCaseRequest

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

    fun getNewUseCases(arguments: Array<String>): List<UseCaseRequest> =
        argumentParser.parsePrimaryWithSecondaries(
            arguments = arguments,
            primaryLong = "--new-use-case",
            primaryShort = "-nuc",
            secondaryFlags =
                listOf(
                    SecondaryFlag(long = "--path", short = "-p"),
                    SecondaryFlag(long = "--input-type", short = "-it"),
                    SecondaryFlag(long = "--output-type", short = "-ot")
                )
        ).map { (useCaseName, secondaries) ->
            UseCaseRequest(
                useCaseName = useCaseName,
                targetPath = secondaries["--path"],
                inputDataType = secondaries["--input-type"],
                outputDataType = secondaries["--output-type"]
            )
        }

    fun getNewArchitecture(arguments: Array<String>): List<ArchitectureRequest> {
        val pairs =
            argumentParser.parsePrimaryWithSecondaries(
                arguments = arguments,
                primaryLong = "--new-architecture",
                primaryShort = "-na",
                secondaryFlags =
                    listOf(
                        SecondaryFlag(long = "--package", short = "-p"),
                        SecondaryFlag(long = "--no-compose", short = "-nc")
                    )
            )
        return pairs.map { (packageName, secondaries) ->
            val enableCompose = secondaries["--no-compose"] == null
            ArchitectureRequest(
                packageName = packageName,
                enableCompose = enableCompose
            )
        }
    }

    private fun ensureDataSourceSuffix(name: String): String {
        val trimmedName = name.trim()
        return if (trimmedName.endsWith("DataSource")) trimmedName else "${trimmedName}DataSource"
    }
}
