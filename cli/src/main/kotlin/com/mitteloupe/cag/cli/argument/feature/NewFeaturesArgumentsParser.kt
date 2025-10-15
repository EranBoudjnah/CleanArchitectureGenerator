package com.mitteloupe.cag.cli.argument.feature

import com.mitteloupe.cag.cli.argument.ArgumentParser
import com.mitteloupe.cag.cli.argument.parseWithNameFlag
import com.mitteloupe.cag.cli.flag.PrimaryFlag.NewFeaturePrimary
import com.mitteloupe.cag.cli.flag.SecondaryFlagOptions
import com.mitteloupe.cag.cli.request.FeatureRequest

fun ArgumentParser.parseNewFeaturesArguments(arguments: Array<String>): List<FeatureRequest> =
    parseWithNameFlag(arguments = arguments, primaryFlag = NewFeaturePrimary) { secondaries ->
        FeatureRequest(
            featureName = secondaries[SecondaryFlagOptions.NAME].orEmpty(),
            packageName = secondaries[SecondaryFlagOptions.PACKAGE],
            enableKtlint = secondaries.containsKey(SecondaryFlagOptions.KTLINT),
            enableDetekt = secondaries.containsKey(SecondaryFlagOptions.DETEKT),
            enableGit = secondaries.containsKey(SecondaryFlagOptions.GIT)
        )
    }
