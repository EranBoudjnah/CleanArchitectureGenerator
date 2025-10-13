package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.content.gradle.GradleFileExtender
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import com.mitteloupe.cag.core.generation.versioncatalog.asAccessor

fun buildProjectGradleScript(
    enableKtlint: Boolean,
    enableDetekt: Boolean,
    catalog: VersionCatalogReader
): String {
    val ktlintPlugins =
        if (enableKtlint) {
            GradleFileExtender().buildKtlintPluginLine(catalog, 3).let { pluginLine ->
                if (pluginLine.isNotEmpty()) {
                    "$pluginLine apply false"
                } else {
                    ""
                }
            }
        } else {
            ""
        }

    val detektPlugins =
        if (enableDetekt) {
            GradleFileExtender().buildDetektPluginLine(catalog, 3).let { pluginLine ->
                if (pluginLine.isNotEmpty()) {
                    "$pluginLine apply false"
                } else {
                    ""
                }
            }
        } else {
            ""
        }

    val ktlintTasks = ""
    val detektTasks = ""

    val aliasAndroidApplication = catalog.getResolvedPluginAliasFor(PluginConstants.ANDROID_APPLICATION).asAccessor
    val aliasAndroidLibrary = catalog.getResolvedPluginAliasFor(PluginConstants.ANDROID_LIBRARY).asAccessor
    val aliasKotlinAndroid = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_ANDROID).asAccessor
    val aliasKotlinJvm = catalog.getResolvedPluginAliasFor(PluginConstants.KOTLIN_JVM).asAccessor
    val aliasHilt = catalog.getResolvedPluginAliasFor(PluginConstants.HILT_ANDROID).asAccessor
    val aliasKsp = catalog.getResolvedPluginAliasFor(PluginConstants.KSP).asAccessor

    return """
        // Top-level build file where you can add configuration options common to all sub-projects/modules.
        import org.jetbrains.kotlin.gradle.dsl.JvmTarget
        import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

        plugins {
            alias(libs.plugins.$aliasAndroidApplication) apply false
            alias(libs.plugins.$aliasAndroidLibrary) apply false
            alias(libs.plugins.$aliasKotlinAndroid) apply false
            alias(libs.plugins.$aliasKotlinJvm) apply false
            alias(libs.plugins.$aliasHilt) apply false
            alias(libs.plugins.$aliasKsp) apply false$ktlintPlugins$detektPlugins
        }

        tasks {
            withType<JavaCompile> {
                sourceCompatibility = JavaVersion.VERSION_17.toString()
                targetCompatibility = JavaVersion.VERSION_17.toString()
            }$ktlintTasks$detektTasks
        }

        subprojects {
            tasks.withType<KotlinCompile> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                    freeCompilerArgs.add("-Xskip-prerelease-check")
                }
            }
        }

        """.trimIndent()
}
