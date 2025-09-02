package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitectureDomainGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasAndroidLibrary = (catalog.getResolvedPluginAliasFor("com.android.library") ?: "android-library").asAccessor
    val pluginAliasKotlinAndroid = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.android") ?: "kotlin-android").asAccessor

    return """import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.$pluginAliasAndroidLibrary)
    alias(libs.plugins.$pluginAliasKotlinAndroid)
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

android {
    namespace = "com.example.architecture.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
"""
}

fun buildArchitecturePresentationGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasAndroidLibrary = (catalog.getResolvedPluginAliasFor("com.android.library") ?: "android-library").asAccessor
    val pluginAliasKotlinAndroid = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.android") ?: "kotlin-android").asAccessor

    return """import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.$pluginAliasAndroidLibrary)
    alias(libs.plugins.$pluginAliasKotlinAndroid)
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

android {
    namespace = "com.example.architecture.presentation"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation(project(":architecture:domain"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
"""
}

fun buildArchitectureUiGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasAndroidLibrary = (catalog.getResolvedPluginAliasFor("com.android.library") ?: "android-library").asAccessor
    val pluginAliasKotlinAndroid = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.android") ?: "kotlin-android").asAccessor
    val pluginAliasComposeCompiler = catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.plugin.compose")?.asAccessor

    return """import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.$pluginAliasAndroidLibrary)
    alias(libs.plugins.$pluginAliasKotlinAndroid)
    alias(libs.plugins.$pluginAliasComposeCompiler)
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

android {
    namespace = "com.example.architecture.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation(project(":architecture:presentation"))
    implementation(project(":architecture:domain"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
}
"""
}
