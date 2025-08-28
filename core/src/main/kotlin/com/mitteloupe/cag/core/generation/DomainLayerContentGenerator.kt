package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildDomainModelKotlinFile
import com.mitteloupe.cag.core.content.buildDomainRepositoryKotlinFile
import com.mitteloupe.cag.core.content.buildDomainUseCaseKotlinFile
import java.io.File

class DomainLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator = KotlinFileCreator()
) {
    fun generate(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String
    ): String? {
        writeDomainModelFile(featureRoot, featurePackageName)?.let { return it }
        writeDomainRepositoryInterface(featureRoot, featurePackageName)?.let { return it }
        writeDomainUseCaseFile(featureRoot, projectNamespace, featurePackageName)?.let { return it }
        return null
    }

    private fun writeDomainUseCaseFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "usecase",
            fileName = "PerformActionUseCase.kt",
            content =
                buildDomainUseCaseKotlinFile(
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName
                )
        )

    private fun writeDomainRepositoryInterface(
        featureRoot: File,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "repository",
            fileName = "PerformExampleRepository.kt",
            content =
                buildDomainRepositoryKotlinFile(featurePackageName)
        )

    private fun writeDomainModelFile(
        featureRoot: File,
        featurePackageName: String
    ): String? =
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "model",
            fileName = "StubDomainModel.kt",
            content =
                buildDomainModelKotlinFile(featurePackageName)
        )
}
