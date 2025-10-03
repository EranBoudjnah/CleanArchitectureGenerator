package com.mitteloupe.cag.cli

object HelpContent {
    private const val NEW_PROJECT_SYNTAX =
        "[--new-project --name=ProjectName --package=PackageName " +
            "[--no-compose] [--ktlint] [--detekt] [--ktor] [--retrofit] [--git]]"
    private const val NEW_ARCHITECTURE_SYNTAX = "[--new-architecture [--no-compose] [--ktlint] [--detekt] [--git]]"
    private const val NEW_FEATURE_SYNTAX = "[--new-feature --name=FeatureName [--package=PackageName] [--ktlint] [--detekt] [--git]]"
    private const val NEW_DATASOURCE_SYNTAX = "[--new-datasource --name=DataSourceName [--with=ktor|retrofit|ktor,retrofit] [--git]]"
    private const val NEW_USE_CASE_SYNTAX = "[--new-use-case --name=UseCaseName [--path=TargetPath] [--git]]"
    private const val NEW_VIEW_MODEL_SYNTAX = "[--new-view-model --name=ViewModelName [--path=TargetPath] [--git]]"
    const val USAGE_SYNTAX: String =
        "usage: cag " +
            "$NEW_PROJECT_SYNTAX... " +
            "$NEW_ARCHITECTURE_SYNTAX... " +
            "$NEW_FEATURE_SYNTAX... " +
            "$NEW_DATASOURCE_SYNTAX... " +
            "$NEW_USE_CASE_SYNTAX... " +
            "$NEW_VIEW_MODEL_SYNTAX..."

    fun helpSections(): Map<String, String> =
        buildMap {
            put(
                "new-project",
                """
                Options: new-project
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
                    --git | -g
                        Automatically initialize git repository and stage changes

                Examples:
                  cag --new-project --name=MyApp --package=com.example.myapp
                  cag --new-project --name=MyApp --package=com.example.myapp --no-compose --ktlint --detekt
                  cag --new-project --name=MyApp --package=com.example.myapp --ktor --retrofit
                  cag --new-project --name=MyApp --package=com.example.myapp --git
                """.trimIndent()
            )
            put(
                "new-architecture",
                """
                Options: new-architecture
                  --new-architecture | -na
                      Generate a new Clean Architecture package with domain, presentation, and UI layers
                    --no-compose | -nc
                        Disable Compose support for the preceding architecture package
                    --ktlint | -kl
                        Enable ktlint for the preceding architecture package
                    --detekt | -d
                        Enable detekt for the preceding architecture package
                    --git | -g
                        Automatically stage changes to git repository

                Examples:
                  cag --new-architecture
                  cag --new-architecture --no-compose
                  cag --new-architecture --ktlint --detekt
                  cag --new-architecture --git
                """.trimIndent()
            )
            put(
                "new-feature",
                """
                Options: new-feature
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
                    --git | -g
                        Automatically stage changes to git repository

                Examples:
                  cag --new-feature --name=Profile
                  cag --new-feature --name=Profile --package=com.example.feature.profile
                  cag --new-feature --name=Profile --ktlint --detekt
                """.trimIndent()
            )
            put(
                "new-datasource",
                """
                Options: new-datasource
                  --new-datasource | -nds
                      Generate a new data source
                    --name=DataSourceName | -n=DataSourceName | -n DataSourceName | -nDataSourceName
                        Specify the data source name (required, DataSource suffix will be added automatically)
                    --with=ktor|retrofit|ktor,retrofit | -w=ktor|retrofit|ktor,retrofit
                        Attach dependencies to the preceding new data source
                    --git | -g
                        Automatically stage changes to git repository

                Examples:
                  cag --new-datasource --name=User
                  cag --new-datasource --name=User --with=retrofit
                  cag --new-datasource --name=User --with=ktor,retrofit
                """.trimIndent()
            )
            put(
                "new-use-case",
                """
                Options: new-use-case
                  --new-use-case | -nuc
                      Generate a new use case
                    --name=UseCaseName | -n=UseCaseName | -n UseCaseName | -nUseCaseName
                        Specify the use case name (required)
                    --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
                        Specify the target directory for the preceding use case

                Examples:
                  cag --new-use-case --name=FetchUser
                  cag --new-use-case --name=FetchUser --path=architecture/domain/src/main/kotlin
                """.trimIndent()
            )
            put(
                "new-view-model",
                """
                Options: new-view-model
                  --new-view-model | -nvm
                      Generate a new ViewModel
                    --name=ViewModelName | -n=ViewModelName | -n ViewModelName | -nViewModelName
                        Specify the ViewModel name (required)
                    --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
                        Specify the target directory for the preceding ViewModel
                    --git | -g
                        Automatically stage changes to git repository

                Examples:
                  cag --new-view-model --name=Profile
                  cag --new-view-model --name=Profile --path=architecture/presentation/src/main/kotlin
                """.trimIndent()
            )
            put(
                "general",
                """
                General
                  --help | -h [--topic=<topic>] [--format=man]
                      Show help. When a topic is provided, prints only that section. With --format=man outputs a roff man page to stdout.
                      Topics: new-project, new-architecture, new-feature, new-datasource, new-use-case, new-view-model, configuration

                Examples:
                  cag --help
                  cag --help --topic=new-feature
                  cag --help --format=man | col -b
                """.trimIndent()
            )
            put(
                "configuration",
                """
                CLI configuration (.cagrc)
                  You can configure library and plugin versions used by the CLI via a simple INI-style config file named .cagrc.

                  Locations:
                    - Project root: ./.cagrc
                    - User home: ~/.cagrc

                  Precedence:
                    - Values in the project .cagrc override values in ~/.cagrc.

                  Sections:
                    - [new.versions] - applied when generating new projects (e.g., --new-project)
                    - [existing.versions] - applied when generating into an existing project (e.g., new architecture, feature, data source, use case, or view model)
                    - [git] - configuration for git integration

                  Version Keys:
                    - Keys in [new.versions] and [existing.versions] correspond to version keys used by the generator, 
                      for example: kotlin, androidGradlePlugin, composeBom, composeNavigation, retrofit, ktor, okhttp3, etc.

                  Git Configuration:
                    - autoInitialize=true|false - Whether to automatically initialize a git repository for new projects (default: false)
                    - autoStage=true|false - Whether to automatically stage changes after generation (default: false)
                    - path=/absolute/path/to/git - Optional path to the git executable (default: resolved via PATH)

                  Example ~/.cagrc:
                    [new.versions]
                    kotlin=2.2.10
                    composeBom=2025.08.01

                    [existing.versions]
                    retrofit=2.11.0
                    ktor=3.0.3

                    [git]
                    autoInitialize=true
                    autoStage=true
                    path=/usr/bin/git

                  Example ./.cagrc (project overrides):
                    [new.versions]
                    composeBom=2025.09.01

                    [existing.versions]
                    okhttp3=4.12.0
                    
                    [git]
                    autoInitialize=false
                    path=/opt/homebrew/bin/git
                """.trimIndent()
            )
        }
}
