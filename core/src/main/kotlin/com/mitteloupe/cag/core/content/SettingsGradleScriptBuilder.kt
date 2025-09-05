package com.mitteloupe.cag.core.content

fun buildSettingsGradleScript(
    projectName: String,
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

    return """
        pluginManagement {
            repositories {
                google()
                mavenCentral()
                gradlePluginPortal()
            }
        }
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
            }
        }

        rootProject.name = "$projectName"
        include(":app")

        plugins {
            alias(libs.plugins.android.application) apply false
            alias(libs.plugins.kotlin.android) apply false$ktlintPlugins$detektPlugins
        }

        setOf(
            "ui",
            "instrumentation-test", 
            "presentation",
            "presentation-test",
            "domain"
        ).forEach { module ->
            include(":architecture:${'$'}module")
        }

        setOf(
            "ui",
            "presentation",
            "domain",
            "data"
        ).forEach { module ->
            include(":features:samplefeature:${'$'}module")
        }

        setOf(
            "source",
            "implementation"
        ).forEach { module ->
            include(":datasource:${'$'}module")
        }
        """.trimIndent()
}
