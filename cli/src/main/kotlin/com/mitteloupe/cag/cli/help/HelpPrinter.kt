package com.mitteloupe.cag.cli.help

import com.mitteloupe.cag.cli.help.HelpContent.USAGE_SYNTAX

fun printUsageMessage() {
    println(
        """
        $USAGE_SYNTAX

        Run with --help or -h for more options.
        """.trimIndent()
    )
}

fun printHelpMessage() {
    println(
        """
        $USAGE_SYNTAX

        Note: You must use either long form (--flag) or short form (-f) arguments consistently throughout your command. Mixing both forms is not allowed.

        Options:
          --new-project | -np
              Generate a complete Clean Architecture project template
            --name=ProjectName | -n=ProjectName | -n ProjectName | -nProjectName
                Specify the project name (required)
            --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
                Specify the package name (required)
            --no-compose | -nc
              Disable Compose support for the project
            --ktlint | -kl
              Enable ktlint for the project
            --detekt | -d
              Enable detekt for the project
            --ktor | -kt
              Enable Ktor for data sources
            --retrofit | -rt
              Enable Retrofit for data sources
          --new-architecture | -na
              Generate a new Clean Architecture package with domain, presentation, and UI layers
            --no-compose | -nc
              Disable Compose support for the preceding architecture package
            --ktlint | -kl
              Enable ktlint for the preceding architecture package
            --detekt | -d
              Enable detekt for the preceding architecture package
          --new-feature | -nf
              Generate a new feature
            --name=FeatureName | -n=FeatureName | -n FeatureName | -nFeatureName
                Specify the feature name (required)
            --package=PackageName | --package PackageName | -p=PackageName | -p PackageName | -pPackageName
                Override the feature package for the preceding feature
            --ktlint | -kl
              Enable ktlint for the preceding feature (adds plugin and .editorconfig if missing)
            --detekt | -d
              Enable detekt for the preceding feature (adds plugin and detekt.yml if missing)
          --new-datasource | -nds
              Generate a new data source
            --name=DataSourceName | -n=DataSourceName | -n DataSourceName | -nDataSourceName
                Specify the data source name (required, DataSource suffix will be added automatically)
            --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
                Attach dependencies to the preceding new data source
          --new-use-case | -nuc
              Generate a new use case
            --name=UseCaseName | -n=UseCaseName | -n UseCaseName | -nUseCaseName
                Specify the use case name (required)
            --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
                Specify the target directory for the preceding use case
          --new-view-model | -nvm
              Generate a new ViewModel
            --name=ViewModelName | -n=ViewModelName | -n ViewModelName | -nViewModelName
                Specify the ViewModel name (required)
            --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
                Specify the target directory for the preceding ViewModel
          --help, -h
              Show this help message and exit
        """.trimIndent()
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
