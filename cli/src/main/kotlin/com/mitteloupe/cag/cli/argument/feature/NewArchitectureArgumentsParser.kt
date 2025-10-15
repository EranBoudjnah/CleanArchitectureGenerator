package com.mitteloupe.cag.cli.argument.feature

import com.mitteloupe.cag.cli.argument.ArgumentParser
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewArchitecturePrimary
import com.mitteloupe.cag.cli.flag.SecondaryFlagOptions
import com.mitteloupe.cag.cli.request.ArchitectureRequest
import com.mitteloupe.cag.core.option.DependencyInjection

fun ArgumentParser.parseNewArchitectureArguments(arguments: Array<String>): List<ArchitectureRequest> =
    parsePrimaryWithSecondaries(arguments = arguments, primaryFlag = NewArchitecturePrimary)
        .map { secondaries ->
            ArchitectureRequest(
                appModuleDirectory = null,
                dependencyInjection = DependencyInjection.Hilt,
                enableCompose = !secondaries.containsKey(SecondaryFlagOptions.NO_COMPOSE),
                enableKtlint = secondaries.containsKey(SecondaryFlagOptions.KTLINT),
                enableDetekt = secondaries.containsKey(SecondaryFlagOptions.DETEKT),
                enableGit = secondaries.containsKey(SecondaryFlagOptions.GIT)
            )
        }
