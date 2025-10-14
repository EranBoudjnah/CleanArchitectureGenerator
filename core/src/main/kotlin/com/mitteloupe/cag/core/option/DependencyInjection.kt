package com.mitteloupe.cag.core.option

sealed interface DependencyInjection {
    data object None : DependencyInjection

    data object Hilt : DependencyInjection

    data object Koin : DependencyInjection
}
