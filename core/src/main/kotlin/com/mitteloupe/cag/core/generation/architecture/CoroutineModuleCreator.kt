package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.generation.generateFileIfMissing
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import java.io.File

internal class CoroutineModuleCreator {
    fun generateCoroutineContent(
        coroutineRoot: File,
        moduleNamespace: String,
        coroutinePackageNameSegments: List<String>
    ) {
        val coroutineSourceRoot = File(coroutineRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(coroutineSourceRoot, coroutinePackageNameSegments)

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
                """package $moduleNamespace

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
