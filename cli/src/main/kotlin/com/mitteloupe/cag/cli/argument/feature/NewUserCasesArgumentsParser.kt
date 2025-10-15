package com.mitteloupe.cag.cli.argument.feature

import com.mitteloupe.cag.cli.argument.ArgumentParser
import com.mitteloupe.cag.cli.argument.parseWithNameFlag
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewUseCasePrimary
import com.mitteloupe.cag.cli.flag.SecondaryFlagOptions
import com.mitteloupe.cag.cli.request.UseCaseRequest

fun ArgumentParser.parseNewUserCasesArguments(arguments: Array<String>): List<UseCaseRequest> =
    parseWithNameFlag(arguments = arguments, primaryFlag = NewUseCasePrimary) { secondaries ->
        UseCaseRequest(
            useCaseName = secondaries[SecondaryFlagOptions.NAME].orEmpty(),
            targetPath = secondaries[SecondaryFlagOptions.PATH],
            inputDataType = secondaries[SecondaryFlagOptions.INPUT_TYPE],
            outputDataType = secondaries[SecondaryFlagOptions.OUTPUT_TYPE],
            enableGit = secondaries.containsKey(SecondaryFlagOptions.GIT)
        )
    }
