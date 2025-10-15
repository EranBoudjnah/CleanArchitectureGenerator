package com.mitteloupe.cag.cli.argument

import com.mitteloupe.cag.cli.flag.FlagOption
import com.mitteloupe.cag.cli.flag.PrimaryFlag
import com.mitteloupe.cag.cli.flag.SecondaryFlag

class ArgumentParser {
    fun parsePrimaryWithSecondaries(
        arguments: Array<String>,
        primaryFlag: PrimaryFlag
    ): List<Map<FlagOption, String>> {
        if (arguments.isEmpty()) {
            return emptyList()
        }

        val isLongForm = determineForm(arguments, primaryFlag.long, primaryFlag.short) ?: return emptyList()

        val primary = if (isLongForm) primaryFlag.long else primaryFlag.short

        return parseArguments(
            arguments = arguments,
            primary = primary,
            secondaryFlags = primaryFlag.secondaryFlags,
            isLongForm = isLongForm
        )
    }

    private fun determineForm(
        arguments: Array<String>,
        primaryLong: String,
        primaryShort: String
    ): Boolean? {
        val firstPrimaryIndex = arguments.indexOfFirst { it in setOf(primaryLong, primaryShort) }
        return if (firstPrimaryIndex < 0) {
            null
        } else {
            arguments[firstPrimaryIndex] == primaryLong
        }
    }

    private fun parseArguments(
        arguments: Array<String>,
        primary: String,
        secondaryFlags: Set<SecondaryFlag>,
        isLongForm: Boolean
    ): List<Map<FlagOption, String>> {
        val results = mutableListOf<Map<FlagOption, String>>()
        var currentSecondaries = mutableMapOf<FlagOption, String>()
        var hasPrimary = false
        var hasEncounteredSecondaries = false

        fun finalizeCurrent() {
            if (hasPrimary || currentSecondaries.isNotEmpty()) {
                validateMandatoryFlags(currentSecondaries, secondaryFlags)
                if (currentSecondaries.isNotEmpty()) {
                    results.add(currentSecondaries.toMap())
                } else if (hasPrimary && !hasEncounteredSecondaries) {
                    results.add(currentSecondaries.toMap())
                }
            }
            currentSecondaries = mutableMapOf()
            @Suppress("AssignedValueIsNeverRead")
            hasPrimary = false
            @Suppress("AssignedValueIsNeverRead")
            hasEncounteredSecondaries = false
        }

        var index = 0
        while (index < arguments.size) {
            val token = arguments[index]
            when {
                token == primary -> {
                    finalizeCurrent()
                    hasPrimary = true
                    index++
                }
                secondaryFlags.any { token == if (isLongForm) it.long else it.short } -> {
                    val secondary = secondaryFlags.first { token == if (isLongForm) it.long else it.short }
                    hasEncounteredSecondaries = true
                    if (secondary.isBoolean) {
                        currentSecondaries[secondary.option] = ""
                        index++
                    } else {
                        index =
                            consumeValue(arguments, index) { value ->
                                currentSecondaries[secondary.option] = value
                            }
                    }
                }
                else -> {
                    val inlineResult = parseInlineArgument(token, secondaryFlags, isLongForm)
                    if (inlineResult != null) {
                        hasEncounteredSecondaries = true
                        currentSecondaries[inlineResult.first] = inlineResult.second
                    } else if (token.startsWith("-") && !token.startsWith(primary)) {
                        hasEncounteredSecondaries = true
                        validateMixedForm(token, primary, isLongForm, secondaryFlags)
                    }
                    index++
                }
            }
        }

        finalizeCurrent()
        return results
    }

    private fun validateMandatoryFlags(
        currentSecondaries: Map<FlagOption, String>,
        secondaryFlags: Collection<SecondaryFlag>
    ) {
        val missingFlags =
            secondaryFlags.filter { flag ->
                flag.isMandatory &&
                    if (flag.isBoolean) {
                        !currentSecondaries.containsKey(flag.option)
                    } else {
                        currentSecondaries[flag.option]?.isNotEmpty() != true
                    }
            }

        if (missingFlags.isNotEmpty()) {
            val errorMessage =
                missingFlags.joinToString("\n") { flag ->
                    flag.missingErrorMessage.ifEmpty { "Missing mandatory flag: ${flag.long}" }
                }
            throw IllegalArgumentException(errorMessage)
        }
    }

    private fun validateMixedForm(
        token: String,
        primary: String,
        isLongForm: Boolean,
        secondaryFlags: Set<SecondaryFlag>
    ) {
        fun mixedFormError(
            isLongForm: Boolean,
            primary: String,
            matchingSecondary: SecondaryFlag
        ): Nothing {
            val errorMessage =
                if (isLongForm) {
                    "Cannot mix long form ($primary) with short form secondary flags " +
                        "(${matchingSecondary.short}). Use ${matchingSecondary.long} instead."
                } else {
                    "Cannot mix short form ($primary) with long form secondary flags " +
                        "(${matchingSecondary.long}). Use ${matchingSecondary.short} instead."
                }
            throw IllegalArgumentException(errorMessage)
        }

        secondaryFlags
            .firstOrNull { flag -> token == if (isLongForm) flag.short else flag.long }
            ?.let { matchingSecondary -> mixedFormError(isLongForm, primary, matchingSecondary) }

        val inlineMatchingSecondary =
            secondaryFlags.firstOrNull { flag ->
                if (isLongForm) {
                    token.startsWith(flag.short) && (token == flag.short || token.startsWith("${flag.short}="))
                } else {
                    token.startsWith(flag.long) && (token == flag.long || token.startsWith("${flag.long}="))
                }
            }

        if (inlineMatchingSecondary != null) {
            mixedFormError(isLongForm, primary, inlineMatchingSecondary)
        }
    }
}

private inline fun consumeValue(
    arguments: Array<String>,
    currentIndex: Int,
    crossinline onValue: (String) -> Unit
): Int {
    val next = arguments.getOrNull(currentIndex + 1)
    return if (next != null && !next.startsWith("-")) {
        val value = next.trim()
        if (value.isNotEmpty()) onValue(value)
        currentIndex + 2
    } else {
        currentIndex + 1
    }
}

private fun parseInlineArgument(
    token: String,
    secondaryFlags: Set<SecondaryFlag>,
    isLongForm: Boolean
): Pair<FlagOption, String>? {
    val matchingSecondaryFlag =
        if (isLongForm) {
            secondaryFlags.firstOrNull { token.startsWith("${it.long}=") }
        } else {
            secondaryFlags.firstOrNull { token.startsWith(it.short) }
        } ?: return null

    val secondaryFlagToken = matchingSecondaryFlag.value(isLongForm)

//    val isMixedForm =
//        if (isLongForm) {
//            matchingSecondaryFlag.startsWith("-") && !matchingSecondaryFlag.startsWith("--")
//        } else {
//            matchingSecondaryFlag.startsWith("--")
//        }
//
//    if (isMixedForm) {
//        return null
//    }

    val value =
        if (isLongForm) {
            token.substringAfter("=").trim()
        } else {
            token
                .removePrefix(secondaryFlagToken)
                .let { if (it.startsWith("=")) it.removePrefix("=") else it }
                .trim()
        }

    return if (value.isNotEmpty()) {
        Pair(matchingSecondaryFlag.option, value)
    } else {
        null
    }
}
