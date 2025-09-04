package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.architecture.buildCoroutineGradleScript
import com.mitteloupe.cag.core.generation.GradleFileCreator
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
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = projectRoot,
            includeCoroutineDependencies = true
        )

        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = coroutineRoot,
            layer = "",
            content = buildCoroutineGradleScript(catalogUpdater)
        )
        CoroutineModuleCreator().generateCoroutineContent(coroutineRoot, coroutinePackageName, packageSegments)
    }
}
