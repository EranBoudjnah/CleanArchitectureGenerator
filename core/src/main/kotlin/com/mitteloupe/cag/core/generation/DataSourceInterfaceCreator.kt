package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildDataSourceInterfaceKotlinFile
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class DataSourceInterfaceCreator(
    private val fileCreator: FileCreator
) {
    fun writeDataSourceInterface(
        destinationRootDirectory: File,
        projectNamespace: String,
        dataSourceName: String
    ) {
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        val sourceRoot = File(datasourceRoot, "source/src/main/java")

        val basePackageDirectory = buildPackageDirectory(sourceRoot, projectNamespace.toSegments())

        val dataSourceBaseName = dataSourceName.removeSuffix("DataSource")
        val targetDirectory =
            listOf("datasource", dataSourceBaseName.lowercase(), "datasource")
                .fold(basePackageDirectory) { parent, segment ->
                    File(parent, segment)
                }

        fileCreator.createDirectoryIfNotExists(targetDirectory)

        val targetFile = File(targetDirectory, "$dataSourceName.kt")
        val packageName =
            (listOf(projectNamespace) + listOf("datasource", dataSourceBaseName.lowercase(), "datasource"))
                .joinToString(".")
        val content =
            buildDataSourceInterfaceKotlinFile(
                packageName = packageName,
                dataSourceName = dataSourceName
            )
        fileCreator.createFileIfNotExists(targetFile) { content }
    }
}
