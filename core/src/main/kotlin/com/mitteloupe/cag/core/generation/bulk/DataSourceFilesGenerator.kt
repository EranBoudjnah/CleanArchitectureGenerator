package com.mitteloupe.cag.core.generation.bulk

import com.mitteloupe.cag.core.generation.app.DataSourceDependencyInjectionModuleCreator
import com.mitteloupe.cag.core.generation.layer.DataSourceImplementationCreator
import com.mitteloupe.cag.core.generation.layer.DataSourceInterfaceCreator
import java.io.File

class DataSourceFilesGenerator(
    private val dataSourceInterfaceCreator: DataSourceInterfaceCreator,
    private val dataSourceImplementationCreator: DataSourceImplementationCreator,
    private val dataSourceDependencyInjectionModuleCreator: DataSourceDependencyInjectionModuleCreator
) {
    fun generateDataSource(
        destinationRootDirectory: File,
        dataSourceName: String,
        projectNamespace: String
    ) {
        dataSourceInterfaceCreator
            .writeDataSourceInterface(
                destinationRootDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                dataSourceName = dataSourceName
            )

        dataSourceImplementationCreator
            .writeDataSourceImplementation(
                destinationRootDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                dataSourceName = dataSourceName
            )

        dataSourceDependencyInjectionModuleCreator
            .writeDataSourceDependencyInjectionModule(
                destinationRootDirectory = destinationRootDirectory,
                projectNamespace = projectNamespace,
                dataSourceName = dataSourceName
            )
    }
}
