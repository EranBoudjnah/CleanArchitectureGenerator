package com.mitteloupe.cag.cli.flag

object SecondaryFlagConstants {
    const val NAME = "--name"
    const val PACKAGE = "--package"
    const val NO_COMPOSE = "--no-compose"
    const val KTLINT = "--ktlint"
    const val DETEKT = "--detekt"
    const val KTOR = "--ktor"
    const val RETROFIT = "--retrofit"
    const val WITH = "--with"
    const val PATH = "--path"
    const val INPUT_TYPE = "--input-type"
    const val OUTPUT_TYPE = "--output-type"
}

interface PrimaryFlag {
    val long: String
    val short: String
    val secondaryFlags: List<SecondaryFlag>

    data object NewProjectPrimary : PrimaryFlag {
        override val long = "--new-project"
        override val short = "-np"
        override val secondaryFlags =
            listOf(
                SecondaryFlag(
                    SecondaryFlagConstants.NAME,
                    "-n",
                    isMandatory = true,
                    missingErrorMessage = "Project name is required. Use --name=ProjectName or -n=ProjectName"
                ),
                SecondaryFlag(SecondaryFlagConstants.PACKAGE, "-p"),
                SecondaryFlag(SecondaryFlagConstants.NO_COMPOSE, "-nc", isBoolean = true),
                SecondaryFlag(SecondaryFlagConstants.KTLINT, "-kl", isBoolean = true),
                SecondaryFlag(SecondaryFlagConstants.DETEKT, "-d", isBoolean = true),
                SecondaryFlag(SecondaryFlagConstants.KTOR, "-kt", isBoolean = true),
                SecondaryFlag(SecondaryFlagConstants.RETROFIT, "-rt", isBoolean = true)
            )
    }

    data object NewArchitecturePrimary : PrimaryFlag {
        override val long = "--new-architecture"
        override val short = "-na"
        override val secondaryFlags =
            listOf(
                SecondaryFlag(SecondaryFlagConstants.NO_COMPOSE, "-nc", isBoolean = true),
                SecondaryFlag(SecondaryFlagConstants.KTLINT, "-kl", isBoolean = true),
                SecondaryFlag(SecondaryFlagConstants.DETEKT, "-d", isBoolean = true)
            )
    }

    data object NewFeaturePrimary : PrimaryFlag {
        override val long = "--new-feature"
        override val short = "-nf"
        override val secondaryFlags =
            listOf(
                SecondaryFlag(
                    SecondaryFlagConstants.NAME,
                    "-n",
                    isMandatory = true,
                    missingErrorMessage = "Feature name is required. Use --name=FeatureName or -n=FeatureName"
                ),
                SecondaryFlag(SecondaryFlagConstants.PACKAGE, "-p"),
                SecondaryFlag(SecondaryFlagConstants.KTLINT, "-kl", isBoolean = true),
                SecondaryFlag(SecondaryFlagConstants.DETEKT, "-d", isBoolean = true)
            )
    }

    data object NewDataSourcePrimary : PrimaryFlag {
        override val long = "--new-datasource"
        override val short = "-nds"
        override val secondaryFlags =
            listOf(
                SecondaryFlag(
                    SecondaryFlagConstants.NAME,
                    "-n",
                    isMandatory = true,
                    missingErrorMessage = "Data source name is required. Use --name=DataSourceName or -n=DataSourceName"
                ),
                SecondaryFlag(SecondaryFlagConstants.WITH, "-w")
            )
    }

    data object NewUseCasePrimary : PrimaryFlag {
        override val long = "--new-use-case"
        override val short = "-nuc"
        override val secondaryFlags =
            listOf(
                SecondaryFlag(
                    SecondaryFlagConstants.NAME,
                    "-n",
                    isMandatory = true,
                    missingErrorMessage = "Use case name is required. Use --name=UseCaseName or -n=UseCaseName"
                ),
                SecondaryFlag(SecondaryFlagConstants.PATH, "-p"),
                SecondaryFlag(SecondaryFlagConstants.INPUT_TYPE, "-it"),
                SecondaryFlag(SecondaryFlagConstants.OUTPUT_TYPE, "-ot")
            )
    }

    data object NewViewModelPrimary : PrimaryFlag {
        override val long = "--new-view-model"
        override val short = "-nvm"
        override val secondaryFlags =
            listOf(
                SecondaryFlag(
                    SecondaryFlagConstants.NAME,
                    "-n",
                    isMandatory = true,
                    missingErrorMessage = "ViewModel name is required. Use ${SecondaryFlagConstants.NAME}=ViewModelName or -n=ViewModelName"
                ),
                SecondaryFlag(SecondaryFlagConstants.PATH, "-p")
            )
    }

    data object HelpPrimary : PrimaryFlag {
        override val long = "--help"
        override val short = "-h"
        override val secondaryFlags: List<SecondaryFlag> = emptyList()
    }
}
