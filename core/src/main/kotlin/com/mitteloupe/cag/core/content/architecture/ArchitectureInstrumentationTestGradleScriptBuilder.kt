package com.mitteloupe.cag.core.content.architecture

import com.mitteloupe.cag.core.content.gradle.GradleFileExtender
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor
import com.mitteloupe.cag.core.option.DependencyInjection

fun buildArchitectureInstrumentationTestGradleScript(
    architecturePackageName: String,
    dependencyInjection: DependencyInjection,
    catalog: VersionCatalogReader
): String {
    val aliasAndroidLibrary = catalog.getResolvedPluginAliasFor(PluginConstants.ANDROID_LIBRARY).asAccessor
    val aliasKotlinAndroid = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_ANDROID).asAccessor

    val aliasMaterial = catalog.getResolvedLibraryAliasForModule(LibraryConstants.MATERIAL).asAccessor
    val aliasComposeBom = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_BOM).asAccessor
    val aliasComposeUi = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_UI).asAccessor
    val aliasComposeUiGraphics = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_UI_GRAPHICS).asAccessor
    val aliasComposeUiToolingPreview = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_UI_TOOLING_PREVIEW).asAccessor
    val aliasComposeMaterial3 = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_MATERIAL3).asAccessor
    val aliasTestJunit = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_JUNIT).asAccessor
    val aliasTestAndroidxJunit = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_ANDROIDX_JUNIT).asAccessor
    val aliasTestAndroidxEspressoCore = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_ANDROIDX_ESPRESSO_CORE).asAccessor
    val aliasTestComposeUiJunit4 = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_COMPOSE_UI_JUNIT4).asAccessor
    val aliasTestAndroidUiAutomator = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_ANDROID_UI_AUTOMATOR).asAccessor
    val aliasOkhttp3 = catalog.getResolvedLibraryAliasForModule(LibraryConstants.OKHTTP3).asAccessor
    val aliasTestAndroidMockWebServer = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_ANDROID_MOCKWEBSERVER).asAccessor
    val aliasAndroidxAppcompat = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_APPCOMPAT).asAccessor
    val aliasTestAndroidxRules = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_ANDROIDX_RULES).asAccessor
    val aliasAndroidxRecyclerview = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_RECYCLERVIEW).asAccessor

    val gradleFileExtender = GradleFileExtender()
    val composePluginLine = gradleFileExtender.buildComposePluginLine(catalog)
    val ktlintPluginLine = gradleFileExtender.buildKtlintPluginLine(catalog)
    val detektPluginLine = gradleFileExtender.buildDetektPluginLine(catalog)
    val ktlintConfiguration = gradleFileExtender.buildKtlintConfiguration(catalog)
    val detektConfiguration = gradleFileExtender.buildDetektConfiguration(catalog)

    val configurations = "$ktlintConfiguration$detektConfiguration".trimIndent()
    val hiltDependency =
        if (dependencyInjection == DependencyInjection.Hilt) {
            val aliasTestAndroidHilt = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_ANDROID_HILT).asAccessor
            """
    implementation(libs.$aliasTestAndroidHilt)"""
        } else {
            ""
        }
    return """plugins {
    alias(libs.plugins.$aliasAndroidLibrary)
    alias(libs.plugins.$aliasKotlinAndroid)$composePluginLine$ktlintPluginLine$detektPluginLine
}

android {
    namespace = "${architecturePackageName.substringBeforeLast('.')}.test"
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

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}
${ if (configurations.isEmpty()) {
        ""
    } else {
        "\n$configurations\n"
    }
    }
dependencies {
    implementation(libs.$aliasMaterial)

    implementation(platform(libs.$aliasComposeBom))
    implementation(libs.$aliasComposeUi)
    implementation(libs.$aliasComposeUiGraphics)
    implementation(libs.$aliasComposeUiToolingPreview)
    implementation(libs.$aliasComposeMaterial3)

    implementation(libs.$aliasTestJunit)
    implementation(libs.$aliasTestAndroidxJunit)
    implementation(libs.$aliasTestAndroidxEspressoCore)
    implementation(libs.$aliasTestComposeUiJunit4)$hiltDependency
    implementation(libs.$aliasTestAndroidUiAutomator)
    implementation(libs.$aliasTestAndroidxEspressoCore)
    implementation(libs.$aliasOkhttp3)
    implementation(libs.$aliasTestAndroidMockWebServer)
    implementation(libs.$aliasAndroidxAppcompat)
    implementation(libs.$aliasTestAndroidxRules)
    implementation(libs.$aliasAndroidxRecyclerview)
    implementation(kotlin("reflect"))
}
"""
}
