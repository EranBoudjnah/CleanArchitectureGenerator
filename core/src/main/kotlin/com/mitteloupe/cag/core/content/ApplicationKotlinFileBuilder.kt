package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.format.optimizeImports
import com.mitteloupe.cag.core.option.DependencyInjection

fun buildApplicationKotlinFile(
    projectNamespace: String,
    appName: String,
    dependencyInjection: DependencyInjection
): String =
    when (dependencyInjection) {
        DependencyInjection.Hilt -> {
            """
            package $projectNamespace

            import android.app.Application
            import dagger.hilt.android.HiltAndroidApp

            @HiltAndroidApp
            class ${appName}Application : Application()

            """.trimIndent()
        }
        DependencyInjection.Koin -> {
            val optimizedImports =
                """import $projectNamespace.di.architectureModule
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
""".optimizeImports()

            """
package $projectNamespace

$optimizedImports
class ${appName}Application : Application() {
    private fun initKoin(config : KoinAppDeclaration? = null){
        startKoin {
            includes(config)
            modules(architectureModule)
        }
    }

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@${appName}Application)
            androidLogger()
        }
    }
}
"""
        }
        DependencyInjection.None -> {
            """
            package $projectNamespace

            import android.app.Application

            class ${appName}Application : Application()

            """.trimIndent()
        }
    }
