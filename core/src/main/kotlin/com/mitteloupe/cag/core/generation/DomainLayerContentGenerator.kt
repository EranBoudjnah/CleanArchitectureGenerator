package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import com.mitteloupe.cag.core.content.USE_CASE_PACKAGE_SUFFIX
import com.mitteloupe.cag.core.content.buildDomainModelKotlinFile
import com.mitteloupe.cag.core.content.buildDomainRepositoryKotlinFile
import com.mitteloupe.cag.core.content.buildDomainUseCaseKotlinFile
import java.io.File

class DomainLayerContentGenerator(
    private val kotlinFileCreator: KotlinFileCreator = KotlinFileCreator()
) {
    fun generateDomainLayer(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String
    ): String? {
        writeDomainModelFile(featureRoot, featurePackageName)?.let { return it }
        writeDomainRepositoryInterface(featureRoot, featurePackageName)?.let { return it }
        writeDomainUseCaseFile(featureRoot, projectNamespace, featurePackageName)?.let { return it }
        return null
    }

    fun generateUseCase(
        destinationDirectory: File,
        useCaseName: String,
        inputDataType: String? = null,
        outputDataType: String? = null
    ): String? {
        val packageSuffixRegex = USE_CASE_PACKAGE_SUFFIX.replace(".", "\\.") + "$"
        val packageName =
            derivePackageNameForDirectory(destinationDirectory)
                ?.replace(packageSuffixRegex.toRegex(), "")
                ?: return "${ERROR_PREFIX}Could not determine package name from directory: " +
                    destinationDirectory.absolutePath

        return writeDomainUseCaseFile(
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
    ): String? {
        return kotlinFileCreator.writeKotlinFileInLayer(
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
    ): String? {
        val useCaseName = "PerformActionUseCase"
        val repositoryName = deriveRepositoryNameFromUseCaseName(useCaseName)

        return kotlinFileCreator.writeKotlinFileInLayer(
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
    ): String? {
        val repositoryName = "PerformExampleRepository"
        return kotlinFileCreator.writeKotlinFileInLayer(
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

    private fun deriveRepositoryNameFromUseCaseName(useCaseName: String): String = useCaseName.removeSuffix("UseCase") + "Repository"

    private fun derivePackageNameForDirectory(directory: File): String? {
        val absolutePath = directory.absolutePath
        val marker =
            listOf("src/main/java", "src/main/kotlin").firstOrNull { absolutePath.contains(it) }
                ?: return null
        val afterMarker = absolutePath.substringAfter(marker).trimStart(File.separatorChar)
        if (afterMarker.isEmpty()) {
            return null
        }
        val segments = afterMarker.split(File.separatorChar).filter { it.isNotEmpty() }
        return segments.joinToString(separator = ".")
    }

    private fun extractProjectNamespace(packageName: String): String {
        val segments = packageName.split(".")
        return if (segments.size >= 3) {
            segments.take(3).joinToString(".") + "."
        } else {
            "$packageName."
        }
    }
}
