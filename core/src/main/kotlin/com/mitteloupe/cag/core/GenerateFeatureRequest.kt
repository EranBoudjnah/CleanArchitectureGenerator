package com.mitteloupe.cag.core

import java.io.File

data class GenerateFeatureRequest(
    val featureName: String,
    val featurePackageName: String?,
    val destinationRootDir: File
)

class GenerateFeatureRequestBuilder(
    private val destinationRootDir: File,
    private val featureName: String
) {
    private var featurePackageName: String? = null

    fun featurePackageName(featurePackageName: String?) =
        apply {
            this.featurePackageName = featurePackageName
        }

    fun build(): GenerateFeatureRequest = GenerateFeatureRequest(featureName, featurePackageName, destinationRootDir)
}
