package com.mitteloupe.cag.core

import java.io.File

class DirectoryFinder {
    fun findDirectory(
        startDirectory: File,
        predicate: (File) -> Boolean
    ): File? {
        var currentDirectory = startDirectory
        while (!predicate(currentDirectory)) {
            currentDirectory = currentDirectory.parentFile ?: return null
        }
        return currentDirectory
    }
}
