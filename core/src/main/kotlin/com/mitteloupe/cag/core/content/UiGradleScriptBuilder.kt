package com.mitteloupe.cag.core.content

fun buildUiGradleScript(
    featurePackageName: String,
    featureNameLowerCase: String,
    enableCompose: Boolean
): String {
    val composePluginLine = if (enableCompose) "    alias(libs.plugins.compose.compiler)\n" else ""
    val composeBuildFeaturesSection =
        if (enableCompose) {
            """
    buildFeatures {
        compose = true
    }
"""
        } else {
            ""
        }

    val composeDependenciesSection =
        if (enableCompose) {
            """
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.graphics)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
"""
        } else {
            ""
        }

    return """plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
$composePluginLine}

android {
    namespace = "$featurePackageName.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
$composeBuildFeaturesSection}

dependencies {
    implementation(projects.features.$featureNameLowerCase.presentation)
    implementation(projects.architecture.ui)
    implementation(projects.architecture.presentation)
$composeDependenciesSection}
"""
}
