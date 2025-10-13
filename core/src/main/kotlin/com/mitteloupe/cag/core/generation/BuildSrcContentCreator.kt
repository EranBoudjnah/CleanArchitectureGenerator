package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildBuildSrcGradleScript
import com.mitteloupe.cag.core.content.buildBuildSrcProjectJavaLibraryGradleScript
import com.mitteloupe.cag.core.content.buildBuildSrcSettingsGradleScript
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import java.io.File

class BuildSrcContentCreator(
    private val fileCreator: FileCreator
) {
    fun writeGradleFile(projectRoot: File) {
        val buildSrcDirectory = File(projectRoot, "buildSrc")
        if (!buildSrcDirectory.exists()) {
            buildSrcDirectory.mkdirs()
        }
        val buildSrcGradleFile = File(buildSrcDirectory, "build.gradle.kts")
        val content = buildBuildSrcGradleScript()
        fileCreator.createFileIfNotExists(buildSrcGradleFile) { content }
    }

    fun writeSettingsGradleFile(projectRoot: File) {
        val buildSrcDirectory = File(projectRoot, "buildSrc")
        if (!buildSrcDirectory.exists()) {
            buildSrcDirectory.mkdirs()
        }
        val buildSrcSettingsFile = File(buildSrcDirectory, "settings.gradle.kts")
        val content = buildBuildSrcSettingsGradleScript()
        fileCreator.createFileIfNotExists(buildSrcSettingsFile) { content }
    }

    fun writeProjectJavaLibraryFile(projectRoot: File) {
        val projectJavaLibrarySrcDirectory = File(projectRoot, "buildSrc/src/main/kotlin")
        if (!projectJavaLibrarySrcDirectory.exists()) {
            projectJavaLibrarySrcDirectory.mkdirs()
        }
        val projectJavaLibraryGradleFile = File(projectJavaLibrarySrcDirectory, "project-java-library.gradle.kts")
        val content = buildBuildSrcProjectJavaLibraryGradleScript()
        fileCreator.createFileIfNotExists(projectJavaLibraryGradleFile) { content }
    }
}
