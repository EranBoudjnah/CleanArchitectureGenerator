package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.content.gradle.GradleFileExtender
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildUiGradleScript(
    featurePackageName: String,
    featureNameLowerCase: String,
    enableCompose: Boolean,
    catalog: VersionCatalogReader
): String {
    val pluginAliasAndroidLibrary = catalog.getResolvedPluginAliasFor(PluginConstants.ANDROID_LIBRARY).asAccessor
    val pluginAliasKotlinAndroid = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_ANDROID).asAccessor
    val pluginAliasComposeCompiler = catalog.getResolvedPluginAliasFor(PluginConstants.COMPOSE_COMPILER).asAccessor

    val libAliasComposeBom = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_BOM).asAccessor
    val libAliasComposeUi = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_UI).asAccessor
    val libAliasComposeMaterial3 = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_MATERIAL3).asAccessor
    val libAliasComposeUiGraphics = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_UI_GRAPHICS).asAccessor
    val libAliasComposeNavigation = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_NAVIGATION).asAccessor
    val libAliasAndroidxUiTooling = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_UI_TOOLING).asAccessor
    val libAliasAndroidxUiToolingPreview = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_UI_TOOLING_PREVIEW).asAccessor

    val composePluginLine =
        if (enableCompose && catalog.isPluginAvailable(PluginConstants.COMPOSE_COMPILER)) {
            "\n    alias(libs.plugins.$pluginAliasComposeCompiler)\n"
        } else {
            ""
        }
    val gradleFileExtender = GradleFileExtender()
    val ktlintPluginLine = gradleFileExtender.buildKtlintPluginLine(catalog)
    val detektPluginLine = gradleFileExtender.buildDetektPluginLine(catalog)
    val ktlintConfiguration = gradleFileExtender.buildKtlintConfiguration(catalog)
    val detektConfiguration = gradleFileExtender.buildDetektConfiguration(catalog, "../../../detekt.yml")
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

    val configurations = "$ktlintConfiguration$detektConfiguration".trimIndent()
    return """plugins {
    alias(libs.plugins.$pluginAliasAndroidLibrary)
    alias(libs.plugins.$pluginAliasKotlinAndroid)$composePluginLine$ktlintPluginLine$detektPluginLine
}

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
${ if (configurations.isEmpty()) {
        ""
    } else {
        "\n$configurations\n"
    }
    }
dependencies {
    implementation(projects.features.$featureNameLowerCase.presentation)
    implementation(projects.architecture.ui)
    implementation(projects.architecture.presentation)
$composeDependenciesSection}
"""
}
