package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.architecture.buildCoroutineGradleScript
import com.mitteloupe.cag.core.generation.GradleFileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.DependencyConfiguration
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class CoroutineModuleContentGenerator {
    fun generate(
        projectRoot: File,
        coroutinePackageName: String
    ) {
        val packageSegments = coroutinePackageName.toSegments()
        if (packageSegments.isEmpty()) {
            throw GenerationException("Coroutine package name is invalid.")
        }

        val coroutineRoot = File(projectRoot, "coroutine")
        val coroutineSourceRoot = File(coroutineRoot, "src/main/java")
        val destinationDirectory = buildPackageDirectory(coroutineSourceRoot, packageSegments)
        if (!destinationDirectory.exists()) {
            if (!destinationDirectory.mkdirs()) {
                throw GenerationException("Failed to create directories for coroutine package '$coroutinePackageName.coroutine'.")
            }
        }

        val catalogUpdater = VersionCatalogUpdater()
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = VersionCatalogConstants.BASIC_VERSIONS,
                libraries = LibraryConstants.CORE_ANDROID_LIBRARIES,
                plugins = PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS
            )
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = projectRoot,
            dependencyConfiguration = dependencyConfiguration
        )

        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = coroutineRoot,
            layer = "",
            content = buildCoroutineGradleScript(catalogUpdater)
        )
        CoroutineModuleCreator().generateCoroutineContent(coroutineRoot, coroutinePackageName, packageSegments)
    }
}
