package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildDataSourceImplementationKotlinFile
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class DataSourceImplementationCreator {
    fun writeDataSourceImplementation(
        destinationRootDirectory: File,
        projectNamespace: String,
        dataSourceName: String
    ) {
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        val implSourceRoot = File(datasourceRoot, "implementation/src/main/java")

        val basePackageDirectory = buildPackageDirectory(implSourceRoot, projectNamespace.toSegments())

        val dataSourceBaseName = dataSourceName.removeSuffix("DataSource")
        val targetDirectory =
            listOf("datasource", dataSourceBaseName.lowercase(), "datasource")
                .fold(basePackageDirectory) { parent, segment ->
                    File(parent, segment)
                }

        FileCreator.createDirectoryIfNotExists(targetDirectory)

        val fileName = "${dataSourceName}Impl.kt"
        val targetFile = File(targetDirectory, fileName)
        val packageName =
            (listOf(projectNamespace) + listOf("datasource", dataSourceBaseName.lowercase(), "datasource"))
                .joinToString(".")

        val content =
            buildDataSourceImplementationKotlinFile(
                packageName = packageName,
                dataSourceName = dataSourceName
            )

        FileCreator.createFileIfNotExists(targetFile) { content }
    }
}
