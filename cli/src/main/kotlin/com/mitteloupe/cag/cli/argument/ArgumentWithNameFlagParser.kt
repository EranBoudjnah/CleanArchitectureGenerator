package com.mitteloupe.cag.cli.argument

import com.mitteloupe.cag.cli.flag.FlagOption
import com.mitteloupe.cag.cli.flag.PrimaryFlag
import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import com.mitteloupe.cag.cli.request.UseCaseRequest

internal inline fun <T> ArgumentParser.parseWithNameFlag(
    arguments: Array<String>,
    primaryFlag: PrimaryFlag,
    transform: (Map<FlagOption, String>) -> T
): List<T> =
    parsePrimaryWithSecondaries(arguments = arguments, primaryFlag = primaryFlag)
        .map(transform)
        .filter(::isValidRequest)

internal fun <T> isValidRequest(request: T): Boolean =
    when (request) {
        is FeatureRequest -> request.featureName.isNotEmpty()
        is DataSourceRequest -> request.dataSourceName.isNotEmpty()
        is UseCaseRequest -> request.useCaseName.isNotEmpty()
        else -> true
    }
