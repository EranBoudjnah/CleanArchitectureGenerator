package com.mitteloupe.cag.cli.flag

import com.mitteloupe.cag.cli.flag.SecondaryFlags.nameSecondaryFlag

object SecondaryFlagOptions {
    val NAME = FlagOption("--name", "-n")
    val PACKAGE = FlagOption("--package", "-p")
    val NO_COMPOSE = FlagOption("--no-compose", "-nc")
    val KTLINT = FlagOption("--ktlint", "-kl")
    val DETEKT = FlagOption("--detekt", "-d")
    val KTOR = FlagOption("--ktor", "-kr")
    val RETROFIT = FlagOption("--retrofit", "-rt")
    val WITH = FlagOption("--with", "-w")
    val PATH = FlagOption("--path", "-p")
    val INPUT_TYPE = FlagOption("--input-type", "-it")
    val OUTPUT_TYPE = FlagOption("--output-type", "-ot")
    val HELP_TOPIC = FlagOption("--topic", "-t")
    val HELP_FORMAT = FlagOption("--format", "-f")
    val GIT = FlagOption("--git", "-g")
    val DEPENDENCY_INJECTION = FlagOption("--dependency-injection", "-DI")
}

private object SecondaryFlags {
    fun PrimaryFlag.nameSecondaryFlag(
        typeName: String,
        exampleValue: String
    ) = SecondaryFlag(
        option = SecondaryFlagOptions.NAME,
        isMandatory = true,
        missingErrorMessage = nameError(typeName = typeName, exampleValue = exampleValue)
    )

    val packageName = SecondaryFlag(option = SecondaryFlagOptions.PACKAGE)
    val noCompose = SecondaryFlag(option = SecondaryFlagOptions.NO_COMPOSE, isBoolean = true)
    val ktlint = SecondaryFlag(option = SecondaryFlagOptions.KTLINT, isBoolean = true)
    val detekt = SecondaryFlag(option = SecondaryFlagOptions.DETEKT, isBoolean = true)
    val git =
        SecondaryFlag(
            option = SecondaryFlagOptions.GIT,
            isBoolean = true,
            missingErrorMessage = "Git flag must be used without a value"
        )
    val path = SecondaryFlag(option = SecondaryFlagOptions.PATH)
    val dependencyInjection = SecondaryFlag(option = SecondaryFlagOptions.DEPENDENCY_INJECTION)
}

interface PrimaryFlag {
    val long: String
    val short: String
    val secondaryFlags: Set<SecondaryFlag>

    fun nameError(
        typeName: String,
        exampleValue: String
    ) = "$typeName name is required. Use ${SecondaryFlagOptions.NAME.long}=$exampleValue or " +
        "${SecondaryFlagOptions.NAME.short}=$exampleValue"

    data object VersionPrimary : PrimaryFlag {
        override val long = "--version"
        override val short = "-v"
        override val secondaryFlags = emptySet<SecondaryFlag>()
    }

    data object NewProjectPrimary : PrimaryFlag {
        override val long = "--new-project"
        override val short = "-np"
        override val secondaryFlags =
            linkedSetOf(
                nameSecondaryFlag(typeName = "Project", exampleValue = "ProjectName"),
                SecondaryFlags.packageName,
                SecondaryFlags.noCompose,
                SecondaryFlags.ktlint,
                SecondaryFlags.detekt,
                SecondaryFlag(option = SecondaryFlagOptions.KTOR, isBoolean = true),
                SecondaryFlag(option = SecondaryFlagOptions.RETROFIT, isBoolean = true),
                SecondaryFlags.git,
                SecondaryFlags.dependencyInjection
            )
    }

    data object NewArchitecturePrimary : PrimaryFlag {
        override val long = "--new-architecture"
        override val short = "-na"
        override val secondaryFlags =
            linkedSetOf(
                SecondaryFlags.noCompose,
                SecondaryFlags.ktlint,
                SecondaryFlags.detekt,
                SecondaryFlags.git,
                SecondaryFlags.dependencyInjection
            )
    }

    data object NewFeaturePrimary : PrimaryFlag {
        override val long = "--new-feature"
        override val short = "-nf"
        override val secondaryFlags =
            linkedSetOf(
                nameSecondaryFlag(typeName = "Feature", exampleValue = "FeatureName"),
                SecondaryFlags.packageName,
                SecondaryFlags.ktlint,
                SecondaryFlags.detekt,
                SecondaryFlags.git
            )
    }

    data object NewDataSourcePrimary : PrimaryFlag {
        override val long = "--new-datasource"
        override val short = "-nds"
        override val secondaryFlags =
            linkedSetOf(
                nameSecondaryFlag(typeName = "Data source", exampleValue = "DataSourceName"),
                SecondaryFlag(option = SecondaryFlagOptions.WITH),
                SecondaryFlags.git
            )
    }

    data object NewUseCasePrimary : PrimaryFlag {
        override val long = "--new-use-case"
        override val short = "-nuc"
        override val secondaryFlags =
            linkedSetOf(
                nameSecondaryFlag(typeName = "Use case", exampleValue = "UseCaseName"),
                SecondaryFlags.path,
                SecondaryFlag(option = SecondaryFlagOptions.INPUT_TYPE),
                SecondaryFlag(option = SecondaryFlagOptions.OUTPUT_TYPE),
                SecondaryFlags.git
            )
    }

    data object NewViewModelPrimary : PrimaryFlag {
        override val long = "--new-view-model"
        override val short = "-nvm"
        override val secondaryFlags =
            linkedSetOf(
                nameSecondaryFlag(typeName = "ViewModel", exampleValue = "ViewModelName"),
                SecondaryFlags.path,
                SecondaryFlags.git
            )
    }

    data object HelpPrimary : PrimaryFlag {
        override val long = "--help"
        override val short = "-h"
        override val secondaryFlags =
            linkedSetOf(
                SecondaryFlag(option = SecondaryFlagOptions.HELP_TOPIC),
                SecondaryFlag(option = SecondaryFlagOptions.HELP_FORMAT)
            )
    }
}
