package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.optimizeImports

fun buildPresentationNavigationEventKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    featureName: String
): String =
    """package $featurePackageName.presentation.navigation

${
        """
import $projectNamespace.architecture.presentation.navigation.PresentationNavigationEvent
""".optimizeImports()
    }
sealed interface ${featureName}PresentationNavigationEvent : PresentationNavigationEvent {
    data object OnEvent : ${featureName}PresentationNavigationEvent
}
"""
