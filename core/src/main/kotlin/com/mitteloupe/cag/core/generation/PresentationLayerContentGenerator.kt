package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildDomainToPresentationMapperKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationModelKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationNavigationEventKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationToDomainMapperKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationViewModelKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationViewStateKotlinFile
import com.mitteloupe.cag.core.content.capitalized
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import java.io.File

class PresentationLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator = KotlinFileCreator()
) {
    fun generatePresentationLayer(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ) {
        writePresentationModelFile(featureRoot, featurePackageName)
        writeDomainToPresentationMapperFile(featureRoot, featurePackageName)
        writePresentationToDomainMapperFile(featureRoot, featurePackageName)
        writePresentationViewStateForFeature(featureRoot, featurePackageName, featureName)
        writePresentationNavigationEvent(featureRoot, projectNamespace, featurePackageName, featureName)
        writePresentationViewModelFileForFeature(featureRoot, projectNamespace, featurePackageName, featureName)
    }

    private fun writePresentationViewStateToDirectory(
        destinationDirectory: File,
        featurePackageName: String,
        featureName: String
    ) {
        FileCreator.createDirectoryIfNotExists(destinationDirectory)
        kotlinFileCreator.writeKotlinFileInDirectory(
            targetDirectory = destinationDirectory,
            fileName = "${featureName.capitalized}ViewState.kt",
            content =
                buildPresentationViewStateKotlinFile(
                    featurePackageName = featurePackageName,
                    featureName = featureName.capitalized
                )
        )
    }

    private fun writePresentationViewStateForFeature(
        featureRoot: File,
        featurePackageName: String,
        featureName: String
    ) {
        val capitalizedFeatureName = featureName.capitalized
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "model",
            fileName = "${capitalizedFeatureName}ViewState.kt",
            content =
                buildPresentationViewStateKotlinFile(
                    featurePackageName = featurePackageName,
                    featureName = capitalizedFeatureName
                )
        )
    }

    private fun writePresentationModelFile(
        featureRoot: File,
        featurePackageName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "model",
            fileName = "StubPresentationModel.kt",
            content =
                buildPresentationModelKotlinFile(featurePackageName)
        )
    }

    private fun writeDomainToPresentationMapperFile(
        featureRoot: File,
        featurePackageName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "mapper",
            fileName = "StubPresentationMapper.kt",
            content = buildDomainToPresentationMapperKotlinFile(featurePackageName)
        )
    }

    private fun writePresentationToDomainMapperFile(
        featureRoot: File,
        featurePackageName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "mapper",
            fileName = "StubDomainMapper.kt",
            content = buildPresentationToDomainMapperKotlinFile(featurePackageName)
        )
    }

    private fun writePresentationViewModelFileToDirectory(
        destinationDirectory: File,
        projectNamespace: String,
        viewModelPackageName: String,
        featurePackageName: String,
        featureName: String
    ) {
        FileCreator.createDirectoryIfNotExists(destinationDirectory)
        kotlinFileCreator.writeKotlinFileInDirectory(
            targetDirectory = destinationDirectory,
            fileName = "${featureName.capitalized}ViewModel.kt",
            content =
                buildPresentationViewModelKotlinFile(
                    projectNamespace = projectNamespace,
                    viewModelPackageName = viewModelPackageName,
                    featurePackageName = featurePackageName,
                    featureName = featureName.capitalized
                )
        )
    }

    private fun writePresentationViewModelFileForFeature(
        destinationDirectory: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = destinationDirectory,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "viewmodel",
            fileName = "${featureName.capitalized}ViewModel.kt",
            content =
                buildPresentationViewModelKotlinFile(
                    projectNamespace = projectNamespace,
                    viewModelPackageName = "$featurePackageName.presentation.viewmodel",
                    featurePackageName = featurePackageName,
                    featureName = featureName.capitalized
                )
        )
    }

    private fun writePresentationNavigationEvent(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "navigation",
            fileName = "${featureName.capitalized}PresentationNavigationEvent.kt",
            content =
                buildPresentationNavigationEventKotlinFile(
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = featureName.capitalized
                )
        )
    }

    fun generateViewState(
        destinationDirectory: File,
        featurePackageName: String,
        featureName: String
    ) {
        writePresentationViewStateToDirectory(
            destinationDirectory = destinationDirectory,
            featurePackageName = featurePackageName,
            featureName = featureName
        )
    }

    fun generateViewModel(
        destinationDirectory: File,
        viewModelName: String,
        viewModelPackageName: String,
        featurePackageName: String,
        projectNamespace: String
    ) {
        val featureName = viewModelName.removeSuffix("ViewModel")
        writePresentationViewModelFileToDirectory(
            destinationDirectory = destinationDirectory,
            projectNamespace = projectNamespace,
            viewModelPackageName = viewModelPackageName,
            featurePackageName = featurePackageName,
            featureName = featureName
        )
    }
}
