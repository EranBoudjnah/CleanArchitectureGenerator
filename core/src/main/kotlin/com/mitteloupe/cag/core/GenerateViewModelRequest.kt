package com.mitteloupe.cag.core

import java.io.File

data class GenerateViewModelRequest(
    val destinationDirectory: File,
    val viewModelName: String,
    val featurePackageName: String,
    val projectNamespace: String
) {
    class Builder(
        private val destinationDirectory: File,
        private val viewModelName: String,
        private val featurePackageName: String,
        private val projectNamespace: String
    ) {
        fun build(): GenerateViewModelRequest =
            GenerateViewModelRequest(
                destinationDirectory = destinationDirectory,
                viewModelName = viewModelName,
                featurePackageName = featurePackageName,
                projectNamespace = projectNamespace
            )
    }
}
