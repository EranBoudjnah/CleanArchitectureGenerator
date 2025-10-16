package com.mitteloupe.cag.cli.argument.feature

import com.mitteloupe.cag.cli.argument.ArgumentParser
import com.mitteloupe.cag.cli.argument.feature.mapper.toDependencyInjection
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewProjectPrimary
import com.mitteloupe.cag.cli.flag.SecondaryFlagOptions
import com.mitteloupe.cag.cli.request.ProjectTemplateRequest

fun ArgumentParser.parseNewProjectArguments(arguments: Array<String>) =
    parsePrimaryWithSecondaries(arguments = arguments, primaryFlag = NewProjectPrimary) { secondaries ->
        ProjectTemplateRequest(
            projectName = secondaries[SecondaryFlagOptions.NAME].orEmpty(),
            packageName = secondaries[SecondaryFlagOptions.PACKAGE].orEmpty(),
            dependencyInjection = secondaries[SecondaryFlagOptions.DEPENDENCY_INJECTION]?.toDependencyInjection(),
            enableCompose = !secondaries.containsKey(SecondaryFlagOptions.NO_COMPOSE),
            enableKtlint = secondaries.containsKey(SecondaryFlagOptions.KTLINT),
            enableDetekt = secondaries.containsKey(SecondaryFlagOptions.DETEKT),
            enableKtor = secondaries.containsKey(SecondaryFlagOptions.KTOR),
            enableRetrofit = secondaries.containsKey(SecondaryFlagOptions.RETROFIT),
            enableGit = secondaries.containsKey(SecondaryFlagOptions.GIT)
        )
    }
