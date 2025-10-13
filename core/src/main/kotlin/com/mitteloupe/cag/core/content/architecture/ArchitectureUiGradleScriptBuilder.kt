package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.content.gradle.GradleFileExtender
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitectureUiGradleScript(
    architecturePackageName: String,
    catalog: VersionCatalogReader
): String {
    val pluginAliasAndroidLibrary = catalog.getResolvedPluginAliasFor(PluginConstants.ANDROID_LIBRARY).asAccessor
    val pluginAliasKotlinAndroid = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_ANDROID).asAccessor
    val pluginAliasKsp = catalog.getResolvedPluginAliasFor(PluginConstants.KSP).asAccessor
    val pluginAliasComposeCompiler = catalog.getResolvedPluginAliasFor(PluginConstants.COMPOSE_COMPILER).asAccessor

    val libAliasComposeBom = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_BOM).asAccessor
    val libAliasComposeUi = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_UI).asAccessor
    val libAliasAndroidxFragmentKtx = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_FRAGMENT_KTX).asAccessor
    val libAliasAndroidxNavigationFragmentKtx =
        catalog
            .getResolvedLibraryAliasForModule(
                LibraryConstants.ANDROIDX_NAVIGATION_FRAGMENT_KTX
            ).asAccessor

    val composePluginLine =
        if (catalog.isPluginAvailable(PluginConstants.COMPOSE_COMPILER)) {
            "\n    alias(libs.plugins.$pluginAliasComposeCompiler)\n"
        } else {
            ""
        }

    val gradleFileExtender = GradleFileExtender()
    val ktlintPluginLine = gradleFileExtender.buildKtlintPluginLine(catalog)
    val detektPluginLine = gradleFileExtender.buildDetektPluginLine(catalog)
    val ktlintConfiguration = gradleFileExtender.buildKtlintConfiguration(catalog)
    val detektConfiguration = gradleFileExtender.buildDetektConfiguration(catalog)

    val configurations = "$ktlintConfiguration$detektConfiguration".trimIndent()
    return """plugins {
    alias(libs.plugins.$pluginAliasAndroidLibrary)
    alias(libs.plugins.$pluginAliasKotlinAndroid)
    alias(libs.plugins.$pluginAliasKsp)$composePluginLine$ktlintPluginLine$detektPluginLine
}

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
${ if (configurations.isEmpty()) {
        ""
    } else {
        "\n$configurations\n"
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
