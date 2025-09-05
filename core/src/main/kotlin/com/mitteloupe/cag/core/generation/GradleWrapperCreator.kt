package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.buildGradleWrapperPropertiesFile
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import java.io.File

class GradleWrapperCreator {
    fun writeGradleWrapperFiles(projectRoot: File) {
        val gradleWrapperDirectory = File(projectRoot, "gradle/wrapper")
        FileCreator.createDirectoryIfNotExists(gradleWrapperDirectory)

        val gradleWrapperPropertiesFile = File(gradleWrapperDirectory, "gradle-wrapper.properties")
        val gradleWrapperPropertiesContent = buildGradleWrapperPropertiesFile()

        FileCreator.createFileIfNotExists(gradleWrapperPropertiesFile) { gradleWrapperPropertiesContent }

        val gradleWrapperJarFile = File(gradleWrapperDirectory, "gradle-wrapper.jar")
        FileCreator.createBinaryFileIfNotExists(gradleWrapperJarFile) { getGradleWrapperResourceAsBytes() }

        val gradlewFile = File(projectRoot, "gradlew")
        val gradlewContent = getResourceAsString("gradlew")
        FileCreator.createFileIfNotExists(gradlewFile) { gradlewContent }
        if (!gradlewFile.setExecutable(true)) {
            throw GenerationException("Failed to make gradlew executable")
        }

        val gradlewBatFile = File(projectRoot, "gradlew.bat")
        val gradlewBatContent = getResourceAsString("gradlew.bat")
        FileCreator.createFileIfNotExists(gradlewBatFile) { gradlewBatContent }
    }

    private fun getGradleWrapperResourceAsBytes(): ByteArray {
        val resourceName = "gradle-wrapper.jar"
        return javaClass.classLoader.getResourceAsStream(resourceName)?.readBytes()
            ?: throw GenerationException("Resource $resourceName not found in classpath")
    }

    private fun getResourceAsString(resourceName: String): String =
        javaClass.classLoader.getResourceAsStream(resourceName)?.bufferedReader()?.readText()
            ?: throw GenerationException("Resource $resourceName not found in classpath")
}
