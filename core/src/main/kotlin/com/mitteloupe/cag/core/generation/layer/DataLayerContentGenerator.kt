package com.mitteloupe.cag.core.generation.layer

import com.mitteloupe.cag.core.content.buildDataRepositoryKotlinFile
import com.mitteloupe.cag.core.content.capitalized
import com.mitteloupe.cag.core.generation.KotlinFileCreator
import java.io.File

class DataLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator
) {
    fun generate(
        featureRoot: File,
        featurePackageName: String,
        featureName: String
    ) {
        writeDataRepositoryFile(featureRoot, featurePackageName, featureName)
    }

    private fun writeDataRepositoryFile(
        featureRoot: File,
        featurePackageName: String,
        featureName: String,
        useCaseName: String = "PerformActionUseCase"
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "data",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "repository",
            fileName = "${featureName.capitalized}Repository.kt"
        ) {
            val repositoryName = deriveRepositoryNameFromUseCaseName(useCaseName)
            buildDataRepositoryKotlinFile(featurePackageName, featureName, repositoryName)
        }
    }

    private fun deriveRepositoryNameFromUseCaseName(useCaseName: String): String = useCaseName.removeSuffix("UseCase") + "Repository"
}
