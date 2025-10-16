package com.mitteloupe.cag.cli.help

import com.mitteloupe.cag.cli.help.HelpContent.KEY_NEW_ARCHITECTURE
import com.mitteloupe.cag.cli.help.HelpContent.KEY_NEW_DATASOURCE
import com.mitteloupe.cag.cli.help.HelpContent.KEY_NEW_FEATURE
import com.mitteloupe.cag.cli.help.HelpContent.KEY_NEW_PROJECT
import com.mitteloupe.cag.cli.help.HelpContent.KEY_NEW_USE_CASE
import com.mitteloupe.cag.cli.help.HelpContent.KEY_NEW_VIEW_MODEL
import com.mitteloupe.cag.cli.help.HelpContent.USAGE_SYNTAX

fun printUsageMessage() {
    println(
        """
        usage: $USAGE_SYNTAX

        Run with --help or -h for more options.
        """.trimIndent()
    )
}

fun printHelpMessage() {
    val helpSections = HelpContent.helpSections()

    println(
        """usage: $USAGE_SYNTAX

Note: You must use either long form (--flag) or short form (-f) arguments consistently throughout your command. Mixing both forms is not allowed.

Options:
${helpSections[KEY_NEW_PROJECT]?.body}
${helpSections[KEY_NEW_ARCHITECTURE]?.body}
${helpSections[KEY_NEW_FEATURE]?.body}
${helpSections[KEY_NEW_DATASOURCE]?.body}
${helpSections[KEY_NEW_USE_CASE]?.body}
${helpSections[KEY_NEW_VIEW_MODEL]?.body}
  --version | -v
      Show the current version
  --help, -h
      Show this help message and exit"""
    )
}

fun printHelpMessage(topic: String?) {
    val normalized = topic?.lowercase()?.trim()
    if (normalized.isNullOrEmpty() || normalized == "all" || normalized == "overview") {
        printHelpMessage()
        return
    }
    val sections = HelpContent.helpSections()
    val content = sections[normalized]
    if (content != null) {
        println(content)
    } else {
        println("Unknown help topic: $topic\nAvailable topics: ${sections.keys.sorted().joinToString(", ")}\n")
        printHelpMessage()
    }
}
