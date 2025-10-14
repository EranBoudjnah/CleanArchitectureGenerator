package com.mitteloupe.cag.core.generation.layer

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.USE_CASE_PACKAGE_SUFFIX
import com.mitteloupe.cag.core.content.buildDomainModelKotlinFile
import com.mitteloupe.cag.core.content.buildDomainRepositoryKotlinFile
import com.mitteloupe.cag.core.content.buildDomainUseCaseKotlinFile
import com.mitteloupe.cag.core.generation.KotlinFileCreator
import com.mitteloupe.cag.core.generation.structure.PackageNameDeriver
import java.io.File

private const val USE_CASE_BASE_NAME = "PerformAction"
private const val USE_CASE_NAME = "${USE_CASE_BASE_NAME}UseCase"
private const val REPOSITORY_NAME = "${USE_CASE_BASE_NAME}Repository"

class DomainLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator
) {
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
            PackageNameDeriver
                .derivePackageNameForDirectory(destinationDirectory)
                ?.replace(packageSuffixRegex.toRegex(), "")
                ?: throw GenerationException(
                    "Could not determine package name from directory: " + destinationDirectory.absolutePath
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
            fileName = "$useCaseName.kt"
        ) {
            buildDomainUseCaseKotlinFile(
                projectNamespace = projectNamespace,
                featurePackageName = featurePackageName,
                useCaseName = useCaseName,
                repositoryName = null,
                inputDataType = inputDataType,
                outputDataType = outputDataType
            )
        }
    }

    private fun writeDomainUseCaseFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "usecase",
            fileName = "$USE_CASE_NAME.kt"
        ) {
            buildDomainUseCaseKotlinFile(
                projectNamespace = projectNamespace,
                featurePackageName = featurePackageName,
                useCaseName = USE_CASE_NAME,
                repositoryName = REPOSITORY_NAME
            )
        }
    }

    private fun writeDomainRepositoryInterface(
        featureRoot: File,
        featurePackageName: String
    ) {
        kotlinFileCreator.writeKotlinFileInLayer(
            featureRoot = featureRoot,
            layer = "domain",
            featurePackageName = featurePackageName,
            relativePackageSubPath = "repository",
            fileName = "$REPOSITORY_NAME.kt"
        ) { buildDomainRepositoryKotlinFile(featurePackageName, REPOSITORY_NAME) }
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
            fileName = "StubDomainModel.kt"
        ) { buildDomainModelKotlinFile(featurePackageName) }
    }

    private fun extractProjectNamespace(packageName: String): String {
        val segments = packageName.split(".")
        return if (segments.size >= 3) {
            segments.take(3).joinToString(".")
        } else {
            packageName
        }
    }
}
