package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildDataSourceInterfaceKotlinFile
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class DataSourceInterfaceCreator {
    fun writeDataSourceInterface(
        destinationRootDirectory: File,
        projectNamespace: String,
        dataSourceName: String
    ) {
        val basePackage = projectNamespace.trimEnd('.')
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        val sourceRoot = File(datasourceRoot, "source/src/main/java")

        val basePackageDirectory = buildPackageDirectory(sourceRoot, basePackage.toSegments())

        val dataSourceBaseName = dataSourceName.removeSuffix("DataSource")
        val targetDirectory =
            listOf("datasource", dataSourceBaseName.lowercase(), "datasource")
                .fold(basePackageDirectory) { parent, segment ->
                    File(parent, segment)
                }

        FileCreator.createDirectoryIfNotExists(targetDirectory)

        val targetFile = File(targetDirectory, "$dataSourceName.kt")
        val packageName =
            (listOf(basePackage) + listOf("datasource", dataSourceBaseName.lowercase(), "datasource"))
                .joinToString(".")
        val content =
            buildDataSourceInterfaceKotlinFile(
                packageName = packageName,
                dataSourceName = dataSourceName
            )
        FileCreator.createFileIfNotExists(targetFile) { content }
    }
}
