package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.USE_CASE_PACKAGE_SUFFIX
import com.mitteloupe.cag.core.content.buildDomainModelKotlinFile
import com.mitteloupe.cag.core.content.buildDomainRepositoryKotlinFile
import com.mitteloupe.cag.core.content.buildDomainUseCaseKotlinFile
import com.mitteloupe.cag.core.generation.structure.PackageNameDeriver
import java.io.File

class DomainLayerContentGenerator(private val kotlinFileCreator: KotlinFileCreator) {
    fun generateDomainLayer(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String
    ) {
        writeDomainModelFile(featureRoot, featurePackageName)
        writeDomainRepositoryInterface(featureRoot, featurePackageName)
        writeDomainUseCaseFile(featureRoot, projectNamespace, featurePackageName)
    }

    fun generateUseCase(
        destinationDirectory: File,
        useCaseName: String,
        inputDataType: String? = null,
        outputDataType: String? = null
    ) {
        val packageSuffixRegex = USE_CASE_PACKAGE_SUFFIX.replace(".", "\\.") + "$"
        val packageName =
            PackageNameDeriver.derivePackageNameForDirectory(destinationDirectory)
                ?.replace(packageSuffixRegex.toRegex(), "")
                ?: throw GenerationException(
                    "Could not determine package name from directory: " +
                        destinationDirectory.absolutePath
                )

        writeDomainUseCaseFile(
            targetDirectory = destinationDirectory,
            projectNamespace = extractProjectNamespace(packageName),
            featurePackageName = packageName,
            useCaseName = useCaseName,
            inputDataType = inputDataType,
            outputDataType = outputDataType
        )
    }

    private fun writeDomainUseCaseFile(
        targetDirectory: File,
        projectNamespace: String,
        featurePackageName: String,
        useCaseName: String,
        inputDataType: String? = null,
        outputDataType: String? = null
    ) {
        kotlinFileCreator.writeKotlinFileInDirectory(
            targetDirectory = targetDirectory,
            fileName = "$useCaseName.kt",
            content =
                buildDomainUseCaseKotlinFile(
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName,
                    useCaseName = useCaseName,
                    repositoryName = null,
                    inputDataType = inputDataType,
                    outputDataType = outputDataType
                )
        )
    }

    private fun writeDomainUseCaseFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String
    ) {
        val useCaseName = "PerformActionUseCase"
        val repositoryName = deriveRepositoryNameFromUseCaseName(useCaseName)

        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "usecase",
            fileName = "$useCaseName.kt",
            content =
                buildDomainUseCaseKotlinFile(
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName,
                    useCaseName = useCaseName,
                    repositoryName = repositoryName
                )
        )
    }

    private fun writeDomainRepositoryInterface(
        featureRoot: File,
        featurePackageName: String
    ) {
        val useCaseName = "PerformActionUseCase"
        val repositoryName = deriveRepositoryNameFromUseCaseName(useCaseName)
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "repository",
            fileName = "$repositoryName.kt",
            content =
                buildDomainRepositoryKotlinFile(featurePackageName, repositoryName)
        )
    }

    private fun writeDomainModelFile(
        featureRoot: File,
        featurePackageName: String
    ) {
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

    private fun deriveRepositoryNameFromUseCaseName(useCaseName: String): String = useCaseName.removeSuffix("UseCase") + "Repository"

    private fun extractProjectNamespace(packageName: String): String {
        val segments = packageName.split(".")
        return if (segments.size >= 3) {
            segments.take(3).joinToString(".")
        } else {
            packageName
        }
    }
}
