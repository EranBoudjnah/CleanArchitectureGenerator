package com.mitteloupe.cag.core.content

fun buildBuildSrcSettingsGradleScript(): String {
    return """rootProject.name = "buildSrc"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
"""
}
