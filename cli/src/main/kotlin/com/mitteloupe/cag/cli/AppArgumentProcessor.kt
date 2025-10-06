package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.flag.PrimaryFlag
import com.mitteloupe.cag.cli.flag.PrimaryFlag.HelpPrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewArchitecturePrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewDataSourcePrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewFeaturePrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewProjectPrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewUseCasePrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewViewModelPrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.VersionPrimary
import com.mitteloupe.cag.cli.flag.SecondaryFlagConstants
import com.mitteloupe.cag.cli.request.ArchitectureRequest
import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import com.mitteloupe.cag.cli.request.ProjectTemplateRequest
import com.mitteloupe.cag.cli.request.UseCaseRequest
import com.mitteloupe.cag.cli.request.ViewModelRequest

private val PRIMARY_FLAGS =
    setOf(
        VersionPrimary,
        NewProjectPrimary,
        NewArchitecturePrimary,
        NewFeaturePrimary,
        NewDataSourcePrimary,
        NewUseCasePrimary,
        NewViewModelPrimary,
        HelpPrimary
    )

class AppArgumentProcessor(private val argumentParser: ArgumentParser = ArgumentParser()) {
    fun isHelpRequested(arguments: Array<String>): Boolean = argumentParser.parsePrimaryWithSecondaries(arguments, HelpPrimary).isNotEmpty()

    fun getHelpOptions(arguments: Array<String>): HelpOptions? {
        val primaryFlagMatches = argumentParser.parsePrimaryWithSecondaries(arguments, HelpPrimary)
        if (primaryFlagMatches.isEmpty()) {
            return null
        }
        val secondaryFlags = primaryFlagMatches.first()
        val topic = secondaryFlags[SecondaryFlagConstants.HELP_TOPIC]
        val format = secondaryFlags[SecondaryFlagConstants.HELP_FORMAT]
        return HelpOptions(topic = topic, format = format)
    }

    fun isVersionRequested(arguments: Array<String>): Boolean =
        argumentParser.parsePrimaryWithSecondaries(arguments, VersionPrimary).isNotEmpty()

    fun validateNoUnknownFlags(arguments: Array<String>) {
        val consumedArguments =
            PRIMARY_FLAGS.flatMap { getConsumedArguments(arguments, it) }.toSet()

        val knownFlags = getAllPrimaryFlagStrings()

        val unknownFlags =
            arguments.filter { argument ->
                argument.startsWith("-") &&
                    !consumedArguments.contains(argument) &&
                    !knownFlags.contains(argument)
            }

        if (unknownFlags.isNotEmpty()) {
            throw IllegalArgumentException("Unknown flags: ${unknownFlags.joinToString(", ")}")
        }
    }

    fun getNewFeatures(arguments: Array<String>): List<FeatureRequest> =
        parseWithNameFlag(
            arguments = arguments,
            primaryFlag = NewFeaturePrimary
        ) { secondaries ->
            FeatureRequest(
                featureName = secondaries[SecondaryFlagConstants.NAME].orEmpty(),
                packageName = secondaries[SecondaryFlagConstants.PACKAGE],
                enableKtlint = secondaries.containsKey(SecondaryFlagConstants.KTLINT),
                enableDetekt = secondaries.containsKey(SecondaryFlagConstants.DETEKT),
                enableGit = secondaries.containsKey(SecondaryFlagConstants.GIT)
            )
        }

    fun getNewDataSources(arguments: Array<String>): List<DataSourceRequest> =
        parseWithNameFlag(
            arguments = arguments,
            primaryFlag = NewDataSourcePrimary
        ) { secondaries ->
            val rawName = secondaries[SecondaryFlagConstants.NAME].orEmpty()
            val name = ensureDataSourceSuffix(rawName)
            val libraries = parseLibraries(secondaries[SecondaryFlagConstants.WITH])
            DataSourceRequest(
                dataSourceName = name,
                useKtor = libraries.contains("ktor"),
                useRetrofit = libraries.contains("retrofit"),
                enableGit = secondaries.containsKey(SecondaryFlagConstants.GIT)
            )
        }

    fun getNewUseCases(arguments: Array<String>): List<UseCaseRequest> =
        parseWithNameFlag(
            arguments = arguments,
            primaryFlag = NewUseCasePrimary
        ) { secondaries ->
            UseCaseRequest(
                useCaseName = secondaries[SecondaryFlagConstants.NAME].orEmpty(),
                targetPath = secondaries[SecondaryFlagConstants.PATH],
                inputDataType = secondaries[SecondaryFlagConstants.INPUT_TYPE],
                outputDataType = secondaries[SecondaryFlagConstants.OUTPUT_TYPE]
            )
        }

    fun getNewViewModels(arguments: Array<String>): List<ViewModelRequest> =
        parseWithNameFlag(
            arguments = arguments,
            primaryFlag = NewViewModelPrimary
        ) { secondaries ->
            ViewModelRequest(
                viewModelName = secondaries[SecondaryFlagConstants.NAME].orEmpty(),
                targetPath = secondaries[SecondaryFlagConstants.PATH],
                enableGit = secondaries.containsKey(SecondaryFlagConstants.GIT)
            )
        }

    fun getNewArchitecture(arguments: Array<String>): List<ArchitectureRequest> =
        argumentParser.parsePrimaryWithSecondaries(
            arguments = arguments,
            primaryFlag = NewArchitecturePrimary
        ).map { secondaries ->
            ArchitectureRequest(
                enableCompose = !secondaries.containsKey(SecondaryFlagConstants.NO_COMPOSE),
                enableKtlint = secondaries.containsKey(SecondaryFlagConstants.KTLINT),
                enableDetekt = secondaries.containsKey(SecondaryFlagConstants.DETEKT),
                enableGit = secondaries.containsKey(SecondaryFlagConstants.GIT)
            )
        }

    fun getNewProjectTemplate(arguments: Array<String>): List<ProjectTemplateRequest> =
        parseWithNameFlag(
            arguments = arguments,
            primaryFlag = NewProjectPrimary
        ) { secondaries ->
            ProjectTemplateRequest(
                projectName = secondaries[SecondaryFlagConstants.NAME].orEmpty(),
                packageName = secondaries[SecondaryFlagConstants.PACKAGE].orEmpty(),
                enableCompose = !secondaries.containsKey(SecondaryFlagConstants.NO_COMPOSE),
                enableKtlint = secondaries.containsKey(SecondaryFlagConstants.KTLINT),
                enableDetekt = secondaries.containsKey(SecondaryFlagConstants.DETEKT),
                enableKtor = secondaries.containsKey(SecondaryFlagConstants.KTOR),
                enableRetrofit = secondaries.containsKey(SecondaryFlagConstants.RETROFIT),
                enableGit = secondaries.containsKey(SecondaryFlagConstants.GIT)
            )
        }

    private inline fun <T> parseWithNameFlag(
        arguments: Array<String>,
        primaryFlag: PrimaryFlag,
        transform: (Map<String, String>) -> T
    ): List<T> {
        return argumentParser.parsePrimaryWithSecondaries(
            arguments = arguments,
            primaryFlag = primaryFlag
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
        (withValue.orEmpty()).lowercase()
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

    private fun getConsumedArguments(
        arguments: Array<String>,
        primaryFlag: PrimaryFlag
    ): Set<String> {
        val consumedArguments = mutableSetOf<String>()
        val isLongForm = determineForm(arguments, primaryFlag) ?: return emptySet()
        val primary = if (isLongForm) primaryFlag.long else primaryFlag.short

        val secondaryFlags = primaryFlag.secondaryFlags
        val secondaryMap = secondaryFlags.associateBy { if (isLongForm) it.long else it.short }

        var index = 0
        while (index < arguments.size) {
            val token = arguments[index]
            if (token == primary) {
                consumedArguments.add(token)
                index++

                while (index < arguments.size) {
                    val nextToken = arguments[index]
                    if (nextToken.startsWith("-")) {
                        if (nextToken in getAllPrimaryFlagStrings()) {
                            break
                        }

                        val matchingKey =
                            secondaryMap.keys.find { key ->
                                nextToken == key || nextToken.startsWith("$key=")
                            }

                        if (matchingKey != null) {
                            consumedArguments.add(nextToken)
                            index++

                            val secondary = secondaryMap.getValue(matchingKey)
                            if (!secondary.isBoolean && !nextToken.contains("=")) {
                                if (index < arguments.size && !arguments[index].startsWith("-")) {
                                    consumedArguments.add(arguments[index])
                                    index++
                                }
                            }
                        } else {
                            break
                        }
                    } else {
                        consumedArguments.add(nextToken)
                        index++
                    }
                }
            } else {
                index++
            }
        }

        return consumedArguments
    }

    private fun determineForm(
        arguments: Array<String>,
        primaryFlag: PrimaryFlag
    ): Boolean? {
        val firstPrimaryIndex = arguments.indexOfFirst { it in setOf(primaryFlag.long, primaryFlag.short) }
        return if (firstPrimaryIndex < 0) {
            null
        } else {
            arguments[firstPrimaryIndex] == primaryFlag.long
        }
    }

    private fun getAllPrimaryFlagStrings(): Set<String> = PRIMARY_FLAGS.flatMap { listOf(it.long, it.short) }.toSet()

    data class HelpOptions(val topic: String?, val format: String?)
}
