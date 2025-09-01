package com.mitteloupe.cag.core.content

const val USE_CASE_PACKAGE_SUFFIX = ".domain.usecase"

fun buildDomainUseCaseKotlinFile(
    projectNamespace: String,
    featurePackageName: String,
    useCaseName: String = "PerformActionUseCase",
    repositoryName: String?
): String {
    return """package $featurePackageName$USE_CASE_PACKAGE_SUFFIX

import ${projectNamespace}architecture.domain.usecase.UseCase
import $featurePackageName.domain.model.$DOMAIN_MODEL_NAME
${
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
) : UseCase<$DOMAIN_MODEL_NAME, $DOMAIN_MODEL_NAME> {
    override fun execute(input: $DOMAIN_MODEL_NAME, onResult: ($DOMAIN_MODEL_NAME) -> Unit) {
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
