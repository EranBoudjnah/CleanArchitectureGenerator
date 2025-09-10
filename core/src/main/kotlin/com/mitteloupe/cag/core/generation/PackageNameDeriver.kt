package com.mitteloupe.cag.core.generation

import java.io.File

object PackageNameDeriver {
    fun derivePackageNameForDirectory(directory: File): String? {
        val absolutePath = directory.absolutePath
        val marker =
            listOf("src/main/java", "src/main/kotlin").firstOrNull { absolutePath.contains(it) }
                ?: return null
        val afterMarker = absolutePath.substringAfter(marker).trimStart(File.separatorChar)
        if (afterMarker.isEmpty()) {
            return ""
        }
        val segments = afterMarker.split(File.separatorChar).filter { it.isNotEmpty() }
        return segments.joinToString(separator = ".")
    }
}
