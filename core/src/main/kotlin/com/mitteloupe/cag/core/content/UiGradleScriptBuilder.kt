package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildUiGradleScript(
    featurePackageName: String,
    featureNameLowerCase: String,
    enableCompose: Boolean,
    catalog: VersionCatalogReader
): String {
    val pluginAliasAndroidLibrary = (catalog.getResolvedPluginAliasFor("com.android.library") ?: "android-library").asAccessor
    val pluginAliasKotlinAndroid = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.android") ?: "kotlin-android").asAccessor
    val pluginAliasComposeCompiler = catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.plugin.compose")?.asAccessor

    val libAliasComposeBom = (catalog.getResolvedLibraryAliasForModule("androidx.compose:compose-bom") ?: "compose-bom").asAccessor
    val libAliasComposeUi = (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui") ?: "compose-ui").asAccessor
    val libAliasComposeMaterial3 =
        (catalog.getResolvedLibraryAliasForModule("androidx.compose.material3:material3") ?: "compose-material3").asAccessor
    val libAliasComposeUiGraphics =
        (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui-graphics") ?: "compose-ui-graphics").asAccessor
    val libAliasComposeNavigation =
        (catalog.getResolvedLibraryAliasForModule("androidx.navigation:navigation-compose") ?: "compose-navigation").asAccessor
    val libAliasAndroidxUiTooling =
        (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui-tooling") ?: "androidx-ui-tooling").asAccessor
    val libAliasAndroidxUiToolingPreview =
        (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui-tooling-preview") ?: "androidx-ui-tooling-preview").asAccessor

    val composePluginLine =
        if (enableCompose && pluginAliasComposeCompiler != null) {
            "    alias(libs.plugins.$pluginAliasComposeCompiler)\n"
        } else {
            ""
        }
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
    implementation(platform(libs.$libAliasComposeBom))
    implementation(libs.$libAliasComposeUi)
    implementation(libs.$libAliasComposeMaterial3)
    implementation(libs.$libAliasComposeUiGraphics)
    implementation(libs.$libAliasAndroidxUiTooling)
    implementation(libs.$libAliasComposeNavigation)
    implementation(libs.$libAliasAndroidxUiToolingPreview)
"""
        } else {
            ""
        }

    return """plugins {
    alias(libs.plugins.$pluginAliasAndroidLibrary)
    alias(libs.plugins.$pluginAliasKotlinAndroid)
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
