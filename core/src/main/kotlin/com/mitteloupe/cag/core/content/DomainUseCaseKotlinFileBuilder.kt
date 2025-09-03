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
    return """package $featurePackageName$USE_CASE_PACKAGE_SUFFIX

${
        """
import ${projectNamespace}architecture.domain.usecase.UseCase
${
            if (inputDataType == null && outputDataType == null) {
                "import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME"
            } else {
                ""
            }
        }${
            if (repositoryName == null) {
                ""
            } else {
                "import $featurePackageName.domain.repository.$repositoryName"
            }
        }
""".optimizeImports()
    }
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
