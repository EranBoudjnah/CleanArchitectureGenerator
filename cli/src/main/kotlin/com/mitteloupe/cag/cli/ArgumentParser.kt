package com.mitteloupe.cag.cli

data class SecondaryFlag(
    val long: String,
    val short: String,
    val isMandatory: Boolean = false,
    val missingErrorMessage: String = "",
    val isBoolean: Boolean = false
)

class ArgumentParser {
    fun parsePrimaryWithSecondaries(
        arguments: Array<String>,
        primaryLong: String,
        primaryShort: String,
        secondaryFlags: List<SecondaryFlag>
    ): List<Map<String, String>> {
        if (arguments.isEmpty()) {
            return emptyList()
        }

        val isLongForm = determineForm(arguments, primaryLong, primaryShort)
        if (isLongForm == null) {
            throw IllegalArgumentException("Invalid syntax: [${arguments.joinToString(", ")}]")
        }

        val primary = if (isLongForm) primaryLong else primaryShort
        val secondaryMap = secondaryFlags.associateBy { if (isLongForm) it.long else it.short }

        return parseArguments(arguments, primary, secondaryMap, isLongForm)
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
        secondaryMap: Map<String, SecondaryFlag>,
        isLongForm: Boolean
    ): List<Map<String, String>> {
        val results = mutableListOf<Map<String, String>>()
        var currentSecondaries = mutableMapOf<String, String>()
        var hasPrimary = false
        var hasEncounteredSecondaries = false

        fun finalizeCurrent() {
            if (hasPrimary || currentSecondaries.isNotEmpty()) {
                validateMandatoryFlags(currentSecondaries, secondaryMap.values)
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
                secondaryMap.containsKey(token) -> {
                    val secondary = secondaryMap.getValue(token)
                    hasEncounteredSecondaries = true
                    if (secondary.isBoolean) {
                        currentSecondaries[secondary.long] = ""
                        index++
                    } else {
                        index =
                            consumeValue(arguments, index) { value ->
                                currentSecondaries[secondary.long] = value
                            }
                    }
                }
                else -> {
                    val inlineResult = parseInlineArgument(token, secondaryMap, isLongForm)
                    if (inlineResult != null) {
                        hasEncounteredSecondaries = true
                        currentSecondaries[inlineResult.first] = inlineResult.second
                    } else if (token.startsWith("-") && !token.startsWith(primary)) {
                        hasEncounteredSecondaries = true
                        validateMixedForm(token, primary, isLongForm, secondaryMap)
                    }
                    index++
                }
            }
        }

        finalizeCurrent()
        return results
    }

    private fun validateMandatoryFlags(
        currentSecondaries: Map<String, String>,
        secondaryFlags: Collection<SecondaryFlag>
    ) {
        val missingFlags =
            secondaryFlags.filter { flag ->
                flag.isMandatory &&
                    if (flag.isBoolean) {
                        !currentSecondaries.containsKey(flag.long)
                    } else {
                        currentSecondaries[flag.long]?.isNotEmpty() != true
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
        secondaryMap: Map<String, SecondaryFlag>
    ) {
        val matchingSecondary =
            secondaryMap.values.find { flag ->
                if (isLongForm) {
                    token == flag.short
                } else {
                    token == flag.long
                }
            }

        if (matchingSecondary != null) {
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

        val inlineMatchingSecondary =
            secondaryMap.values.find { flag ->
                if (isLongForm) {
                    token.startsWith(flag.short) && (token == flag.short || token.startsWith("${flag.short}="))
                } else {
                    token.startsWith(flag.long) && (token == flag.long || token.startsWith("${flag.long}="))
                }
            }

        if (inlineMatchingSecondary != null) {
            val errorMessage =
                if (isLongForm) {
                    "Cannot mix long form ($primary) with short form secondary flags " +
                        "(${inlineMatchingSecondary.short}). Use ${inlineMatchingSecondary.long} instead."
                } else {
                    "Cannot mix short form ($primary) with long form secondary flags " +
                        "(${inlineMatchingSecondary.long}). Use ${inlineMatchingSecondary.short} instead."
                }
            throw IllegalArgumentException(errorMessage)
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
    secondaryMap: Map<String, SecondaryFlag>,
    isLongForm: Boolean
): Pair<String, String>? {
    val matchingKey =
        if (isLongForm) {
            secondaryMap.keys.find { token.startsWith("$it=") }
        } else {
            secondaryMap.keys.find { token.startsWith(it) }
        } ?: return null

    val secondary = secondaryMap.getValue(matchingKey)

    val isMixedForm =
        if (isLongForm) {
            matchingKey.startsWith("-") && !matchingKey.startsWith("--")
        } else {
            matchingKey.startsWith("--")
        }

    if (isMixedForm) {
        return null
    }

    val value =
        if (isLongForm) {
            token.substringAfter("=").trim()
        } else {
            token.removePrefix(matchingKey).let {
                if (it.startsWith("=")) it.removePrefix("=") else it
            }.trim()
        }

    return if (value.isNotEmpty()) {
        Pair(secondary.long, value)
    } else {
        null
    }
}
