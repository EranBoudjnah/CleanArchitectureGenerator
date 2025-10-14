package com.mitteloupe.cag.cleanarchitecturegenerator.model

import com.mitteloupe.cag.core.option.DependencyInjection

enum class DependencyInjection(
    val coreValue: DependencyInjection
) {
    Hilt(DependencyInjection.Hilt),
    Koin(DependencyInjection.Koin),
    None(DependencyInjection.None)
}
