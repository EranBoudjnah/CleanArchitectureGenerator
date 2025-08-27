package com.mitteloupe.cag.core.content

fun buildDomainGradleScript(): String =
    """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.architecture.domain)
}
"""
