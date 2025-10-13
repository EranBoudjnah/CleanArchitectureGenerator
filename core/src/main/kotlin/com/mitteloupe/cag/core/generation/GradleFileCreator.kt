package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildAppGradleScript
import com.mitteloupe.cag.core.content.buildProjectGradleScript
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import java.io.File

class GradleFileCreator(
    private val fileCreator: FileCreator
) {
    fun writeGradleFileIfMissing(
        featureRoot: File,
        layer: String,
        content: String
    ) {
        val moduleDirectory = File(featureRoot, layer)
        val buildGradleFile = File(moduleDirectory, "build.gradle.kts")
        fileCreator.createFileIfNotExists(buildGradleFile) { content }
    }

    fun writeProjectGradleFile(
        projectRoot: File,
        enableKtlint: Boolean,
        enableDetekt: Boolean,
        catalog: VersionCatalogReader
    ) {
        val buildGradleFile = File(projectRoot, "build.gradle.kts")
        val content = buildProjectGradleScript(enableKtlint, enableDetekt, catalog)
        fileCreator.createOrUpdateFile(buildGradleFile) { content }
    }

    fun writeAppGradleFile(
        projectRoot: File,
        packageName: String,
        enableCompose: Boolean,
        catalog: VersionCatalogReader
    ) {
        val appGradleFile = File(projectRoot, "app/build.gradle.kts")
        val content = buildAppGradleScript(packageName, enableCompose, catalog)
        fileCreator.createOrUpdateFile(appGradleFile) { content }
    }
}
