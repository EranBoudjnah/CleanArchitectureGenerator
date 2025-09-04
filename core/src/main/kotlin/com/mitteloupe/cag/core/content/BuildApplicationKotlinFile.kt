package com.mitteloupe.cag.core.content

fun buildApplicationKotlinFile(projectNamespace: String): String {
    val packageName = projectNamespace.trimEnd('.')
    val appName = packageName.split('.').last().capitalized

    return """
        package $packageName

        import android.app.Application

        class ${appName}Application : Application() {
            override fun onCreate() {
                super.onCreate()
            }
        }
        """.trimIndent()
}
