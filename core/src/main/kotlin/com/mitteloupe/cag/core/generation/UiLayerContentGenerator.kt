package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildPresentationToUiMapperKotlinFile
import com.mitteloupe.cag.core.content.buildUiModelKotlinFile
import java.io.File

class UiLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator = KotlinFileCreator()
) {
    fun generate(
        featureRoot: File,
        featurePackageName: String
    ): String? {
        writeUiModelFile(featureRoot, featurePackageName)?.let { return it }
        return writePresentationToUiMapperFile(featureRoot, featurePackageName)
    }

    private fun writeUiModelFile(
        featureRoot: File,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "ui",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "model",
            fileName = "StubUiModel.kt",
            content = buildUiModelKotlinFile(featurePackageName)
        )

    private fun writePresentationToUiMapperFile(
        featureRoot: File,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "ui",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "mapper",
            fileName = "StubUiMapper.kt",
            content = buildPresentationToUiMapperKotlinFile(featurePackageName)
        )
}
