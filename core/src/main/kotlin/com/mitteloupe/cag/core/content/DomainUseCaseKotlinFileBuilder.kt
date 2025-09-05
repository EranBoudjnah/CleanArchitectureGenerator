package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.optimizeImports

const val USE_CASE_PACKAGE_SUFFIX = ".domain.usecase"

fun buildDomainUseCaseKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    useCaseName: String = "PerformActionUseCase",
    repositoryName: String?,
    inputDataType: String? = null,
    outputDataType: String? = null
): String {
    val imports = mutableListOf<String>()
    imports.add("import ${projectNamespace}architecture.domain.usecase.UseCase")

    if (inputDataType == null && outputDataType == null) {
        imports.add("import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME")
    }

    if (repositoryName != null) {
        imports.add("import $featurePackageName.domain.repository.$repositoryName")
    }

    return """package $featurePackageName$USE_CASE_PACKAGE_SUFFIX

${imports.joinToString("\n").optimizeImports()}
class $useCaseName(${
        if (repositoryName == null) {
            ""
        } else {
            "private val performExampleRepository: $repositoryName"
        }
    }) : UseCase<${inputDataType ?: DOMAIN_MODEL_NAME}, ${outputDataType ?: DOMAIN_MODEL_NAME}> {
    override fun execute(input: ${inputDataType ?: DOMAIN_MODEL_NAME}, onResult: (${outputDataType ?: DOMAIN_MODEL_NAME}) -> Unit) {
        onResult(${
        if (repositoryName == null) {
            "TODO(\"Evaluate result\")"
        } else {
            "performExampleRepository.perform(input)"
        }
    })
    }
}
"""
}
