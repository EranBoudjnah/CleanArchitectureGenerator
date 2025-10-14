package com.mitteloupe.cag.core.generation.layer

import com.mitteloupe.cag.core.content.buildPresentationToUiMapperKotlinFile
import com.mitteloupe.cag.core.content.buildUiDiKotlinFile
import com.mitteloupe.cag.core.content.buildUiModelKotlinFile
import com.mitteloupe.cag.core.content.buildUiScreenKotlinFile
import com.mitteloupe.cag.core.content.capitalized
import com.mitteloupe.cag.core.generation.KotlinFileCreator
import java.io.File

class UiLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator
) {
    fun generate(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ) {
        writeUiModelFile(featureRoot, featurePackageName)
        writePresentationToUiMapperFile(featureRoot, featurePackageName)
        writeUiDiFile(featureRoot, projectNamespace, featurePackageName, featureName)
        writeUiScreenFile(featureRoot, projectNamespace, featurePackageName, featureName)
    }

    private fun writeUiModelFile(
        featureRoot: File,
        featurePackageName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "ui",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "model",
            fileName = "StubUiModel.kt"
        ) { buildUiModelKotlinFile(featurePackageName) }
    }

    private fun writePresentationToUiMapperFile(
        featureRoot: File,
        featurePackageName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "ui",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "mapper",
            fileName = "StubUiMapper.kt"
        ) { buildPresentationToUiMapperKotlinFile(featurePackageName) }
    }

    private fun writeUiDiFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "ui",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "di",
            fileName = "${featureName.capitalized}Dependencies.kt"
        ) { buildUiDiKotlinFile(projectNamespace, featurePackageName, featureName) }
    }

    private fun writeUiScreenFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String,
        featureName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "ui",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "view",
            fileName = "${featureName.capitalized}Screen.kt"
        ) { buildUiScreenKotlinFile(projectNamespace, featurePackageName, featureName) }
    }
}
