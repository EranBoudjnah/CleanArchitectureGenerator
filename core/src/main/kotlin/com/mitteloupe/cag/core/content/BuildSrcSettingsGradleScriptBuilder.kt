package com.mitteloupe.cag.core.content

fun buildBuildSrcSettingsGradleScript(): String =
    """rootProject.name = "buildSrc"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
"""
