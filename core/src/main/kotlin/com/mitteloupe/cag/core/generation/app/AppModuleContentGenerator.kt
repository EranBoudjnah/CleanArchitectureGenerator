package com.mitteloupe.cag.core.generation.app

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
import com.mitteloupe.cag.core.generation.withoutSpaces
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import com.mitteloupe.cag.core.option.DependencyInjection
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
        val basePackageDirectory = buildPackageDirectory(sourceRoot, projectNamespace.toSegments())
        val dependencyInjectionDirectory = createDirectoryIfNotExists(basePackageDirectory, "di")
        createFileIfNotExists(dependencyInjectionDirectory, "${featureName.capitalized}Module.kt") {
            buildAppFeatureModuleKotlinFile(projectNamespace, featurePackageName, featureName)
        }
    }

    fun writeAppModule(
        startDirectory: File,
        appName: String,
        projectNamespace: String,
        dependencyInjection: DependencyInjection,
        enableCompose: Boolean
    ) {
        val appModuleDirectory = File(startDirectory, "app")
        val sourceRoot = File(appModuleDirectory, "src/main/java")
        val basePackageDirectory = buildPackageDirectory(sourceRoot, projectNamespace.toSegments())

        fileCreator.createDirectoryIfNotExists(basePackageDirectory)

        val sanitizedAppName = appName.withoutSpaces()
        createFileIfNotExists(basePackageDirectory, "MainActivity.kt") {
            buildMainActivityKotlinFile(
                appName = sanitizedAppName,
                projectNamespace = projectNamespace,
                enableCompose = enableCompose
            )
        }

        createFileIfNotExists(basePackageDirectory, "${sanitizedAppName}Application.kt") {
            buildApplicationKotlinFile(
                projectNamespace = projectNamespace,
                appName = sanitizedAppName,
                dependencyInjection = dependencyInjection
            )
        }

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
        val sanitizedAppName = appName.withoutSpaces()
        val manifestFile = File(appModuleDirectory, "src/main/AndroidManifest.xml")
        fileCreator.createOrUpdateFile(manifestFile) { buildAndroidManifest(sanitizedAppName) }

        val valuesDirectory = createDirectoryIfNotExists(appModuleDirectory, "src/main/res/values")
        createFileIfNotExists(valuesDirectory, "strings.xml") { buildStringsXml(appName) }
        val xmlDirectory = createDirectoryIfNotExists(appModuleDirectory, "src/main/res/xml")

        if (enableCompose) {
            createFileIfNotExists(valuesDirectory, "themes.xml") { buildThemesXml(sanitizedAppName) }

            val uiDirectory = createDirectoryIfNotExists(appModuleDirectory, "src/main/java/${packageName.replace('.', '/')}/ui/theme")
            createFileIfNotExists(uiDirectory, "Theme.kt") {
                buildThemeKt(appName = sanitizedAppName, packageName = packageName)
            }

            createFileIfNotExists(uiDirectory, "Color.kt") { buildColorsKt(packageName) }
            createFileIfNotExists(uiDirectory, "Type.kt") { buildTypographyKt(packageName) }
        }

        createFileIfNotExists(xmlDirectory, "backup_rules.xml") { buildBackupRulesXml() }
        createFileIfNotExists(xmlDirectory, "data_extraction_rules.xml") { buildDataExtractionRulesXml() }

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

    private fun createDirectoryIfNotExists(
        root: File,
        relativePath: String
    ) = File(root, relativePath).also(fileCreator::createDirectoryIfNotExists)

    private fun createFileIfNotExists(
        directory: File,
        filePath: String,
        contentProvider: () -> String
    ) {
        fileCreator.createFileIfNotExists(File(directory, filePath), contentProvider)
    }
}
