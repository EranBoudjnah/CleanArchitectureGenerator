package com.mitteloupe.cag.core.generation.bulk

import com.mitteloupe.cag.core.generation.layer.PresentationLayerContentGenerator
import java.io.File

class ViewModelFilesGenerator(
    private val presentationLayerContentGenerator: PresentationLayerContentGenerator
) {
    fun generateViewModel(
        destinationDirectory: File,
        viewModelName: String,
        viewModelPackageName: String,
        featurePackageName: String,
        projectNamespace: String
    ) {
        val viewModelName = viewModelName.trim()
        val featureName = viewModelName.removeSuffix("ViewModel")

        presentationLayerContentGenerator
            .generateViewState(
                destinationDirectory = File(destinationDirectory.parentFile, "model"),
                featurePackageName = featurePackageName,
                featureName = featureName
            )

        presentationLayerContentGenerator
            .generateViewModel(
                destinationDirectory = destinationDirectory,
                viewModelName = viewModelName,
                viewModelPackageName = viewModelPackageName,
                featurePackageName = featurePackageName,
                projectNamespace = projectNamespace
            )
    }
}
