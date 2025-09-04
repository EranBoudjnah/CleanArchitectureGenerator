package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.request.ArchitectureRequest
import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import com.mitteloupe.cag.cli.request.UseCaseRequest

class AppArgumentProcessor(private val argumentParser: ArgumentParser = ArgumentParser()) {
    fun isHelpRequested(arguments: Array<String>): Boolean = arguments.any { it in setOf("--help", "-h") }

    fun getNewFeatures(arguments: Array<String>): List<FeatureRequest> =
        parseWithNameFlag(
            arguments = arguments,
            primaryLong = "--new-feature",
            primaryShort = "-nf",
            nameErrorMessage = "Feature name is required. Use --name=FeatureName or -n=FeatureName",
            additionalFlags = listOf(SecondaryFlag("--package", "-p"))
        ) { secondaries ->
            FeatureRequest(
                featureName = secondaries["--name"] ?: "",
                packageName = secondaries["--package"]
            )
        }

    fun getNewDataSources(arguments: Array<String>): List<DataSourceRequest> =
        parseWithNameFlag(
            arguments = arguments,
            primaryLong = "--new-datasource",
            primaryShort = "-nds",
            nameErrorMessage = "Data source name is required. Use --name=DataSourceName or -n=DataSourceName",
            additionalFlags = listOf(SecondaryFlag("--with", "-w"))
        ) { secondaries ->
            val rawName = secondaries["--name"] ?: ""
            val name = ensureDataSourceSuffix(rawName)
            val libraries = parseLibraries(secondaries["--with"])
            DataSourceRequest(
                dataSourceName = name,
                useKtor = libraries.contains("ktor"),
                useRetrofit = libraries.contains("retrofit")
            )
        }

    fun getNewUseCases(arguments: Array<String>): List<UseCaseRequest> =
        parseWithNameFlag(
            arguments = arguments,
            primaryLong = "--new-use-case",
            primaryShort = "-nuc",
            nameErrorMessage = "Use case name is required. Use --name=UseCaseName or -n=UseCaseName",
            additionalFlags =
                listOf(
                    SecondaryFlag("--path", "-p"),
                    SecondaryFlag("--input-type", "-it"),
                    SecondaryFlag("--output-type", "-ot")
                )
        ) { secondaries ->
            UseCaseRequest(
                useCaseName = secondaries["--name"] ?: "",
                targetPath = secondaries["--path"],
                inputDataType = secondaries["--input-type"],
                outputDataType = secondaries["--output-type"]
            )
        }

    fun getNewArchitecture(arguments: Array<String>): List<ArchitectureRequest> =
        argumentParser.parsePrimaryWithSecondaries(
            arguments = arguments,
            primaryLong = "--new-architecture",
            primaryShort = "-na",
            secondaryFlags =
                listOf(
                    SecondaryFlag("--no-compose", "-nc", isBoolean = true),
                    SecondaryFlag("--ktlint", "-kl", isBoolean = true),
                    SecondaryFlag("--detekt", "-d", isBoolean = true)
                )
        ).map { secondaries ->
            ArchitectureRequest(
                enableCompose = !secondaries.containsKey("--no-compose"),
                enableKtlint = secondaries.containsKey("--ktlint"),
                enableDetekt = secondaries.containsKey("--detekt")
            )
        }

    private inline fun <T> parseWithNameFlag(
        arguments: Array<String>,
        primaryLong: String,
        primaryShort: String,
        nameErrorMessage: String,
        additionalFlags: List<SecondaryFlag>,
        transform: (Map<String, String>) -> T
    ): List<T> {
        val allFlags =
            listOf(
                SecondaryFlag("--name", "-n", isMandatory = true, missingErrorMessage = nameErrorMessage)
            ) + additionalFlags

        return argumentParser.parsePrimaryWithSecondaries(
            arguments = arguments,
            primaryLong = primaryLong,
            primaryShort = primaryShort,
            secondaryFlags = allFlags
        ).map(transform).filter { isValidRequest(it) }
    }

    private fun <T> isValidRequest(request: T): Boolean =
        when (request) {
            is FeatureRequest -> request.featureName.isNotEmpty()
            is DataSourceRequest -> request.dataSourceName.isNotEmpty()
            is UseCaseRequest -> request.useCaseName.isNotEmpty()
            else -> true
        }

    private fun parseLibraries(withValue: String?): Set<String> =
        (withValue ?: "").lowercase()
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
}
