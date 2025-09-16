package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildAppGradleScript(
    packageName: String,
    enableCompose: Boolean,
    catalog: VersionCatalogReader
): String {
    val pluginAliasComposeCompiler = catalog.getResolvedPluginAliasFor(PluginConstants.COMPOSE_COMPILER).asAccessor

    val composePlugins =
        if (enableCompose) {
            "\n            alias(libs.plugins.$pluginAliasComposeCompiler)"
        } else {
            ""
        }

    val viewDependencies =
        if (enableCompose) {
            val aliasComposeBom = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_BOM).asAccessor
            val aliasComposeUi = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_UI).asAccessor
            val aliasComposeUiToolingPreview =
                catalog.getResolvedLibraryAliasForModule(
                    LibraryConstants.COMPOSE_UI_TOOLING_PREVIEW
                ).asAccessor
            val aliasComposeMaterial3 = catalog.getResolvedLibraryAliasForModule(LibraryConstants.COMPOSE_MATERIAL3).asAccessor
            val aliasComposeUiTooling = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_UI_TOOLING).asAccessor

            val aliasActivityCompose = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_ACTIVITY_COMPOSE).asAccessor
            """
            implementation(libs.$aliasActivityCompose)
            implementation(platform(libs.$aliasComposeBom))
            implementation(libs.$aliasComposeUi)
            implementation(libs.$aliasComposeUiToolingPreview)
            implementation(libs.$aliasComposeMaterial3)
            debugImplementation(libs.$aliasComposeUiTooling)
            debugImplementation(libs.compose.ui.test.manifest)
"""
        } else {
            val aliasAndroidxAppcompat = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_APPCOMPAT).asAccessor
            val aliasMaterial = catalog.getResolvedLibraryAliasForModule(LibraryConstants.MATERIAL).asAccessor
            val aliasConstraintLayout = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_CONSTRAINTLAYOUT).asAccessor
            """
            implementation(libs.$aliasAndroidxAppcompat)
            implementation(libs.$aliasConstraintLayout)
            implementation(libs.$aliasMaterial)
"""
        }

    val aliasAndroidApplication = catalog.getResolvedPluginAliasFor(PluginConstants.ANDROID_APPLICATION).asAccessor
    val aliasKotlinAndroid = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_ANDROID).asAccessor
    val aliasMaterial = catalog.getResolvedLibraryAliasForModule(LibraryConstants.MATERIAL).asAccessor
    val aliasCoreKtx = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_CORE_KTX).asAccessor
    val aliasLifecycleRuntimeKtx = catalog.getResolvedLibraryAliasForModule(LibraryConstants.ANDROIDX_LIFECYCLE_RUNTIME_KTX).asAccessor
    val aliasTestJunit = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_JUNIT).asAccessor
    val aliasTestAndroidxJunit = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_ANDROIDX_JUNIT).asAccessor
    val aliasTestAndroidxEspressoCore = catalog.getResolvedLibraryAliasForModule(LibraryConstants.TEST_ANDROIDX_ESPRESSO_CORE).asAccessor
    val result =
        """
        plugins {
            alias(libs.plugins.$aliasAndroidApplication)
            alias(libs.plugins.$aliasKotlinAndroid)$composePlugins
        }
        
        android {
            namespace = "$packageName"
            compileSdk = libs.versions.compileSdk.get().toInt()
        
            defaultConfig {
                applicationId = "$packageName"
                minSdk = libs.versions.minSdk.get().toInt()
                targetSdk = libs.versions.targetSdk.get().toInt()
                versionCode = 1
                versionName = "1.0"
        
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            kotlinOptions {
                jvmTarget = "17"
            }${
            if (enableCompose) {
                """
            buildFeatures {
                compose = true
            }"""
            } else {
                ""
            }
        }
        }
            
        dependencies {
            implementation(libs.$aliasMaterial)
            implementation(libs.$aliasCoreKtx)
            implementation(libs.$aliasLifecycleRuntimeKtx)$viewDependencies
            implementation(projects.architecture.ui)
            implementation(projects.architecture.presentation)
            implementation(projects.architecture.domain)
            implementation(projects.features.samplefeature.ui)
            implementation(projects.features.samplefeature.presentation)
            implementation(projects.features.samplefeature.domain)
            implementation(projects.features.samplefeature.data)
            implementation(projects.datasource.source)
            implementation(projects.datasource.implementation)
                
            testImplementation(libs.$aliasTestJunit)
            androidTestImplementation(libs.$aliasTestAndroidxJunit)
            androidTestImplementation(libs.$aliasTestAndroidxEspressoCore)
        }
        """.trimIndent()
    return result
}
