package com.mitteloupe.cag.core.request

import java.io.File

data class GenerateViewModelRequest(
    val destinationDirectory: File,
    val viewModelName: String,
    val viewModelPackageName: String,
    val featurePackageName: String,
    val projectNamespace: String
) {
    class Builder(
        private val destinationDirectory: File,
        private val viewModelName: String,
        private val viewModelPackageName: String,
        private val featurePackageName: String,
        private val projectNamespace: String
    ) {
        fun build(): GenerateViewModelRequest =
            GenerateViewModelRequest(
                destinationDirectory = destinationDirectory,
                viewModelName = viewModelName,
                viewModelPackageName = viewModelPackageName,
                featurePackageName = featurePackageName,
                projectNamespace = projectNamespace
            )
    }
}
