package com.mitteloupe.cag.cli.argument

import com.mitteloupe.cag.cli.argument.feature.parseNewArchitectureArguments
import com.mitteloupe.cag.cli.argument.feature.parseNewDataSourcesArgument
import com.mitteloupe.cag.cli.argument.feature.parseNewFeaturesArguments
import com.mitteloupe.cag.cli.argument.feature.parseNewProjectArguments
import com.mitteloupe.cag.cli.argument.feature.parseNewUserCasesArguments
import com.mitteloupe.cag.cli.argument.feature.parseNewViewModelsArguments
import com.mitteloupe.cag.cli.flag.PrimaryFlag
import com.mitteloupe.cag.cli.flag.PrimaryFlag.HelpPrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewArchitecturePrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewDataSourcePrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewFeaturePrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewProjectPrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewUseCasePrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewViewModelPrimary
import com.mitteloupe.cag.cli.flag.PrimaryFlag.VersionPrimary
import com.mitteloupe.cag.cli.flag.SecondaryFlagOptions
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

class AppArgumentProcessor(
    private val argumentParser: ArgumentParser = ArgumentParser()
) {
    fun isHelpRequested(arguments: Array<String>): Boolean = argumentParser.parsePrimaryWithSecondaries(arguments, HelpPrimary).isNotEmpty()

    fun getHelpOptions(arguments: Array<String>): HelpOptions? {
        val primaryFlagMatches = argumentParser.parsePrimaryWithSecondaries(arguments, HelpPrimary)
        if (primaryFlagMatches.isEmpty()) {
            return null
        }
        val secondaryFlags = primaryFlagMatches.first()
        val topic = secondaryFlags[SecondaryFlagOptions.HELP_TOPIC]
        val format = secondaryFlags[SecondaryFlagOptions.HELP_FORMAT]
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

    fun getNewFeatures(arguments: Array<String>): List<FeatureRequest> = argumentParser.parseNewFeaturesArguments(arguments)

    fun getNewDataSources(arguments: Array<String>): List<DataSourceRequest> = argumentParser.parseNewDataSourcesArgument(arguments)

    fun getNewUseCases(arguments: Array<String>): List<UseCaseRequest> = argumentParser.parseNewUserCasesArguments(arguments)

    fun getNewViewModels(arguments: Array<String>): List<ViewModelRequest> = argumentParser.parseNewViewModelsArguments(arguments)

    fun getNewArchitecture(arguments: Array<String>): List<ArchitectureRequest> = argumentParser.parseNewArchitectureArguments(arguments)

    fun getNewProjectTemplate(arguments: Array<String>): List<ProjectTemplateRequest> = argumentParser.parseNewProjectArguments(arguments)

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
        val allPrimaryFlagStrings = getAllPrimaryFlagStrings()
        while (index < arguments.size) {
            val token = arguments[index]
            if (token == primary) {
                consumedArguments.add(token)
                index++

                while (index < arguments.size) {
                    val nextToken = arguments[index]
                    if (nextToken.startsWith("-")) {
                        if (nextToken in allPrimaryFlagStrings) {
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

    data class HelpOptions(
        val topic: String?,
        val format: String?
    )
}
