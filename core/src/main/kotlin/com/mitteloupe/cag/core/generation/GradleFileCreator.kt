package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildAppGradleScript
import com.mitteloupe.cag.core.content.buildProjectGradleScript
import java.io.File

class GradleFileCreator {
    fun writeGradleFileIfMissing(
        featureRoot: File,
        layer: String,
        content: String
    ) {
        val moduleDirectory = File(featureRoot, layer)
        val buildGradleFile = File(moduleDirectory, "build.gradle.kts")
        if (!buildGradleFile.exists()) {
            runCatching { buildGradleFile.writeText(content) }
                .onFailure { throw GenerationException("Failed to create $layer/build.gradle.kts: ${it.message}") }
        }
    }

    fun writeProjectGradleFile(
        projectRoot: File,
        enableKtlint: Boolean,
        enableDetekt: Boolean
    ) {
        val buildGradleFile = File(projectRoot, "build.gradle.kts")
        val content = buildProjectGradleScript(enableKtlint, enableDetekt)
        runCatching { buildGradleFile.writeText(content) }
            .onFailure { throw GenerationException("Failed to create build.gradle.kts: ${it.message}") }
    }

    fun writeAppGradleFile(
        projectRoot: File,
        packageName: String,
        enableCompose: Boolean
    ) {
        val appGradleFile = File(projectRoot, "app/build.gradle.kts")
        val content = buildAppGradleScript(packageName, enableCompose)
        runCatching { appGradleFile.writeText(content) }
            .onFailure { throw GenerationException("Failed to create app/build.gradle.kts: ${it.message}") }
    }
}
