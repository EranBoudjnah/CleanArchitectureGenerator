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
    fun generate(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ): String? {
        writePresentationModelFile(featureRoot, featurePackageName)?.let { return it }
        writeDomainToPresentationMapperFile(featureRoot, featurePackageName)?.let { return it }
        writePresentationToDomainMapperFile(featureRoot, featurePackageName)?.let { return it }
        writePresentationViewState(featureRoot, featurePackageName, featureName)?.let { return it }
        writePresentationNavigationEvent(featureRoot, projectNamespace, featurePackageName, featureName)?.let { return it }
        return writePresentationViewModelFile(featureRoot, projectNamespace, featurePackageName, featureName)
    }

    private fun writePresentationViewState(
        featureRoot: File,
        featurePackageName: String,
        featureName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "model",
            fileName = "${featureName.capitalized}ViewState.kt",
            content =
                buildPresentationViewStateKotlinFile(
                    featurePackageName = featurePackageName,
                    featureName = featureName.capitalized
                )
        )

    private fun writePresentationModelFile(
        featureRoot: File,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "model",
            fileName = "StubPresentationModel.kt",
            content =
                buildPresentationModelKotlinFile(featurePackageName)
        )

    private fun writeDomainToPresentationMapperFile(
        featureRoot: File,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "mapper",
            fileName = "StubPresentationMapper.kt",
            content = buildDomainToPresentationMapperKotlinFile(featurePackageName)
        )

    private fun writePresentationToDomainMapperFile(
        featureRoot: File,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "mapper",
            fileName = "StubDomainMapper.kt",
            content = buildPresentationToDomainMapperKotlinFile(featurePackageName)
        )

    private fun writePresentationViewModelFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "viewmodel",
            fileName = "${featureName.capitalized}ViewModel.kt",
            content =
                buildPresentationViewModelKotlinFile(
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName,
                    featureName = featureName.capitalized
                )
        )

    private fun writePresentationNavigationEvent(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ): String? =
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
