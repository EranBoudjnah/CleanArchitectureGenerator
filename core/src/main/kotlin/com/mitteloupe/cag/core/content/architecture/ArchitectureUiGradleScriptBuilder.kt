package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitectureUiGradleScript(
    architecturePackageName: String,
    catalog: VersionCatalogReader
): String {
    val pluginAliasAndroidLibrary = (catalog.getResolvedPluginAliasFor("com.android.library") ?: "android-library").asAccessor
    val pluginAliasKotlinAndroid = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.android") ?: "kotlin-android").asAccessor
    val pluginAliasKsp = (catalog.getResolvedPluginAliasFor("com.google.devtools.ksp") ?: "ksp").asAccessor
    val pluginAliasComposeCompiler = catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.plugin.compose")?.asAccessor

    val libAliasComposeBom = (catalog.getResolvedLibraryAliasForModule("androidx.compose:compose-bom") ?: "compose-bom").asAccessor
    val libAliasComposeUi = (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui") ?: "compose-ui").asAccessor
    val libAliasAndroidxFragmentKtx =
        (catalog.getResolvedLibraryAliasForModule("androidx.fragment:fragment-ktx") ?: "androidx-fragment-ktx").asAccessor
    val libAliasAndroidxNavigationFragmentKtx =
        (
            catalog.getResolvedLibraryAliasForModule("androidx.navigation:navigation-fragment-ktx")
                ?: "androidx-navigation-fragment-ktx"
        ).asAccessor

    val composePluginLine =
        if (pluginAliasComposeCompiler != null) {
            "    alias(libs.plugins.$pluginAliasComposeCompiler)\n"
        } else {
            ""
        }

    return """plugins {
    alias(libs.plugins.$pluginAliasAndroidLibrary)
    alias(libs.plugins.$pluginAliasKotlinAndroid)
    alias(libs.plugins.$pluginAliasKsp)
$composePluginLine}

android {
    namespace = "$architecturePackageName.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.architecture.presentation)

    implementation(projects.coroutine)

    implementation(libs.$libAliasAndroidxFragmentKtx)
    implementation(libs.$libAliasAndroidxNavigationFragmentKtx)

    implementation(platform(libs.$libAliasComposeBom))
    implementation(libs.$libAliasComposeUi)
}
"""
}
