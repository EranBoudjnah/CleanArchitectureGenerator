package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.AppModuleDirectoryFinder
import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.content.buildDataSourceModuleKotlinFile
import com.mitteloupe.cag.core.findGradleProjectRoot
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class DataSourceModuleCreator {
    fun writeDataSourceModule(
        destinationRootDirectory: File,
        projectNamespace: String,
        dataSourceName: String
    ) {
        val projectRoot = findGradleProjectRoot(destinationRootDirectory, DirectoryFinder()) ?: destinationRootDirectory
        val appModuleDirectory =
            AppModuleDirectoryFinder(DirectoryFinder())
                .findAndroidAppModuleDirectories(projectRoot)
                .firstOrNull() ?: return
        val appSourceRoot = File(appModuleDirectory, "src/main/java")
        val basePackageDirectory = buildPackageDirectory(appSourceRoot, projectNamespace.toSegments())
        val targetDirectory = File(basePackageDirectory, "di")

        FileCreator.createDirectoryIfNotExists(targetDirectory)

        val fileName = "${dataSourceName}Module.kt"
        val targetFile = File(targetDirectory, fileName)
        val dataSourceBaseName = dataSourceName.removeSuffix("DataSource")
        val dataSourcePackageName =
            (listOf(projectNamespace) + listOf("datasource", dataSourceBaseName.lowercase(), "datasource"))
                .joinToString(".")

        val content =
            buildDataSourceModuleKotlinFile(
                appPackageName = projectNamespace,
                dataSourcePackageName = dataSourcePackageName,
                dataSourceName = dataSourceName
            )

        FileCreator.createFileIfNotExists(targetFile) { content }
    }
}
