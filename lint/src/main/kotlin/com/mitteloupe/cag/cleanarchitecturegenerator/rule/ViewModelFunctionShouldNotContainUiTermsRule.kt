package com.mitteloupe.cag.cleanarchitecturegenerator.rule

import com.intellij.codeInsight.intention.FileModifier.SafeTypeForPreview

private val uiTermsRegex =
    "(${firstLetter("c")}lick(ed)?|${firstLetter("s")}croll(ed)?|${firstLetter("s")}wipe(d)?|${firstLetter("t")}ap(ped)?|${firstLetter("l")}ongPress(ed)?|${firstLetter("t")}ouch(ed)?)".toRegex()

private fun firstLetter(firstCharacter: String) =
    "((?<=\\w)${firstCharacter.uppercase()}|^${firstCharacter.lowercase()})"

object ViewModelFunctionShouldNotContainUiTermsRule {
    fun validate(
        className: String?,
        functionName: String
    ): Result {
        if (className?.endsWith("ViewModel") != true) {
            return Result.Valid
        }

        val uiTermsMatchResult = uiTermsRegex.find(functionName) ?: return Result.Valid

        return Result.Invalid(functionName = functionName, offendingValue = uiTermsMatchResult.value)
    }

    fun violationMessage(offendingValue: String): String =
        "Function name contains UI term '$offendingValue', consider replacing it with 'Action'"

    sealed interface Result {
        data object Valid : Result

        @SafeTypeForPreview
        data class Invalid(
            private val functionName: String,
            val offendingValue: String
        ) : Result {
            fun fixFunctionName(): String {
                val capitalizedA =
                    if (functionName.indexOf(offendingValue) == 0) {
                        "a"
                    } else {
                        "A"
                    }
                return functionName.replaceFirst(offendingValue.toRegex(RegexOption.IGNORE_CASE), "${capitalizedA}ction")
            }
        }
    }
}
