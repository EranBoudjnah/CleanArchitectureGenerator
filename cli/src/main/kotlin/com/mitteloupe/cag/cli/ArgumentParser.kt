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
        if (arguments.isEmpty()) return emptyList()

        val isLongForm = determineForm(arguments, primaryLong, primaryShort)
        val primary = if (isLongForm) primaryLong else primaryShort
        val secondaryMap = secondaryFlags.associateBy { if (isLongForm) it.long else it.short }

        return parseArguments(arguments, primary, secondaryMap, isLongForm)
    }

    private fun determineForm(
        arguments: Array<String>,
        primaryLong: String,
        primaryShort: String
    ): Boolean {
        val firstPrimaryIndex = arguments.indexOfFirst { it in setOf(primaryLong, primaryShort) }
        return firstPrimaryIndex < 0 || arguments[firstPrimaryIndex] == primaryLong
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
                    parseInlineArgument(token, secondaryMap, isLongForm)?.let { (key, value) ->
                        hasEncounteredSecondaries = true
                        currentSecondaries[key] = value
                    }
                    if (token.startsWith("-") && !token.startsWith(primary)) {
                        hasEncounteredSecondaries = true
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
            secondaryFlags.filter { it.isMandatory && currentSecondaries[it.long]?.isNotEmpty() != true }

        if (missingFlags.isNotEmpty()) {
            val errorMessage =
                missingFlags.joinToString("\n") { flag ->
                    flag.missingErrorMessage.ifEmpty { "Missing mandatory flag: ${flag.long}" }
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
