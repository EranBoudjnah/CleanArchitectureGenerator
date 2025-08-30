package com.mitteloupe.cag.cli

private const val NEW_FEATURE_FLAG_LONG = "--new-feature="
private const val NEW_FEATURE_FLAG_SHORT = "-nf="
private const val HELP_FLAG_LONG = "--help"
private const val HELP_FLAG_SHORT = "-h"
private const val NEW_DATA_SOURCE_FLAG_LONG = "--new-datasource="
private const val NEW_DATA_SOURCE_FLAG_SHORT = "-nds="
private const val PACKAGE_FLAG_LONG = "--package="
private const val PACKAGE_FLAG_SHORT = "-p="

class ArgumentParser {
    fun isHelpRequested(arguments: Array<String>): Boolean =
        arguments.isNotEmpty() && arguments.any { argument -> argument.hasFlag(HELP_FLAG_LONG, HELP_FLAG_SHORT) }

    fun parseFeatureNames(args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return emptyList()
        }

        val features = mutableListOf<String>()
        args.forEach { arg ->
            when {
                arg.startsWith(NEW_FEATURE_FLAG_LONG) -> {
                    val value = arg.substringAfter(NEW_FEATURE_FLAG_LONG).trim()
                    if (value.isNotEmpty()) features.add(value)
                }
                arg.startsWith(NEW_FEATURE_FLAG_SHORT) -> {
                    val value = arg.substringAfter(NEW_FEATURE_FLAG_SHORT).trim()
                    if (value.isNotEmpty()) features.add(value)
                }
            }
        }
        return features
    }

    fun parseDataSourceNames(args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return emptyList()
        }

        val dataSources = mutableListOf<String>()
        args.forEach { arg ->
            when {
                arg.startsWith(NEW_DATA_SOURCE_FLAG_LONG) -> {
                    val value = arg.substringAfter(NEW_DATA_SOURCE_FLAG_LONG).trim()
                    if (value.isNotEmpty()) dataSources.add(value)
                }
                arg.startsWith(NEW_DATA_SOURCE_FLAG_SHORT) -> {
                    val value = arg.substringAfter(NEW_DATA_SOURCE_FLAG_SHORT).trim()
                    if (value.isNotEmpty()) dataSources.add(value)
                }
            }
        }
        return dataSources.map(::ensureDataSourceSuffix)
    }

    fun parseFeaturePackages(args: Array<String>): List<String?> {
        if (args.isEmpty()) {
            return emptyList()
        }

        val packages = mutableListOf<String?>()
        args.forEach { arg ->
            when {
                arg.startsWith(NEW_FEATURE_FLAG_LONG, NEW_FEATURE_FLAG_SHORT) -> {
                    packages.add(null)
                }
                arg.startsWith(PACKAGE_FLAG_LONG, PACKAGE_FLAG_SHORT) -> {
                    val value = arg.substringAfter("=").trim()
                    if (packages.isNotEmpty()) {
                        val lastIndex = packages.lastIndex
                        packages[lastIndex] = value.ifEmpty { null }
                    }
                }
            }
        }

        return packages
    }

    private fun ensureDataSourceSuffix(name: String): String {
        val trimmed = name.trim()
        return if (trimmed.endsWith("DataSource")) trimmed else "${trimmed}DataSource"
    }

    private fun String.hasFlag(
        longName: String,
        shortName: String
    ): Boolean = this == longName || this == shortName

    private fun String.startsWith(
        longName: String,
        shortName: String
    ): Boolean = startsWith(longName) || startsWith(shortName)
}
