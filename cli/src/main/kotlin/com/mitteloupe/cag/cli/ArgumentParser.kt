package com.mitteloupe.cag.cli

private const val NEW_FEATURE_FLAG_LONG = "--new-feature"
private const val NEW_FEATURE_FLAG_SHORT = "-nf"
private const val HELP_FLAG_LONG = "--help"
private const val HELP_FLAG_SHORT = "-h"
private const val NEW_DATA_SOURCE_FLAG_LONG = "--new-datasource"
private const val NEW_DATA_SOURCE_FLAG_SHORT = "-nds"
private const val PACKAGE_FLAG_LONG = "--package"
private const val PACKAGE_FLAG_SHORT = "-p"

class ArgumentParser {
    fun isHelpRequested(arguments: Array<String>): Boolean =
        arguments.isNotEmpty() && arguments.any { argument -> argument.hasFlag(HELP_FLAG_LONG, HELP_FLAG_SHORT) }

    fun parseFeatureNames(args: Array<String>): List<String> {
        return parseNames(args, NEW_FEATURE_FLAG_LONG, NEW_FEATURE_FLAG_SHORT)
    }

    fun parseDataSourceNames(args: Array<String>): List<String> {
        return parseNames(args, NEW_DATA_SOURCE_FLAG_LONG, NEW_DATA_SOURCE_FLAG_SHORT).map(::ensureDataSourceSuffix)
    }

    fun parseFeaturePackages(args: Array<String>): List<String?> {
        if (args.isEmpty()) {
            return emptyList()
        }

        val packages = mutableListOf<String?>()
        var index = 0
        while (index < args.size) {
            val arg = args[index]
            when {
                arg == NEW_FEATURE_FLAG_LONG || arg.startsWith("$NEW_FEATURE_FLAG_LONG=") ||
                    arg == NEW_FEATURE_FLAG_SHORT || arg.startsWith(NEW_FEATURE_FLAG_SHORT) -> {
                    packages.add(null)
                }
                arg == PACKAGE_FLAG_LONG -> {
                    val newIndex = consumeNextNonFlagForPackage(args, index, packages)
                    if (newIndex != index + 1) {
                        index = newIndex
                        continue
                    }
                }
                arg.startsWith("$PACKAGE_FLAG_LONG=") -> {
                    setLastPackageFromEquals(arg, packages)
                }
                arg == PACKAGE_FLAG_SHORT -> {
                    val newIndex = consumeNextNonFlagForPackage(args, index, packages)
                    if (newIndex != index + 1) {
                        index = newIndex
                        continue
                    }
                }
                arg.startsWith(PACKAGE_FLAG_SHORT) -> {
                    setLastPackageFromShortAttached(arg, packages)
                }
            }
            index += 1
        }

        return packages
    }

    private fun ensureDataSourceSuffix(name: String): String {
        val trimmed = name.trim()
        return if (trimmed.endsWith("DataSource")) trimmed else "${trimmed}DataSource"
    }

    private fun parseNames(
        arguments: Array<String>,
        longFlag: String,
        shortFlag: String
    ): List<String> {
        if (arguments.isEmpty()) {
            return emptyList()
        }

        val values = mutableListOf<String>()
        var index = 0
        while (index < arguments.size) {
            val argument = arguments[index]
            when {
                argument == longFlag -> {
                    val newIndex = consumeNextNonFlag(arguments, index) { value -> values.add(value) }
                    if (newIndex != index + 1) {
                        index = newIndex
                        continue
                    }
                }
                argument.startsWith("$longFlag=") -> {
                    handleValueAfterEquals(argument) { value -> values.add(value) }
                }
                argument == shortFlag -> {
                    val newIndex = consumeNextNonFlag(arguments, index) { value -> values.add(value) }
                    if (newIndex != index + 1) {
                        index = newIndex
                        continue
                    }
                }
                argument.startsWith(shortFlag) -> {
                    handleShortFlagTail(argument, shortFlag) { value -> values.add(value) }
                }
            }
            index += 1
        }
        return values
    }
}

private inline fun consumeNextNonFlag(
    args: Array<String>,
    currentIndex: Int,
    crossinline onValue: (String) -> Unit
): Int {
    val next = args.getOrNull(currentIndex + 1)
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

private fun String.hasFlag(
    longName: String,
    shortName: String
): Boolean = this == longName || this == shortName

private inline fun handleValueAfterEquals(
    arg: String,
    crossinline onValue: (String) -> Unit
) {
    val value = arg.extractValueAfterEquals()
    if (value.isNotEmpty()) {
        onValue(value)
    }
}

private inline fun handleShortFlagTail(
    arg: String,
    shortFlag: String,
    crossinline onValue: (String) -> Unit
) {
    val tail = arg.removePrefix(shortFlag)
    val value = tail.extractValueFromFlagTail()
    if (value.isNotEmpty()) {
        onValue(value)
    }
}

private fun consumeNextNonFlagForPackage(
    args: Array<String>,
    currentIndex: Int,
    packages: MutableList<String?>
): Int {
    val next = args.getOrNull(currentIndex + 1)
    if (packages.isNotEmpty()) {
        val lastIndex = packages.lastIndex
        packages[lastIndex] = if (next != null && !next.startsWith("-")) next.trim().ifEmpty { null } else null
    }
    return currentIndex +
        if (next != null && !next.startsWith("-")) {
            2
        } else {
            1
        }
}

private fun setLastPackageFromEquals(
    arg: String,
    packages: MutableList<String?>
) {
    if (packages.isNotEmpty()) {
        val value = arg.extractValueAfterEquals()
        val lastIndex = packages.lastIndex
        packages[lastIndex] = value.ifEmpty { null }
    }
}

private fun setLastPackageFromShortAttached(
    arg: String,
    packages: MutableList<String?>
) {
    if (packages.isNotEmpty()) {
        val tail = arg.removePrefix(PACKAGE_FLAG_SHORT)
        val value = tail.extractValueFromFlagTail()
        val lastIndex = packages.lastIndex
        packages[lastIndex] = value.ifEmpty { null }
    }
}
