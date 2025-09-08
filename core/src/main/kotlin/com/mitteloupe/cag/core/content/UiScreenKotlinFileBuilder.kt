package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.optimizeImports

fun buildUiScreenKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    featureName: String
): String {
    val className = featureName.capitalized
    return """package $featurePackageName.ui

${
        """
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import $projectNamespace.architecture.ui.view.ScreenEnterObserver
import $featurePackageName.presentation.model.StubPresentationModel
import $featurePackageName.ui.di.${className}Dependencies
""".optimizeImports()
    }
@Composable
fun ${className}Dependencies.${className}Screen(navController: NavController) {
    ScreenEnterObserver {
        ${className.replaceFirstChar { it.lowercase() }}ViewModel.onEnter(
            StubPresentationModel("StubId")
        )
    }

    ViewModelObserver(navController)

    ${className}ScreenContent()
}

@Composable
fun ${className}ScreenContent() {
    Text("$className ready!")
}

@Preview
@Composable
private fun Preview() {
    ${className}ScreenContent()
}
"""
}
