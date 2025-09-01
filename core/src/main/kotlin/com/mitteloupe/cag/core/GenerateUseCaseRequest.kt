package com.mitteloupe.cag.core

import java.io.File

data class GenerateUseCaseRequest(
    val destinationDirectory: File,
    val useCaseName: String,
    val inputDataType: String?,
    val outputDataType: String?
) {
    class Builder(
        private val destinationDirectory: File,
        private val useCaseName: String,
        private val inputDataType: String? = null,
        private val outputDataType: String? = null
    ) {
        fun inputDataType(inputDataType: String?): Builder = Builder(destinationDirectory, useCaseName, inputDataType, outputDataType)

        fun outputDataType(outputDataType: String?): Builder = Builder(destinationDirectory, useCaseName, inputDataType, outputDataType)

        fun build(): GenerateUseCaseRequest =
            GenerateUseCaseRequest(
                destinationDirectory = destinationDirectory,
                useCaseName = useCaseName,
                inputDataType = inputDataType,
                outputDataType = outputDataType
            )
    }
}
