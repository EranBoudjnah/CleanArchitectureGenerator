package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import com.mitteloupe.cag.core.content.buildDataSourceImplementationKotlinFile
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class DataSourceImplementationCreator {
    fun writeDataSourceImplementation(
        destinationRootDirectory: File,
        projectNamespace: String,
        dataSourceName: String
    ): String? {
        val basePackage = projectNamespace.trimEnd('.')
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        val implSourceRoot = File(datasourceRoot, "implementation/src/main/java")

        val basePackageDirectory = buildPackageDirectory(implSourceRoot, basePackage.toSegments())

        val dataSourceBaseName = dataSourceName.removeSuffix("DataSource")
        val targetDirectory =
            listOf("datasource", dataSourceBaseName.lowercase(), "datasource")
                .fold(basePackageDirectory) { parent, segment ->
                    File(parent, segment)
                }

        if (!targetDirectory.exists()) {
            val created = runCatching { targetDirectory.mkdirs() }.getOrElse { false }
            if (!created) {
                return "${ERROR_PREFIX}Failed to create directory: ${targetDirectory.absolutePath}"
            }
        }

        val fileName = "${dataSourceName}Impl.kt"
        val targetFile = File(targetDirectory, fileName)
        if (!targetFile.exists()) {
            val packageName =
                (listOf(basePackage) + listOf("datasource", dataSourceBaseName.lowercase(), "datasource"))
                    .joinToString(".")

            val content =
                buildDataSourceImplementationKotlinFile(
                    packageName = packageName,
                    dataSourceName = dataSourceName
                )

            runCatching { targetFile.writeText(content) }
                .onFailure {
                    return "${ERROR_PREFIX}Failed to create file: ${targetFile.absolutePath}: ${it.message}"
                }
        }

        return null
    }
}
