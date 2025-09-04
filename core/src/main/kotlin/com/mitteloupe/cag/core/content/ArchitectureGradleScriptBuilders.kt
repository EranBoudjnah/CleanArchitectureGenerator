package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildArchitectureDomainGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)
}

dependencies {
    implementation(projects.coroutine)
    implementation(libs.kotlinx.coroutines.core)
}
"""
}

fun buildArchitecturePresentationGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

dependencies {
    implementation(projects.architecture.domain)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit4)
}
"""
}

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

fun buildArchitectureInstrumentationTestGradleScript(
    architecturePackageName: String,
    catalog: VersionCatalogReader
): String {
    val aliasAndroidLibrary = (catalog.getResolvedPluginAliasFor("com.android.library") ?: "android-library").asAccessor
    val aliasKotlinAndroid = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.android") ?: "kotlin-android").asAccessor
    val aliasComposeCompiler = catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.plugin.compose")?.asAccessor

    val aliasMaterial = (catalog.getResolvedLibraryAliasForModule("com.google.android.material:material") ?: "material").asAccessor
    val aliasComposeBom = (catalog.getResolvedLibraryAliasForModule("androidx.compose:compose-bom") ?: "compose-bom").asAccessor
    val aliasComposeUi = (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui") ?: "compose-ui").asAccessor
    val aliasComposeUiGraphics =
        (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui-graphics") ?: "compose-ui-graphics").asAccessor
    val aliasComposeUiToolingPreview =
        (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui-tooling-preview") ?: "compose-ui-tooling-preview").asAccessor
    val aliasComposeMaterial3 =
        (catalog.getResolvedLibraryAliasForModule("androidx.compose.material3:material3") ?: "compose-material3").asAccessor
    val aliasTestJunit = (catalog.getResolvedLibraryAliasForModule("junit:junit") ?: "test-junit").asAccessor
    val aliasTestAndroidxJunit = (catalog.getResolvedLibraryAliasForModule("androidx.test.ext:junit") ?: "test-androidx-junit").asAccessor
    val aliasTestAndroidxEspressoCore =
        (catalog.getResolvedLibraryAliasForModule("androidx.test.espresso:espresso-core") ?: "test-androidx-espresso-core").asAccessor
    val aliasTestComposeUiJunit4 =
        (catalog.getResolvedLibraryAliasForModule("androidx.compose.ui:ui-test-junit4") ?: "test-compose-ui-junit4").asAccessor
    val aliasTestAndroidHilt =
        (catalog.getResolvedLibraryAliasForModule("com.google.dagger:hilt-android-testing") ?: "test-android-hilt").asAccessor
    val aliasTestAndroidUiAutomator =
        (catalog.getResolvedLibraryAliasForModule("androidx.test.uiautomator:uiautomator") ?: "test-android-uiautomator").asAccessor
    val aliasOkhttp3 = (catalog.getResolvedLibraryAliasForModule("com.squareup.okhttp3:okhttp") ?: "okhttp3").asAccessor
    val aliasTestAndroidMockWebServer =
        (catalog.getResolvedLibraryAliasForModule("com.squareup.okhttp3:mockwebserver") ?: "test-android-mockwebserver").asAccessor
    val aliasAndroidxAppcompat =
        (catalog.getResolvedLibraryAliasForModule("androidx.appcompat:appcompat") ?: "androidx-appcompat").asAccessor
    val aliasTestAndroidxRules = (catalog.getResolvedLibraryAliasForModule("androidx.test:rules") ?: "test-androidx-rules").asAccessor
    val aliasAndroidxRecyclerview =
        (catalog.getResolvedLibraryAliasForModule("androidx.recyclerview:recyclerview") ?: "androidx-recyclerview").asAccessor

    val composePluginLine =
        if (aliasComposeCompiler != null) {
            "    alias(libs.plugins.$aliasComposeCompiler)\n"
        } else {
            ""
        }

    return """plugins {
    alias(libs.plugins.$aliasAndroidLibrary)
    alias(libs.plugins.$aliasKotlinAndroid)
$composePluginLine}

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
    implementation(libs.$aliasTestComposeUiJunit4)
    implementation(libs.$aliasTestAndroidHilt)
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

fun buildArchitecturePresentationTestGradleScript(catalog: VersionCatalogReader): String {
    val pluginAliasKotlinJvm = (catalog.getResolvedPluginAliasFor("org.jetbrains.kotlin.jvm") ?: "kotlin-jvm").asAccessor

    val aliasTestJunit = (catalog.getResolvedLibraryAliasForModule("junit:junit") ?: "test-junit").asAccessor
    val aliasTestMockitoKotlin =
        (catalog.getResolvedLibraryAliasForModule("org.mockito.kotlin:mockito-kotlin") ?: "test-mockito-kotlin").asAccessor
    val aliasTestKotlinxCoroutines =
        (catalog.getResolvedLibraryAliasForModule("org.jetbrains.kotlinx:kotlinx-coroutines-test") ?: "test-kotlinx-coroutines").asAccessor

    return """plugins {
    id("project-java-library")
    alias(libs.plugins.$pluginAliasKotlinJvm)
}

dependencies {
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.$aliasTestJunit)
    implementation(libs.$aliasTestMockitoKotlin)
    implementation(libs.$aliasTestKotlinxCoroutines)
    implementation(projects.coroutineTest)
}
"""
}
