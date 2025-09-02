package com.mitteloupe.cag.cli

data class SecondaryFlag(
    val long: String,
    val short: String,
    val isMandatory: Boolean = false,
    val missingErrorMessage: String = ""
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

        val secondaryByLong = secondaryFlags.associateBy { it.long }
        val secondaryByShort = secondaryFlags.associateBy { it.short }

        val results = mutableListOf<Map<String, String>>()
        var currentSecondaries = mutableMapOf<String, String>()

        fun finalizeCurrentIfNeeded() {
            if (currentSecondaries.isNotEmpty()) {
                validateMandatoryFlags(currentSecondaries, secondaryFlags)
                results.add(currentSecondaries.toMap())
            }
            currentSecondaries = mutableMapOf()
        }

        var index = 0
        while (index < arguments.size) {
            val token = arguments[index]
            when {
                token == primaryLong || token == primaryShort -> {
                    finalizeCurrentIfNeeded()
                    index++
                }
                secondaryByLong.containsKey(token) -> {
                    val secondary = secondaryByLong.getValue(token)
                    val newIndex =
                        consumeNextNonFlag(arguments, index) { value ->
                            currentSecondaries[secondary.long] = value
                        }
                    index = newIndex
                }
                secondaryByLong.keys.any { token.startsWith("$it=") } -> {
                    val matched = secondaryByLong.keys.first { token.startsWith("$it=") }
                    val secondary = secondaryByLong.getValue(matched)
                    handleValueAfterEquals(token) { value ->
                        currentSecondaries[secondary.long] = value
                    }
                    index++
                }
                secondaryByShort.containsKey(token) -> {
                    val secondary = secondaryByShort.getValue(token)
                    val newIndex =
                        consumeNextNonFlag(arguments, index) { value ->
                            currentSecondaries[secondary.long] = value
                        }
                    index = newIndex
                }
                secondaryByShort.keys.any { token.startsWith(it) } -> {
                    val matched = secondaryByShort.keys.first { token.startsWith(it) }
                    handleShortFlagTail(token, matched) { value ->
                        val longKey = secondaryByShort.getValue(matched).long
                        currentSecondaries[longKey] = value
                    }
                    index++
                }
                else -> index++
            }
        }

        validateMandatoryFlags(currentSecondaries, secondaryFlags)
        if (currentSecondaries.isNotEmpty()) {
            results.add(currentSecondaries.toMap())
        }

        return results
    }

    private fun validateMandatoryFlags(
        currentSecondaries: Map<String, String>,
        secondaryFlags: List<SecondaryFlag>
    ) {
        val mandatoryFlags = secondaryFlags.filter { it.isMandatory }
        val missingFlags =
            mandatoryFlags.filter { flag ->
                currentSecondaries[flag.long]?.isNotEmpty() != true
            }

        if (missingFlags.isNotEmpty()) {
            val errorMessage =
                missingFlags.joinToString("\n") { flag ->
                    flag.missingErrorMessage.ifEmpty { "Missing mandatory flag: ${flag.long}" }
                }
            throw IllegalArgumentException(errorMessage)
        }
    }
}

private inline fun consumeNextNonFlag(
    arguments: Array<String>,
    currentIndex: Int,
    crossinline onValue: (String) -> Unit
): Int {
    val next = arguments.getOrNull(currentIndex + 1)
    if (next != null && !next.startsWith("-")) {
        val value = next.trim()
        if (value.isNotEmpty()) onValue(value)
        return currentIndex + 2
    }
    return currentIndex + 1
}

private fun String.extractValueFromFlagTail(): String =
    if (startsWith("=")) {
        removePrefix("=")
    } else {
        this
    }.trim()

private fun String.extractValueAfterEquals(): String = substringAfter("=").trim()

private inline fun handleValueAfterEquals(
    argument: String,
    crossinline onValue: (String) -> Unit
) {
    val value = argument.extractValueAfterEquals()
    if (value.isNotEmpty()) {
        onValue(value)
    }
}

private inline fun handleShortFlagTail(
    argument: String,
    shortFlag: String,
    crossinline onValue: (String) -> Unit
) {
    val tail = argument.removePrefix(shortFlag)
    val value = tail.extractValueFromFlagTail()
    if (value.isNotEmpty()) {
        onValue(value)
    }
}
