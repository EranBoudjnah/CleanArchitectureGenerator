package com.mitteloupe.cag.core.generation.layer

import com.mitteloupe.cag.core.content.buildDataSourceImplementationKotlinFile
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class DataSourceImplementationCreator(
    private val fileCreator: FileCreator
) {
    fun writeDataSourceImplementation(
        destinationRootDirectory: File,
        projectNamespace: String,
        dataSourceName: String
    ) {
        val dataSourceRoot = File(destinationRootDirectory, "datasource")
        val implementationSourceRoot = File(dataSourceRoot, "implementation/src/main/java")

        val basePackageDirectory = buildPackageDirectory(implementationSourceRoot, projectNamespace.toSegments())

        val dataSourceBaseName = dataSourceName.removeSuffix("DataSource")
        val targetDirectory =
            listOf("datasource", dataSourceBaseName.lowercase(), "datasource")
                .fold(basePackageDirectory) { parent, segment ->
                    File(parent, segment)
                }

        fileCreator.createDirectoryIfNotExists(targetDirectory)

        val fileName = "${dataSourceName}Impl.kt"
        val targetFile = File(targetDirectory, fileName)
        fileCreator.createFileIfNotExists(targetFile) {
            val packageName =
                listOf(projectNamespace, "datasource", dataSourceBaseName.lowercase(), "datasource")
                    .joinToString(".")

            buildDataSourceImplementationKotlinFile(
                packageName = packageName,
                dataSourceName = dataSourceName
            )
        }
    }
}
