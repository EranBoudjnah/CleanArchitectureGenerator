package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildAppGradleScript
import com.mitteloupe.cag.core.content.buildProjectGradleScript
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import java.io.File

class GradleFileCreator {
    fun writeGradleFileIfMissing(
        featureRoot: File,
        layer: String,
        content: String
    ) {
        val moduleDirectory = File(featureRoot, layer)
        val buildGradleFile = File(moduleDirectory, "build.gradle.kts")
        FileCreator.createFileIfNotExists(buildGradleFile) { content }
    }

    fun writeProjectGradleFile(
        projectRoot: File,
        enableKtlint: Boolean,
        enableDetekt: Boolean
    ) {
        val buildGradleFile = File(projectRoot, "build.gradle.kts")
        val content = buildProjectGradleScript(enableKtlint, enableDetekt)
        FileCreator.createFileIfNotExists(buildGradleFile) { content }
    }

    fun writeAppGradleFile(
        projectRoot: File,
        packageName: String,
        enableCompose: Boolean
    ) {
        val appGradleFile = File(projectRoot, "app/build.gradle.kts")
        val content = buildAppGradleScript(packageName, enableCompose)
        FileCreator.createFileIfNotExists(appGradleFile) { content }
    }
}
