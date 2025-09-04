package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.architecture.buildCoroutineGradleScript
import com.mitteloupe.cag.core.generation.GradleFileCreator
import com.mitteloupe.cag.core.generation.generateFileIfMissing
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

        createCoroutineModule(coroutineRoot, catalogUpdater)
        generateCoroutineContent(coroutineRoot, coroutinePackageName, packageSegments)
    }

    private fun createCoroutineModule(
        coroutineRoot: File,
        catalog: VersionCatalogUpdater
    ) {
        GradleFileCreator().writeGradleFileIfMissing(
            featureRoot = coroutineRoot,
            layer = "",
            content = buildCoroutineGradleScript(catalog)
        )
    }

    private fun generateCoroutineContent(
        coroutineRoot: File,
        moduleNamespace: String,
        coroutinePackageNameSegments: List<String>
    ) {
        val coroutineSourceRoot = File(coroutineRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(coroutineSourceRoot, coroutinePackageNameSegments)

        generateCoroutineContextProvider(packageDirectory, moduleNamespace)
    }

    private fun generateCoroutineContextProvider(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "CoroutineContextProvider.kt",
            content =
                """package $moduleNamespace

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

interface CoroutineContextProvider {
    val main: CoroutineContext
    val io: CoroutineContext

    object Default : CoroutineContextProvider {
        override val main: CoroutineContext = Dispatchers.Main
        override val io: CoroutineContext = Dispatchers.IO
    }
}
""",
            errorMessage = "coroutine context provider"
        )
    }
}
