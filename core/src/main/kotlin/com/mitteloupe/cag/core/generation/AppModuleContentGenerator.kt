package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.AppModuleDirectoryFinder
import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildAppFeatureModuleKotlinFile
import com.mitteloupe.cag.core.content.buildApplicationKotlinFile
import com.mitteloupe.cag.core.content.buildMainActivityKotlinFile
import com.mitteloupe.cag.core.content.capitalized
import com.mitteloupe.cag.core.findGradleProjectRoot
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class AppModuleContentGenerator(private val directoryFinder: DirectoryFinder = DirectoryFinder()) {
    fun writeFeatureModuleIfPossible(
        startDirectory: File,
        projectNamespace: String,
        featureName: String,
        featurePackageName: String
    ) {
        val projectRoot = findGradleProjectRoot(startDirectory, directoryFinder) ?: startDirectory
        val appModuleDirectory =
            AppModuleDirectoryFinder(directoryFinder)
                .findAndroidAppModuleDirectories(projectRoot).firstOrNull()
                ?: return
        val packageName = projectNamespace.trimEnd('.')
        val sourceRoot = File(appModuleDirectory, "src/main/java")
        val basePackageDir = buildPackageDirectory(sourceRoot, packageName.toSegments())
        val dependencyInjectionDirectory = File(basePackageDir, "di")
        if (!dependencyInjectionDirectory.exists()) {
            val created =
                runCatching { dependencyInjectionDirectory.mkdirs() }
                    .getOrElse { false }
            if (!created) {
                val absolutePath = dependencyInjectionDirectory.absolutePath
                throw GenerationException("Failed to create directory: $absolutePath")
            }
        }
        val filename = "${featureName.capitalized}Module.kt"
        val targetFile = File(dependencyInjectionDirectory, filename)
        if (!targetFile.exists()) {
            val content = buildAppFeatureModuleKotlinFile(projectNamespace, featurePackageName, featureName)
            runCatching { targetFile.writeText(content) }
                .onFailure {
                    val absolutePath = targetFile.absolutePath
                    throw GenerationException("Failed to create file: $absolutePath: ${it.message}")
                }
        }
    }

    fun writeAppModule(
        startDirectory: File,
        projectNamespace: String,
        enableCompose: Boolean
    ) {
        val appModuleDirectory = File(startDirectory, "app")
        val packageName = projectNamespace.trimEnd('.')
        val sourceRoot = File(appModuleDirectory, "src/main/java")
        val basePackageDir = buildPackageDirectory(sourceRoot, packageName.toSegments())

        if (!basePackageDir.exists()) {
            val created = runCatching { basePackageDir.mkdirs() }.getOrElse { false }
            if (!created) {
                val absolutePath = basePackageDir.absolutePath
                throw GenerationException("Failed to create directory: $absolutePath")
            }
        }

        val mainActivityFile = File(basePackageDir, "MainActivity.kt")
        if (!mainActivityFile.exists()) {
            val content = buildMainActivityKotlinFile(projectNamespace, enableCompose)
            runCatching { mainActivityFile.writeText(content) }
                .onFailure {
                    val absolutePath = mainActivityFile.absolutePath
                    throw GenerationException("Failed to create file: $absolutePath: ${it.message}")
                }
        }

        val applicationFile = File(basePackageDir, "Application.kt")
        if (!applicationFile.exists()) {
            val content = buildApplicationKotlinFile(projectNamespace)
            runCatching { applicationFile.writeText(content) }
                .onFailure {
                    val absolutePath = applicationFile.absolutePath
                    throw GenerationException("Failed to create file: $absolutePath: ${it.message}")
                }
        }
    }
}
