package com.mitteloupe.cag.cleanarchitecturegenerator.model

import com.mitteloupe.cag.core.option.DependencyInjection

enum class DependencyInjection(
    val coreValue: DependencyInjection
) {
    Hilt(DependencyInjection.Hilt),
    Koin(DependencyInjection.Koin),
    None(DependencyInjection.None);

    companion object {
        fun fromString(value: String) =
            entries
                .firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unexpected value $value")
    }
}
