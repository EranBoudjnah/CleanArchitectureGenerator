package com.mitteloupe.cag.core.request

import java.io.File

data class GenerateFeatureRequest(
    val featureName: String,
    val featurePackageName: String?,
    val projectNamespace: String,
    val destinationRootDirectory: File,
    val enableCompose: Boolean,
    val enableKtlint: Boolean,
    val enableDetekt: Boolean,
    val appModuleDirectory: File?
)

class GenerateFeatureRequestBuilder(
    private val destinationRootDir: File,
    private val projectNamespace: String,
    private val featureName: String
) {
    private var featurePackageName: String? = null
    private var enableCompose: Boolean = false
    private var appModuleDirectory: File? = null
    private var enableKtlint: Boolean = false
    private var enableDetekt: Boolean = false

    fun featurePackageName(featurePackageName: String?) =
        apply {
            this.featurePackageName = featurePackageName
        }

    fun enableCompose(enable: Boolean) =
        apply {
            this.enableCompose = enable
        }

    fun appModuleDirectory(appModuleDirectory: File?) =
        apply {
            this.appModuleDirectory = appModuleDirectory
        }

    fun enableKtlint(enable: Boolean) =
        apply {
            this.enableKtlint = enable
        }

    fun enableDetekt(enable: Boolean) =
        apply {
            this.enableDetekt = enable
        }

    fun build(): GenerateFeatureRequest =
        GenerateFeatureRequest(
            featureName = featureName,
            featurePackageName = featurePackageName,
            projectNamespace = projectNamespace,
            destinationRootDirectory = destinationRootDir,
            enableCompose = enableCompose,
            enableKtlint = enableKtlint,
            enableDetekt = enableDetekt,
            appModuleDirectory = appModuleDirectory
        )
}
