package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildGradleWrapperPropertiesFile
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import java.io.File

class GradleWrapperCreator(
    private val fileCreator: FileCreator
) {
    fun writeGradleWrapperFiles(projectRoot: File) {
        val gradleWrapperDirectory = File(projectRoot, "gradle/wrapper")
        fileCreator.createDirectoryIfNotExists(gradleWrapperDirectory)

        val gradleWrapperPropertiesFile = File(gradleWrapperDirectory, "gradle-wrapper.properties")
        fileCreator.createOrUpdateFile(gradleWrapperPropertiesFile) { buildGradleWrapperPropertiesFile() }

        val gradleWrapperJarFile = File(gradleWrapperDirectory, "gradle-wrapper.jar")
        fileCreator.createBinaryFileIfNotExists(gradleWrapperJarFile) { getGradleWrapperResourceAsBytes() }

        val gradlewFile = File(projectRoot, "gradlew")
        fileCreator.createFileIfNotExists(gradlewFile) { getResourceAsString("gradlew") }
        if (!gradlewFile.setExecutable(true)) {
            throw GenerationException("Failed to make gradlew executable")
        }

        val gradlewBatFile = File(projectRoot, "gradlew.bat")
        val gradlewBatContent = getResourceAsString("gradlew.bat")
        fileCreator.createFileIfNotExists(gradlewBatFile) { gradlewBatContent }
    }

    private fun getGradleWrapperResourceAsBytes(): ByteArray {
        val resourceName = "gradle-wrapper.jar"
        return javaClass.classLoader.getResourceAsStream(resourceName)?.readBytes()
            ?: throw GenerationException("Resource $resourceName not found in classpath")
    }

    private fun getResourceAsString(resourceName: String): String =
        javaClass.classLoader
            .getResourceAsStream(resourceName)
            ?.bufferedReader()
            ?.readText()
            ?: throw GenerationException("Resource $resourceName not found in classpath")
}
