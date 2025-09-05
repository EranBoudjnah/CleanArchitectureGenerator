package com.mitteloupe.cag.core.content

fun buildProjectGradleScript(
    enableKtlint: Boolean,
    enableDetekt: Boolean
): String {
    val ktlintPlugins =
        if (enableKtlint) {
            """
    alias(libs.plugins.ktlint)
"""
        } else {
            ""
        }

    val detektPlugins =
        if (enableDetekt) {
            """
    alias(libs.plugins.detekt)
"""
        } else {
            ""
        }

    val ktlintTasks =
        if (enableKtlint) {
            """
    ktlint {
        android.set(true)
        ignoreFailures.set(false)
        filter {
            exclude("**/build/**")
            include("**/*.kt")
            include("**/*.kts")
        }
    }
"""
        } else {
            ""
        }

    val detektTasks =
        if (enableDetekt) {
            """
    detekt {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom("${'$'}projectDir/config/detekt.yml")
        baseline = file("${'$'}projectDir/config/baseline.xml")
    }
"""
        } else {
            ""
        }

    return """
        plugins {
            alias(libs.plugins.android.application) apply false
            alias(libs.plugins.kotlin.android) apply false$ktlintPlugins$detektPlugins
        }

        tasks {
            withType<JavaCompile> {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions {
                    jvmTarget = "17"
                }
            }$ktlintTasks$detektTasks
        }
        """.trimIndent()
}
