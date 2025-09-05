package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.optimizeImports

fun buildUiDiKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    featureName: String
): String {
    val className = featureName.capitalized
    val variableName = className.replaceFirstChar { it.lowercase() }
    return """package $featurePackageName.ui.di

${
        """
import $projectNamespace.architecture.presentation.notification.PresentationNotification
import $projectNamespace.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import $projectNamespace.architecture.ui.notification.mapper.NotificationUiMapper
import $projectNamespace.architecture.ui.view.BaseComposeHolder
import $featurePackageName.presentation.model.${className}ViewState
import $featurePackageName.presentation.navigation.${className}PresentationNavigationEvent
import $featurePackageName.presentation.viewmodel.${className}ViewModel
import $featurePackageName.ui.mapper.StubUiMapper
""".optimizeImports()
    }
data class ${className}Dependencies(
    val ${variableName}ViewModel: ${className}ViewModel,
    private val ${variableName}NavigationMapper: NavigationEventDestinationMapper<${className}PresentationNavigationEvent>,
    private val ${variableName}NotificationMapper: NotificationUiMapper<PresentationNotification>,
    val stubUiMapper: StubUiMapper
) : BaseComposeHolder<${className}ViewState, PresentationNotification>(
    ${variableName}ViewModel,
    ${variableName}NavigationMapper,
    ${variableName}NotificationMapper
)
"""
}
