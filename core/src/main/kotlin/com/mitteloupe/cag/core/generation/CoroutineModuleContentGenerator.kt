package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildCoroutineGradleScript
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class CoroutineModuleContentGenerator {
    fun generate(
        projectRoot: File,
        architecturePackageName: String
    ) {
        val packageSegments = architecturePackageName.toSegments()
        if (packageSegments.isEmpty()) {
            throw GenerationException("Architecture package name is invalid.")
        }

        val coroutineRoot = File(projectRoot, "coroutine")
        val coroutineSourceRoot = File(coroutineRoot, "src/main/java")
        val destinationDirectory = buildPackageDirectory(coroutineSourceRoot, "$architecturePackageName.coroutine".toSegments())
        if (!destinationDirectory.exists()) {
            if (!destinationDirectory.mkdirs()) {
                throw GenerationException("Failed to create directories for coroutine package '$architecturePackageName.coroutine'.")
            }
        }

        val catalogUpdater = VersionCatalogUpdater()
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = projectRoot,
            enableCompose = false
        )

        createCoroutineModule(coroutineRoot, catalogUpdater)
        generateCoroutineContent(coroutineRoot, architecturePackageName, architecturePackageName)
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
        architecturePackageName: String
    ) {
        val coroutineSourceRoot = File(coroutineRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(coroutineSourceRoot, "$architecturePackageName.coroutine".toSegments())

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
                """package $moduleNamespace.coroutine

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
