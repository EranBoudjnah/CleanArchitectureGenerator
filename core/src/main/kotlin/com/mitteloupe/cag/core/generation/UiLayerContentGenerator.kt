package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildPresentationToUiMapperKotlinFile
import com.mitteloupe.cag.core.content.buildUiDiKotlinFile
import com.mitteloupe.cag.core.content.buildUiModelKotlinFile
import com.mitteloupe.cag.core.content.buildUiScreenKotlinFile
import com.mitteloupe.cag.core.content.capitalized
import java.io.File

class UiLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator = KotlinFileCreator()
) {
    fun generate(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ): String? {
        writeUiModelFile(featureRoot, featurePackageName)?.let { return it }
        writePresentationToUiMapperFile(featureRoot, featurePackageName)?.let { return it }
        writeUiDiFile(featureRoot, projectNamespace, featurePackageName, featureName)?.let { return it }
        return writeUiScreenFile(featureRoot, projectNamespace, featurePackageName, featureName)
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

    private fun writeUiDiFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "ui",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "di",
            fileName = "${featureName.capitalized}Dependencies.kt",
            content = buildUiDiKotlinFile(projectNamespace, featurePackageName, featureName)
        )

    private fun writeUiScreenFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "ui",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "",
            fileName = "${featureName.capitalized}Screen.kt",
            content = buildUiScreenKotlinFile(projectNamespace, featurePackageName, featureName)
        )
}
