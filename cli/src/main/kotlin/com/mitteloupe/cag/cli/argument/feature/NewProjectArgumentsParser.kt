package com.mitteloupe.cag.cli.argument.feature

import com.mitteloupe.cag.cli.argument.ArgumentParser
import com.mitteloupe.cag.cli.argument.parseWithNameFlag
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewProjectPrimary
import com.mitteloupe.cag.cli.flag.SecondaryFlagOptions
import com.mitteloupe.cag.cli.request.ProjectTemplateRequest
import com.mitteloupe.cag.core.option.DependencyInjection

fun ArgumentParser.parseNewProjectArguments(arguments: Array<String>) =
    parseWithNameFlag(arguments = arguments, primaryFlag = NewProjectPrimary) { secondaries ->
        ProjectTemplateRequest(
            projectName = secondaries[SecondaryFlagOptions.NAME].orEmpty(),
            packageName = secondaries[SecondaryFlagOptions.PACKAGE].orEmpty(),
            dependencyInjection = secondaries[SecondaryFlagOptions.DEPENDENCY_INJECTION].toDependencyInjection(),
            enableCompose = !secondaries.containsKey(SecondaryFlagOptions.NO_COMPOSE),
            enableKtlint = secondaries.containsKey(SecondaryFlagOptions.KTLINT),
            enableDetekt = secondaries.containsKey(SecondaryFlagOptions.DETEKT),
            enableKtor = secondaries.containsKey(SecondaryFlagOptions.KTOR),
            enableRetrofit = secondaries.containsKey(SecondaryFlagOptions.RETROFIT),
            enableGit = secondaries.containsKey(SecondaryFlagOptions.GIT)
        )
    }

private fun String?.toDependencyInjection() =
    when (this?.lowercase()) {
        null, "hilt" -> DependencyInjection.Hilt
        "koin" -> DependencyInjection.Koin
        "none" -> DependencyInjection.None
        else -> throw IllegalArgumentException("Unknown dependency injection value: $this")
    }
