package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.help.HelpContent

object ManPagePrinter {
    private const val BOLD = "\\fB"
    private const val ITALIC = "\\fI"
    private const val END = "\\fR"

    fun printManPage(topic: String?) {
        val command = "cag"
        val section = 1
        val explanation = "generate Clean Architecture Android code"
        val normalizedTopic = topic?.lowercase()?.trim()

        val sections = HelpContent.helpSections()
        val topicsToRender =
            if (normalizedTopic.isNullOrEmpty() || normalizedTopic == "all" || normalizedTopic == "overview") {
                sections
            } else {
                sections.filterKeys { it == normalizedTopic }
            }

        val content =
            buildString {
                titleHeader("${command.uppercase()} $section \"\" \"\" \"cag\"")
                appendNameSection(command = command, explanation = explanation)
                appendSynopsisSection(command = command)
                appendDescriptionSection(command = command)
                topicsToRender.forEach { (key, value) ->
                    appendSectionHeader(key.uppercase())
                    "$value".lines().forEach { line ->
                        if (line.isBlank()) {
                            appendBlankLine()
                        } else {
                            appendLine(line)
                        }
                    }
                }
                if (topicsToRender.isEmpty()) {
                    appendSectionHeader("ERROR")
                    appendLine("Unknown topic: \"$normalizedTopic\".")
                    appendBlankLine()
                    appendLine("Valid topics:")
                    sections.keys.sorted().forEach { topic ->
                        appendLine("  $topic")
                    }
                }
            }
        print(content)
    }

    private fun StringBuilder.titleHeader(header: String) {
        appendLine(".TH $header")
    }

    private fun StringBuilder.appendNameSection(
        command: String,
        explanation: String
    ) {
        appendSectionHeader("NAME")
        appendLine("$command - $explanation")
    }

    private fun StringBuilder.appendSynopsisSection(command: String) {
        appendSectionHeader("SYNOPSIS")
        appendLine(".B $command")
        appendLine(
            "[${bold("--new-project")} ${bold("--name=")}${italic("ProjectName")} " +
                "${bold("--package=")}${italic("PackageName")} [${bold("--no-compose")}] " +
                "[${bold("--ktlint")}] [${bold("--detekt")}] [${bold("--ktor")}] " +
                "[${bold("--retrofit")}] [${bold("--git")}] " +
                "[${bold("--dependency-injection=hilt")}|${bold("koin")}|${bold("none")}]]... " +
                "[${bold("--new-architecture")} [${bold("--no-compose")}] [${bold("--ktlint")}] " +
                "[${bold("--detekt")}] [${bold("--git")}] " +
                "[${bold("--dependency-injection=hilt")}|${bold("koin")}|${bold("none")}]]... " +
                "[${bold("--new-feature")} [${bold("--name=") + italic("FeatureName")} " +
                "[${bold("--package=") + italic("PackageName")}] [${bold("--ktlint")}] " +
                "[${bold("--detekt")}] [${bold("--git")}] " +
                "[${bold("--dependency-injection=hilt")}|${bold("koin")}|${bold("none")}]]... " +
                "[${bold("--new-datasource")} [${bold("--name=") + italic("DataSourceName")} " +
                "[${bold("--with=ktor")}|${bold("retrofit")}|${bold("ktor,retrofit")}] " +
                "[${bold("--git")}]]... [${bold("--new-use-case")} " +
                "[${bold("--name=") + italic("UseCaseName")} [${bold("--path=") + italic("TargetPath")}] " +
                "[${bold("--git")}]]... [${bold("--new-view-model")} " +
                "[${bold("--name=") + italic("ViewModelName")} [${bold("--path=") + italic("TargetPath")}] " +
                "[${bold("--git")}]]..."
        )
    }

    private fun StringBuilder.appendDescriptionSection(command: String) {
        appendSectionHeader("DESCRIPTION")
        appendLine(".B $command")
        appendLine("generates Android Clean Architecture scaffolding and components.")
    }

    private fun StringBuilder.appendSectionHeader(header: String) {
        appendLine(".SH $header")
    }

    private fun StringBuilder.appendBlankLine() {
        appendLine(".PP")
    }

    private fun bold(text: String) = "$BOLD$text$END"

    private fun italic(text: String) = "$ITALIC$text$END"
}
