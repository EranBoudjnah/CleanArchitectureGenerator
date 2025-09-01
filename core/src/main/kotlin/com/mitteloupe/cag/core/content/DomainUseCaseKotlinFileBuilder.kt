package com.mitteloupe.cag.core.content

const val USE_CASE_PACKAGE_SUFFIX = ".domain.usecase"

fun buildDomainUseCaseKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    useCaseName: String = "PerformActionUseCase",
    repositoryName: String?,
    inputDataType: String? = null,
    outputDataType: String? = null
): String {
    return """package $featurePackageName$USE_CASE_PACKAGE_SUFFIX

import ${projectNamespace}architecture.domain.usecase.UseCase
${
        if (inputDataType == null && outputDataType == null) {
            "import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME\n"
        } else {
            ""
        }
    }${
        if (repositoryName == null) {
            ""
        } else {
            "import $featurePackageName.domain.repository.$repositoryName\n"
        }
    }
class $useCaseName(${
        if (repositoryName == null) {
            ""
        } else {
            "\nprivate val performExampleRepository: $repositoryName\n"
        }
    }    
) : UseCase<${inputDataType ?: DOMAIN_MODEL_NAME}, ${outputDataType ?: DOMAIN_MODEL_NAME}> {
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
