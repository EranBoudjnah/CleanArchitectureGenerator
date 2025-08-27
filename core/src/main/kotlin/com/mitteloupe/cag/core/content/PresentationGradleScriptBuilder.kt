package com.mitteloupe.cag.core.content

fun buildPresentationGradleScript(featureNameLowerCase: String): String =
    """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.features.$featureNameLowerCase.domain)
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)
}
"""
