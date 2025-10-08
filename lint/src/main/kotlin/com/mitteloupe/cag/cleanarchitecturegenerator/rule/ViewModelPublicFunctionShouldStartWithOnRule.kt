package com.mitteloupe.cag.cleanarchitecturegenerator.rule

object ViewModelPublicFunctionShouldStartWithOnRule {
    private val validFunctionNameRegex = Regex("^on[A-Z0-9].*$")

    fun isValid(
        className: String?,
        functionName: String,
        isPublic: Boolean
    ): Boolean =
        !isPublic ||
            className?.endsWith("ViewModel") != true ||
            functionName.matches(validFunctionNameRegex)

    fun violationMessage(functionName: String): String =
        "ViewModel public function name '$functionName' should start with 'on'"
}
