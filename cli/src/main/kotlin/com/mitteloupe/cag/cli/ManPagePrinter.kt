package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.help.HelpContent

object ManPagePrinter {
    fun printManPage(topic: String?) {
        val name = "cag"
        val section = 1
        val title = "Clean Architecture Generator"
        val normalized = topic?.lowercase()?.trim()

        val sections = HelpContent.helpSections()
        val topicsToRender =
            if (normalized.isNullOrEmpty() || normalized == "all" || normalized == "overview") {
                sections
            } else {
                sections.filterKeys { it == normalized }
            }

        val content =
            buildString {
                appendLine(".TH ${name.uppercase()} $section \"\" \"\" \"cag\"")
                appendLine(".SH NAME")
                appendLine("$name - $title")
                appendLine(".SH SYNOPSIS")
                appendLine(".PP")
                appendLine(HelpContent.USAGE_SYNTAX)
                appendLine(".SH DESCRIPTION")
                appendLine("cag generates Android Clean Architecture scaffolding and components.")
                topicsToRender.forEach { (key, value) ->
                    appendLine(".SH ${key.uppercase()}")
                    value.lines().forEach { line ->
                        if (line.isBlank()) {
                            appendLine(".PP")
                        } else {
                            appendLine(line)
                        }
                    }
                }
            }
        println(content)
    }
}
