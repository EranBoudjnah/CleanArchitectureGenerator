package com.mitteloupe.cag.core

import java.io.File

fun findGradleProjectRoot(
    startDirectory: File,
    directoryFinder: DirectoryFinder = DirectoryFinder()
): File? =
    directoryFinder.findDirectory(startDirectory) { currentDirectory ->
        val hasSettings =
            File(currentDirectory, "settings.gradle.kts").exists() ||
                File(currentDirectory, "settings.gradle").exists()
        val hasWrapper =
            File(currentDirectory, "gradlew").exists() ||
                File(currentDirectory, "gradlew.bat").exists()
        hasSettings || hasWrapper
    }
