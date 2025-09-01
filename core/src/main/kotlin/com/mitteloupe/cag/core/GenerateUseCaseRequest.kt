package com.mitteloupe.cag.core

import java.io.File

data class GenerateUseCaseRequest(
    val destinationDirectory: File,
    val useCaseName: String
) {
    class Builder(
        private val destinationDirectory: File,
        private val useCaseName: String
    ) {
        fun build(): GenerateUseCaseRequest =
            GenerateUseCaseRequest(
                destinationDirectory = destinationDirectory,
                useCaseName = useCaseName
            )
    }
}
