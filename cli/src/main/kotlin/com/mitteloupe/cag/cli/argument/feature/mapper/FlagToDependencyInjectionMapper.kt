package com.mitteloupe.cag.cli.argument.feature.mapper

import com.mitteloupe.cag.core.option.DependencyInjection

fun String.toDependencyInjection() =
    when (lowercase()) {
        "hilt" -> DependencyInjection.Hilt
        "koin" -> DependencyInjection.Koin
        "none" -> DependencyInjection.None
        else -> throw IllegalArgumentException("Unknown dependency injection value: $this")
    }
