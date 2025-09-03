package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildDataSourceInterfaceKotlinFile
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

        if (!targetDirectory.exists()) {
            val created = runCatching { targetDirectory.mkdirs() }.getOrElse { false }
            if (!created) {
                throw GenerationException("Failed to create directory: ${targetDirectory.absolutePath}")
            }
        }

        val targetFile = File(targetDirectory, "$dataSourceName.kt")
        if (!targetFile.exists()) {
            val packageName =
                (listOf(basePackage) + listOf("datasource", dataSourceBaseName.lowercase(), "datasource"))
                    .joinToString(".")
            val content =
                buildDataSourceInterfaceKotlinFile(
                    packageName = packageName,
                    dataSourceName = dataSourceName
                )
            runCatching { targetFile.writeText(content) }
                .onFailure {
                    throw GenerationException("Failed to create file: ${targetFile.absolutePath}: ${it.message}")
                }
        }
    }
}
