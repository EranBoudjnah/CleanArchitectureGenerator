package com.mitteloupe.cag.core.content

fun buildBuildSrcGradleScript(): String {
    return """plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
"""
}
