package com.mitteloupe.cag.core

data class GenerateFeatureRequest(
    val featureName: String,
    val featurePackageName: String?
)

class GenerateFeatureRequestBuilder {
    private var featureName: String? = null
    private var featurePackageName: String? = null

    fun featureName(featureName: String) = apply { this.featureName = featureName }

    fun featurePackageName(featurePackageName: String?) =
        apply {
            this.featurePackageName = featurePackageName
        }

    fun build(): GenerateFeatureRequest {
        val name = requireNotNull(featureName) { "featureName is required" }
        return GenerateFeatureRequest(name, featurePackageName)
    }
}
