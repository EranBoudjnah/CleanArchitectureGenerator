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
        FileCreator.createDirectoryIfNotExists(dependencyInjectionDirectory)
        val filename = "${featureName.capitalized}Module.kt"
        val targetFile = File(dependencyInjectionDirectory, filename)
        val content = buildAppFeatureModuleKotlinFile(projectNamespace, featurePackageName, featureName)
        FileCreator.createFileIfNotExists(targetFile) { content }
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

        FileCreator.createDirectoryIfNotExists(basePackageDir)

        val mainActivityFile = File(basePackageDir, "MainActivity.kt")
        val mainActivityContent = buildMainActivityKotlinFile(projectNamespace, enableCompose)
        FileCreator.createFileIfNotExists(mainActivityFile) { mainActivityContent }

        val applicationFile = File(basePackageDir, "Application.kt")
        val applicationContent = buildApplicationKotlinFile(projectNamespace)
        FileCreator.createFileIfNotExists(applicationFile) { applicationContent }

        generateAndroidResources(appModuleDirectory, packageName, enableCompose)
    }

    private fun generateAndroidResources(
        appModuleDirectory: File,
        packageName: String,
        enableCompose: Boolean
    ) {
        val manifestFile = File(appModuleDirectory, "src/main/AndroidManifest.xml")
        FileCreator.createFileIfNotExists(manifestFile) { buildAndroidManifest(packageName) }

        val valuesDirectory = File(appModuleDirectory, "src/main/res/values")
        FileCreator.createDirectoryIfNotExists(valuesDirectory)
        val stringsFile = File(valuesDirectory, "strings.xml")
        FileCreator.createFileIfNotExists(stringsFile) { buildStringsXml(packageName) }

        if (enableCompose) {
            val themeFile = File(valuesDirectory, "themes.xml")
            FileCreator.createFileIfNotExists(themeFile) { buildThemesXml(packageName) }

            val uiDirectory = File(appModuleDirectory, "src/main/java/${packageName.replace('.', '/')}/ui/theme")
            FileCreator.createDirectoryIfNotExists(uiDirectory)
            val themeKtFile = File(uiDirectory, "Theme.kt")
            FileCreator.createFileIfNotExists(themeKtFile) { buildThemeKt(packageName) }

            val colorsKtFile = File(uiDirectory, "Color.kt")
            FileCreator.createFileIfNotExists(colorsKtFile) { buildColorsKt(packageName) }

            val typographyKtFile = File(uiDirectory, "Type.kt")
            FileCreator.createFileIfNotExists(typographyKtFile) { buildTypographyKt(packageName) }
        }

        val backupRulesFile = File(valuesDirectory, "backup_rules.xml")
        FileCreator.createFileIfNotExists(backupRulesFile) { buildBackupRulesXml() }

        val dataExtractionRulesFile = File(valuesDirectory, "data_extraction_rules.xml")
        FileCreator.createFileIfNotExists(dataExtractionRulesFile) { buildDataExtractionRulesXml() }
    }
}
