package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.AppModuleDirectoryFinder
import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildDataSourceModuleKotlinFile
import com.mitteloupe.cag.core.findGradleProjectRoot
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class DataSourceModuleCreator {
    fun writeDataSourceModule(
        destinationRootDirectory: File,
        projectNamespace: String,
        dataSourceName: String
    ) {
        val basePackage = projectNamespace.trimEnd('.')
        val projectRoot = findGradleProjectRoot(destinationRootDirectory, DirectoryFinder()) ?: destinationRootDirectory
        val appModuleDirectory =
            AppModuleDirectoryFinder(DirectoryFinder())
                .findAndroidAppModuleDirectories(projectRoot)
                .firstOrNull() ?: return
        val appSourceRoot = File(appModuleDirectory, "src/main/java")
        val basePackageDirectory = buildPackageDirectory(appSourceRoot, basePackage.toSegments())
        val targetDirectory = File(basePackageDirectory, "di")

        if (!targetDirectory.exists()) {
            val created = runCatching { targetDirectory.mkdirs() }.getOrElse { false }
            if (!created) {
                throw GenerationException("Failed to create directory: ${targetDirectory.absolutePath}")
            }
        }

        val fileName = "${dataSourceName}Module.kt"
        val targetFile = File(targetDirectory, fileName)
        if (!targetFile.exists()) {
            val dataSourceBaseName = dataSourceName.removeSuffix("DataSource")
            val dataSourcePackageName =
                (listOf(basePackage) + listOf("datasource", dataSourceBaseName.lowercase(), "datasource"))
                    .joinToString(".")

            val content =
                buildDataSourceModuleKotlinFile(
                    appPackageName = basePackage,
                    dataSourcePackageName = dataSourcePackageName,
                    dataSourceName = dataSourceName
                )

            runCatching { targetFile.writeText(content) }
                .onFailure {
                    throw GenerationException("Failed to create file: ${targetFile.absolutePath}: ${it.message}")
                }
        }
    }
}
