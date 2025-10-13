package com.mitteloupe.cag.core.content

fun buildApplicationKotlinFile(
    projectNamespace: String,
    appName: String
): String =
    """
    package $projectNamespace

    import android.app.Application
    import dagger.hilt.android.HiltAndroidApp

    @HiltAndroidApp
    class ${appName}Application : Application()

    """.trimIndent()
