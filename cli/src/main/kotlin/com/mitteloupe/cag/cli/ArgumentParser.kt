package com.mitteloupe.cag.cli

data class SecondaryFlag(val long: String, val short: String)

class ArgumentParser {
    fun parsePrimaryWithSecondaries(
        arguments: Array<String>,
        primaryLong: String,
        primaryShort: String,
        secondaryFlags: List<SecondaryFlag>
    ): List<Pair<String, Map<String, String>>> {
        if (arguments.isEmpty()) {
            return emptyList()
        }

        val secondaryByLong = secondaryFlags.associateBy { it.long }
        val secondaryByShort = secondaryFlags.associateBy { it.short }

        val results = mutableListOf<Pair<String, Map<String, String>>>()
        var currentPrimaryValue: String? = null
        var currentSecondaries = mutableMapOf<String, String>()

        fun finalizeCurrentIfNeeded() {
            val primaryValue = currentPrimaryValue
            if (primaryValue != null) {
                results.add(primaryValue to currentSecondaries.toMap())
            }
            currentPrimaryValue = null
            currentSecondaries = mutableMapOf()
        }

        var index = 0
        while (index < arguments.size) {
            val token = arguments[index]
            when {
                token == primaryLong -> {
                    val newIndex =
                        consumeNextNonFlag(arguments, index) { value ->
                            finalizeCurrentIfNeeded()
                            currentPrimaryValue = value
                        }
                    index = newIndex
                    continue
                }
                token.startsWith("$primaryLong=") -> {
                    handleValueAfterEquals(token) { value ->
                        finalizeCurrentIfNeeded()
                        currentPrimaryValue = value
                    }
                }
                token == primaryShort -> {
                    val newIndex =
                        consumeNextNonFlag(arguments, index) { value ->
                            finalizeCurrentIfNeeded()
                            currentPrimaryValue = value
                        }
                    index = newIndex
                    continue
                }
                token.startsWith(primaryShort) -> {
                    handleShortFlagTail(token, primaryShort) { value ->
                        finalizeCurrentIfNeeded()
                        currentPrimaryValue = value
                    }
                }
                secondaryByLong.containsKey(token) -> {
                    val secondary = secondaryByLong.getValue(token)
                    val newIndex =
                        consumeNextNonFlag(arguments, index) { value ->
                            if (currentPrimaryValue != null) {
                                currentSecondaries[secondary.long] = value
                            }
                        }
                    index = newIndex
                    continue
                }
                secondaryByLong.keys.any { token.startsWith("$it=") } -> {
                    val matched = secondaryByLong.keys.first { token.startsWith("$it=") }
                    handleValueAfterEquals(token) { value ->
                        if (currentPrimaryValue != null) {
                            currentSecondaries[matched] = value
                        }
                    }
                }
                secondaryByShort.containsKey(token) -> {
                    val secondary = secondaryByShort.getValue(token)
                    val newIndex =
                        consumeNextNonFlag(arguments, index) { value ->
                            if (currentPrimaryValue != null) {
                                currentSecondaries[secondary.long] = value
                            }
                        }
                    index = newIndex
                    continue
                }
                secondaryByShort.keys.any { token.startsWith(it) } -> {
                    val matched = secondaryByShort.keys.first { token.startsWith(it) }
                    handleShortFlagTail(token, matched) { value ->
                        if (currentPrimaryValue != null) {
                            val longKey = secondaryByShort.getValue(matched).long
                            currentSecondaries[longKey] = value
                        }
                    }
                }
            }
            index += 1
        }

        if (currentPrimaryValue != null) {
            results.add(currentPrimaryValue!! to currentSecondaries.toMap())
        }

        return results
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
