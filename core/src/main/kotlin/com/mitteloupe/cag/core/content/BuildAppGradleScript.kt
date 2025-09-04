package com.mitteloupe.cag.core.content

fun buildAppGradleScript(
    packageName: String,
    enableCompose: Boolean
): String {
    val composePlugins =
        if (enableCompose) {
            """
    alias(libs.plugins.kotlin.compose)
"""
        } else {
            ""
        }

    val composeDependencies =
        if (enableCompose) {
            """
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
"""
        } else {
            """
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.android.material)
"""
        }

    val result =
        """
            plugins {
                alias(libs.plugins.android.application)
                alias(libs.plugins.kotlin.android)$composePlugins
            }
            
            android {
                namespace = "$packageName"
                compileSdk = libs.versions.android.compileSdk.get().toInt()
            
                defaultConfig {
                    applicationId = "$packageName"
                    minSdk = libs.versions.android.minSdk.get().toInt()
                    targetSdk = libs.versions.android.targetSdk.get().toInt()
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
                        }
                        composeOptions {
                            kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
                        }"""
            } else {
                ""
            }
        }
            }
            
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)$composeDependencies
                
                implementation(project(":architecture:ui"))
                implementation(project(":architecture:presentation"))
                implementation(project(":architecture:domain"))
                implementation(project(":features:samplefeature:ui"))
                implementation(project(":features:samplefeature:presentation"))
                implementation(project(":features:samplefeature:domain"))
                implementation(project(":features:samplefeature:data"))
                implementation(project(":datasource:source"))
                implementation(project(":datasource:implementation"))
                
                testImplementation(libs.junit)
                androidTestImplementation(libs.androidx.junit)
                androidTestImplementation(libs.androidx.espresso.core)
            }
        """.trimIndent()
    return result
}
