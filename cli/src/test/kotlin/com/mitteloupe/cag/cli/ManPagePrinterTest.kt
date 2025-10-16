package com.mitteloupe.cag.cli

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.ByteArrayOutputStream
import java.io.PrintStream

private const val HEADER =
    """.TH CAG 1 "" "" "cag"
.SH NAME
cag - generate Clean Architecture Android code
.SH SYNOPSIS
.B cag
[\fB--new-project\fR \fB--name=\fR\fIProjectName\fR \fB--package=\fR\fIPackageName\fR [\fB--no-compose\fR] [\fB--ktlint\fR] [\fB--detekt\fR] [\fB--ktor\fR] [\fB--retrofit\fR] [\fB--git\fR] [\fB--dependency-injection=hilt\fR|\fBkoin\fR|\fBnone\fR]]... [\fB--new-architecture\fR [\fB--no-compose\fR] [\fB--ktlint\fR] [\fB--detekt\fR] [\fB--git\fR] [\fB--dependency-injection=hilt\fR|\fBkoin\fR|\fBnone\fR]]... [\fB--new-feature\fR [\fB--name=\fR\fIFeatureName\fR [\fB--package=\fR\fIPackageName\fR] [\fB--ktlint\fR] [\fB--detekt\fR] [\fB--git\fR]]... [\fB--new-datasource\fR [\fB--name=\fR\fIDataSourceName\fR [\fB--with=ktor\fR|\fBretrofit\fR|\fBktor,retrofit\fR] [\fB--git\fR]]... [\fB--new-use-case\fR [\fB--name=\fR\fIUseCaseName\fR [\fB--path=\fR\fITargetPath\fR] [\fB--git\fR]]... [\fB--new-view-model\fR [\fB--name=\fR\fIViewModelName\fR [\fB--path=\fR\fITargetPath\fR] [\fB--git\fR]]...
.SH DESCRIPTION
.B cag
generates Android Clean Architecture scaffolding and components.
"""

private val FULL_HELP =
    """
    .SH NEW-PROJECT
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
        --ktor | -kr
            Enable Ktor for data sources
        --retrofit | -rt
            Enable Retrofit for data sources
        --git | -g
            Automatically initialize git repository and stage changes
        --dependency-injection=hilt[default]|koin|none | -DI hilt[default]|koin|none
            Specify the dependency injection library
    Examples:
      cag --new-project --name=MyApp --package=com.example.myapp
      cag --new-project --name=MyApp --package=com.example.myapp --no-compose --ktlint --detekt
      cag --new-project --name=MyApp --package=com.example.myapp --ktor --retrofit
      cag --new-project --name=MyApp --package=com.example.myapp --git
      cag --new-project --name=MyApp --package=com.example.myapp -DI koin
    .SH NEW-ARCHITECTURE
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
        --dependency-injection=hilt[default]|koin|none | -DI hilt[default]|koin|none
            Specify the dependency injection library
    Examples:
      cag --new-architecture
      cag --new-architecture --no-compose
      cag --new-architecture --ktlint --detekt
      cag --new-architecture --git
    .SH NEW-FEATURE
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
    .SH NEW-DATASOURCE
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
    .SH NEW-USE-CASE
    Options: new-use-case
      --new-use-case | -nuc
          Generate a new use case
        --name=UseCaseName | -n=UseCaseName | -n UseCaseName | -nUseCaseName
            Specify the use case name (required)
        --path=TargetPath | --path TargetPath | -p=TargetPath | -p TargetPath | -pTargetPath
            Specify the target directory for the preceding use case
        --git | -g
            Automatically stage changes to git repository
    Examples:
      cag --new-use-case --name=FetchUser
      cag --new-use-case --name=FetchUser --path=architecture/domain/src/main/kotlin
    .SH NEW-VIEW-MODEL
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
    .SH CONFIGURATION
    CLI configuration (.cagrc)
      You can configure library and plugin versions used by the CLI via a simple INI-style config file named .cagrc.
    .PP
      Locations:
        - Project root: ./.cagrc
        - User home: ~/.cagrc
    .PP
      Precedence:
        - Values in the project .cagrc override values in ~/.cagrc
    .PP
      Sections:
        - [new.versions] - applied when generating new projects (e.g., --new-project)
        - [existing.versions] - applied when generating into an existing project (e.g., new architecture, feature, data source, use case, or view model)
        - [git] - configuration for git integration
    .PP
      Version Keys:
        - Keys in [new.versions] and [existing.versions] correspond to version keys used by the generator, 
          for example: kotlin, androidGradlePlugin, composeBom, composeNavigation, retrofit, ktor, okhttp3, etc.
    .PP
      Git Configuration:
        - autoInitialize=true|false - Whether to automatically initialize a git repository for new projects (default: false)
        - autoStage=true|false - Whether to automatically stage changes after generation (default: false)
        - path=/absolute/path/to/git - Optional path to the git executable (default: resolved via PATH)
    Example ~/.cagrc:
      [new.versions]
      kotlin=2.2.10
      composeBom=2025.08.01
    .PP
      [existing.versions]
      retrofit=2.11.0
      ktor=3.0.3
    .PP
      [git]
      autoInitialize=true
      autoStage=true
      path=/usr/bin/git
    .PP
    Example ./.cagrc (project overrides):
      [new.versions]
      composeBom=2025.09.01
    .PP
      [existing.versions]
      okhttp3=4.12.0
    .PP
      [git]
      autoInitialize=false
      path=/opt/homebrew/bin/git
    .SH GENERAL
    General
      --version | -v
          Show the current version
      --help | -h [--topic=<topic>] [--format=man]
          Show help. When a topic is provided, prints only that section. With --format=man outputs a roff man page to stdout.
          Topics: new-project, new-architecture, new-feature, new-datasource, new-use-case, new-view-model, configuration, general
    Examples:
      cag --version
      cag --help
      cag --help --topic=new-feature
      cag --help --format=man | col -b
    """.trimIndent()

@RunWith(Parameterized::class)
class ManPagePrinterTest(
    val givenTopic: String?,
    val expectedBody: String
) {
    companion object {
        @JvmStatic
        @Parameters(name = "Given primitive type {0} then returns {1}")
        fun parameters(): Collection<Array<Any?>> =
            listOf(
                arrayOf(null, FULL_HELP),
                arrayOf(" All ", FULL_HELP),
                arrayOf("overview", FULL_HELP),
                arrayOf(
                    "unknown",
                    """
                    .SH ERROR
                    Unknown topic: "unknown".
                    .PP
                    Valid topics:
                      configuration
                      general
                      new-architecture
                      new-datasource
                      new-feature
                      new-project
                      new-use-case
                      new-view-model
                    """.trimIndent()
                ),
                arrayOf(
                    "new-architecture",
                    """
                    .SH NEW-ARCHITECTURE
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
                        --dependency-injection=hilt[default]|koin|none | -DI hilt[default]|koin|none
                            Specify the dependency injection library
                    Examples:
                      cag --new-architecture
                      cag --new-architecture --no-compose
                      cag --new-architecture --ktlint --detekt
                      cag --new-architecture --git
                    """.trimIndent()
                )
            )
    }

    private lateinit var originalOutput: PrintStream
    private lateinit var stubbedOutputStream: ByteArrayOutputStream

    @Before
    fun setUp() {
        originalOutput = System.out
        stubbedOutputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(stubbedOutputStream))
    }

    @After
    fun tearDown() {
        System.setOut(originalOutput)
    }

    @Test
    fun `Given topic is null when printManPage then prints full help`() {
        // Given
        val expectedOutput = HEADER + expectedBody + "\n"

        // When
        ManPagePrinter.printManPage(givenTopic)
        val actual = stubbedOutputStream.toString()

        // Then
        assertEquals(expectedOutput, actual)
    }
}
