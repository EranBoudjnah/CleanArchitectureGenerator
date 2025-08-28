package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildPresentationMapperKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationModelKotlinFile
import com.mitteloupe.cag.core.content.buildPresentationViewStateKotlinFile
import java.io.File

class PresentationLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator = KotlinFileCreator()
) {
    fun generate(
        featureRoot: File,
        featurePackageName: String,
        featureName: String
    ): String? {
        writePresentationModelFile(featureRoot, featurePackageName)?.let { return it }
        writePresentationMapperFile(featureRoot, featurePackageName)?.let { return it }
        return writePresentationViewState(featureRoot, featurePackageName, featureName)
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
            fileName = "${featureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}ViewState.kt",
            content =
                buildPresentationViewStateKotlinFile(
                    featurePackageName = featurePackageName,
                    featureName = featureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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

    private fun writePresentationMapperFile(
        featureRoot: File,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "presentation",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "mapper",
            fileName = "StubDomainToPresentationMapper.kt",
            content = buildPresentationMapperKotlinFile(featurePackageName)
        )
}
