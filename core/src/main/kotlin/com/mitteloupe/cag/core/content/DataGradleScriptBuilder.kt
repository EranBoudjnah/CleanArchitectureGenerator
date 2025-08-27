package com.mitteloupe.cag.core.content

fun buildDataGradleScript(featureNameLowerCase: String): String =
    """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.features.$featureNameLowerCase.domain)
    implementation(projects.architecture.domain)

    implementation(projects.datasource.architecture)
    implementation(projects.datasource.source)
}
"""
