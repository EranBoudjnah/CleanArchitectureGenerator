package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildDataRepositoryKotlinFile
import com.mitteloupe.cag.core.content.capitalized
import java.io.File

class DataLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator = KotlinFileCreator()
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
        val repositoryName = deriveRepositoryNameFromUseCaseName(useCaseName)
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "data",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "repository",
            fileName = "${featureName.capitalized}Repository.kt",
            content = buildDataRepositoryKotlinFile(featurePackageName, featureName, repositoryName)
        )
    }

    private fun deriveRepositoryNameFromUseCaseName(useCaseName: String): String = useCaseName.removeSuffix("UseCase") + "Repository"
}
