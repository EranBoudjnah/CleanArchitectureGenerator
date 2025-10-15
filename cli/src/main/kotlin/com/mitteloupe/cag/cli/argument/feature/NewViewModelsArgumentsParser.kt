package com.mitteloupe.cag.cli.argument.feature

import com.mitteloupe.cag.cli.argument.ArgumentParser
import com.mitteloupe.cag.cli.argument.parseWithNameFlag
import com.mitteloupe.cag.cli.flag.PrimaryFlag
import com.mitteloupe.cag.cli.flag.SecondaryFlagOptions
import com.mitteloupe.cag.cli.request.ViewModelRequest

fun ArgumentParser.parseNewViewModelsArguments(arguments: Array<String>): List<ViewModelRequest> =
    parseWithNameFlag(arguments = arguments, primaryFlag = PrimaryFlag.NewViewModelPrimary) { secondaries ->
        ViewModelRequest(
            viewModelName = secondaries[SecondaryFlagOptions.NAME].orEmpty(),
            targetPath = secondaries[SecondaryFlagOptions.PATH],
            enableGit = secondaries.containsKey(SecondaryFlagOptions.GIT)
        )
    }
