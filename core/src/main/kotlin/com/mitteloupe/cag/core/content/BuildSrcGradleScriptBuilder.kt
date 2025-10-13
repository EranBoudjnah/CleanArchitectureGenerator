package com.mitteloupe.cag.core.content

fun buildBuildSrcGradleScript(): String =
    """plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
"""
