package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildDomainToPresentationMapperKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationModelKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationNavigationEventKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationToDomainMapperKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationViewModelKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationViewStateKotlinFile
import com.mitteloupe.cag.core.content.capitalized
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
        writePresentationViewState(featureRoot, featurePackageName, featureName)
        writePresentationNavigationEvent(featureRoot, projectNamespace, featurePackageName, featureName)
        writePresentationViewModelFile(featureRoot, projectNamespace, featurePackageName, featureName)
    }

    private fun writePresentationViewState(
        featureRoot: File,
        featurePackageName: String,
        featureName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "presentation/model",
            fileName = "${featureName.capitalized}ViewState.kt",
            content =
                buildPresentationViewStateKotlinFile(
                    featurePackageName = featurePackageName,
                    featureName = featureName.capitalized
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
            relativePackageSubPath = "presentation/model",
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
            relativePackageSubPath = "presentation/mapper",
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
            relativePackageSubPath = "presentation/mapper",
            fileName = "StubDomainMapper.kt",
            content = buildPresentationToDomainMapperKotlinFile(featurePackageName)
        )
    }

    private fun writePresentationViewModelFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "presentation/viewmodel",
            fileName = "${featureName.capitalized}ViewModel.kt",
            content =
                buildPresentationViewModelKotlinFile(
                    projectNamespace = projectNamespace,
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
            relativePackageSubPath = "presentation/navigation",
            fileName = "${featureName.capitalized}PresentationNavigationEvent.kt",
            content =
                buildPresentationNavigationEventKotlinFile(
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = featureName.capitalized
                )
        )
    }

    fun generateViewModel(
        destinationDirectory: File,
        viewModelName: String,
        featurePackageName: String,
        projectNamespace: String
    ) {
        writePresentationViewModelFile(
            featureRoot = destinationDirectory,
            projectNamespace = projectNamespace,
            featurePackageName = featurePackageName,
            featureName = viewModelName
        )
    }
}
