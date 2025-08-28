package com.mitteloupe.cag.core.content

fun buildAppFeatureModuleKotlinFile(
    projectNamespace: String,
    featureName: String
): String {
    val className = featureName.capitalized
    val appPackage = projectNamespace.trimEnd('.')
    return """package $appPackage.di

object ${className}Module
"""
}
