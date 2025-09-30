package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.AppModuleDirectoryFinder
import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.content.buildAndroidManifest
import com.mitteloupe.cag.core.content.buildAppFeatureModuleKotlinFile
import com.mitteloupe.cag.core.content.buildApplicationKotlinFile
import com.mitteloupe.cag.core.content.buildBackupRulesXml
import com.mitteloupe.cag.core.content.buildColorsKt
import com.mitteloupe.cag.core.content.buildDataExtractionRulesXml
import com.mitteloupe.cag.core.content.buildMainActivityKotlinFile
import com.mitteloupe.cag.core.content.buildStringsXml
import com.mitteloupe.cag.core.content.buildThemeKt
import com.mitteloupe.cag.core.content.buildThemesXml
import com.mitteloupe.cag.core.content.buildTypographyKt
import com.mitteloupe.cag.core.content.capitalized
import com.mitteloupe.cag.core.findGradleProjectRoot
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class AppModuleContentGenerator(
    private val fileCreator: FileCreator,
    private val directoryFinder: DirectoryFinder
) {
    fun writeFeatureModuleIfPossible(
        startDirectory: File,
        projectNamespace: String,
        featureName: String,
        featurePackageName: String,
        appModuleDirectory: File?
    ) {
        val rootDirectory =
            appModuleDirectory ?: run {
                val projectRoot = findGradleProjectRoot(startDirectory, directoryFinder) ?: startDirectory
                val appModuleDirectories =
                    AppModuleDirectoryFinder(directoryFinder).findAndroidAppModuleDirectories(projectRoot)
                if (appModuleDirectories.isEmpty()) {
                    return
                } else {
                    appModuleDirectories.first()
                }
            }
        val sourceRoot = File(rootDirectory, "src/main/java")
        val basePackageDir = buildPackageDirectory(sourceRoot, projectNamespace.toSegments())
        val dependencyInjectionDirectory = File(basePackageDir, "di")
        fileCreator.createDirectoryIfNotExists(dependencyInjectionDirectory)
        val filename = "${featureName.capitalized}Module.kt"
        val targetFile = File(dependencyInjectionDirectory, filename)
        val content = buildAppFeatureModuleKotlinFile(projectNamespace, featurePackageName, featureName)
        fileCreator.createFileIfNotExists(targetFile) { content }
    }

    fun writeAppModule(
        startDirectory: File,
        appName: String,
        projectNamespace: String,
        enableCompose: Boolean
    ) {
        val appModuleDirectory = File(startDirectory, "app")
        val sourceRoot = File(appModuleDirectory, "src/main/java")
        val basePackageDir = buildPackageDirectory(sourceRoot, projectNamespace.toSegments())

        fileCreator.createDirectoryIfNotExists(basePackageDir)

        val mainActivityFile = File(basePackageDir, "MainActivity.kt")
        val mainActivityContent =
            buildMainActivityKotlinFile(
                appName = appName,
                projectNamespace = projectNamespace,
                enableCompose = enableCompose
            )
        fileCreator.createFileIfNotExists(mainActivityFile) { mainActivityContent }

        val applicationFile = File(basePackageDir, "Application.kt")
        val applicationContent = buildApplicationKotlinFile(projectNamespace)
        fileCreator.createFileIfNotExists(applicationFile) { applicationContent }

        generateAndroidResources(
            appModuleDirectory = appModuleDirectory,
            appName = appName,
            packageName = projectNamespace,
            enableCompose = enableCompose
        )
    }

    private fun generateAndroidResources(
        appModuleDirectory: File,
        appName: String,
        packageName: String,
        enableCompose: Boolean
    ) {
        val manifestFile = File(appModuleDirectory, "src/main/AndroidManifest.xml")
        fileCreator.createOrUpdateFile(manifestFile) { buildAndroidManifest(appName) }

        val valuesDirectory = File(appModuleDirectory, "src/main/res/values")
        fileCreator.createDirectoryIfNotExists(valuesDirectory)
        val stringsFile = File(valuesDirectory, "strings.xml")
        fileCreator.createFileIfNotExists(stringsFile) { buildStringsXml(packageName) }
        val xmlDirectory = File(appModuleDirectory, "src/main/res/xml")
        fileCreator.createDirectoryIfNotExists(xmlDirectory)

        if (enableCompose) {
            val themeFile = File(valuesDirectory, "themes.xml")
            fileCreator.createFileIfNotExists(themeFile) { buildThemesXml(appName) }

            val uiDirectory = File(appModuleDirectory, "src/main/java/${packageName.replace('.', '/')}/ui/theme")
            fileCreator.createDirectoryIfNotExists(uiDirectory)
            val themeKtFile = File(uiDirectory, "Theme.kt")
            fileCreator.createFileIfNotExists(themeKtFile) { buildThemeKt(appName = appName, packageName = packageName) }

            val colorsKtFile = File(uiDirectory, "Color.kt")
            fileCreator.createFileIfNotExists(colorsKtFile) { buildColorsKt(packageName) }

            val typographyKtFile = File(uiDirectory, "Type.kt")
            fileCreator.createFileIfNotExists(typographyKtFile) { buildTypographyKt(packageName) }
        }

        val backupRulesFile = File(xmlDirectory, "backup_rules.xml")
        fileCreator.createFileIfNotExists(backupRulesFile) { buildBackupRulesXml() }

        val dataExtractionRulesFile = File(xmlDirectory, "data_extraction_rules.xml")
        fileCreator.createFileIfNotExists(dataExtractionRulesFile) { buildDataExtractionRulesXml() }

        copyMipmapResources(appModuleDirectory)
    }

    private fun copyMipmapResources(appModuleDirectory: File) {
        val mipmapDirectories =
            listOf(
                "drawable",
                "mipmap-anydpi-v26",
                "mipmap-hdpi",
                "mipmap-mdpi",
                "mipmap-xhdpi",
                "mipmap-xxhdpi",
                "mipmap-xxxhdpi"
            )

        mipmapDirectories.forEach { mipmapDirectory ->
            val targetDirectory = File(appModuleDirectory, "src/main/res/$mipmapDirectory")
            fileCreator.copyResourceDirectoryIfNotExists(
                targetDirectory = targetDirectory,
                resourcePath = mipmapDirectory,
                classLoader = javaClass.classLoader
            )
        }
    }
}
