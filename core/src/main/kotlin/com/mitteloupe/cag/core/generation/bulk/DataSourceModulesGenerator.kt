package com.mitteloupe.cag.core.generation.bulk

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildDataSourceImplementationGradleScript
import com.mitteloupe.cag.core.content.buildDataSourceSourceGradleScript
import com.mitteloupe.cag.core.generation.SettingsFileUpdater
import com.mitteloupe.cag.core.generation.gradle.GradleFileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.DependencyConfiguration
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import java.io.File

class DataSourceModulesGenerator(
    private val catalogUpdater: VersionCatalogUpdater,
    private val gradleFileCreator: GradleFileCreator,
    private val settingsFileUpdater: SettingsFileUpdater
) {
    fun generateDataSourceModules(
        destinationRootDirectory: File,
        useKtor: Boolean,
        useRetrofit: Boolean
    ) {
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        val modules = listOf("source", "implementation")

        val allCreated =
            modules.all { moduleName ->
                val moduleDirectory = File(datasourceRoot, moduleName)
                if (moduleDirectory.exists()) {
                    moduleDirectory.isDirectory
                } else {
                    moduleDirectory.mkdirs()
                }
            }

        if (!allCreated) {
            throw GenerationException("Failed to create directories for datasource.")
        }

        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.ANDROID_VERSIONS,
                libraries = emptyList(),
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )
        catalogUpdater.createOrUpdateVersionCatalog(
            projectRootDirectory = destinationRootDirectory,
            dependencyConfiguration = dependencyConfiguration
        )

        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = datasourceRoot,
            layer = "source",
            content = buildDataSourceSourceGradleScript(catalogUpdater)
        )

        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = datasourceRoot,
            layer = "implementation",
            content =
                buildDataSourceImplementationGradleScript(
                    catalog = catalogUpdater,
                    useKtor = useKtor,
                    useRetrofit = useRetrofit
                )
        )

        settingsFileUpdater.updateDataSourceSettingsIfPresent(destinationRootDirectory)
    }
}
