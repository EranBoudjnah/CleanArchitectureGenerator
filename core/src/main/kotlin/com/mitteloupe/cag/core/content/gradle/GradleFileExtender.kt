package com.mitteloupe.cag.core.content.gradle

class GradleFileExtender internal constructor() {
    fun buildKtlintPluginLine(enableKtlint: Boolean): String =
        if (enableKtlint) {
            """    alias(libs.plugins.ktlint)
"""
        } else {
            ""
        }

    fun buildDetektPluginLine(enableDetekt: Boolean): String =
        if (enableDetekt) {
            """    alias(libs.plugins.detekt)
"""
        } else {
            ""
        }

    fun buildKtlintConfiguration(enableKtlint: Boolean): String =
        if (enableKtlint) {
            """
ktlint {
    version.set("0.49.1")
    android.set(true)
}
"""
        } else {
            ""
        }

    fun buildDetektConfiguration(enableDetekt: Boolean): String =
        if (enableDetekt) {
            """
detekt {
    config.setFrom("${'$'}projectDir/../../detekt.yml")
}
"""
        } else {
            ""
        }
}
