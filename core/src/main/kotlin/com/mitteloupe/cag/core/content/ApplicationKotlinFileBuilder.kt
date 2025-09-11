package com.mitteloupe.cag.core.content

fun buildApplicationKotlinFile(projectNamespace: String): String {
    val appName = projectNamespace.split('.').last().capitalized

    return """
        package $projectNamespace

        import android.app.Application

        class ${appName}Application : Application() {
            override fun onCreate() {
                super.onCreate()
            }
        }
        """.trimIndent()
}
