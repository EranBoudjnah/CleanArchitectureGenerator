package com.mitteloupe.cag.core.content

fun buildPresentationNavigationEventKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    featureName: String
): String =
    """package $featurePackageName.presentation.navigation

import ${projectNamespace}architecture.presentation.navigation.PresentationNavigationEvent

sealed interface ${featureName}PresentationNavigationEvent : PresentationNavigationEvent {
    data object OnEvent : ${featureName}PresentationNavigationEvent
}
"""
