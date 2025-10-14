package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.generation.gradle.GradleFileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class CoroutineModuleContentGeneratorTest {
    private lateinit var classUnderTest: CoroutineModuleContentGenerator
    private lateinit var temporaryDirectory: File

    @Before
    fun setUp() {
        val fileCreator = FileCreator(FakeFileSystemBridge())
        classUnderTest =
            CoroutineModuleContentGenerator(
                gradleFileCreator = GradleFileCreator(fileCreator),
                catalogUpdater = VersionCatalogUpdater(fileCreator)
            )
        temporaryDirectory = createTempDirectory(prefix = "test").toFile()
    }

    @Test(expected = GenerationException::class)
    fun `Given empty architecture package name when generate then throws exception`() {
        // Given
        val projectRoot = temporaryDirectory
        val architecturePackageName = ""

        // When
        classUnderTest.generate(projectRoot, architecturePackageName)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given invalid architecture package name when generate then throws exception`() {
        // Given
        val projectRoot = temporaryDirectory
        val architecturePackageName = "..."

        // When
        classUnderTest.generate(projectRoot, architecturePackageName)

        // Then throws GenerationException
    }

    @Test
    fun `Given valid architecture package when generate then creates CoroutineContextProvider with exact content`() {
        // Given
        val projectRoot = temporaryDirectory
        val coroutinePackageName = "com.example.superapp.coroutine"

        // When
        classUnderTest.generate(projectRoot, coroutinePackageName)

        // Then
        val coroutineContextProviderFile =
            File(
                projectRoot,
                "coroutine/src/main/java/com/example/superapp/coroutine/CoroutineContextProvider.kt"
            )
        val expectedContent = """package com.example.superapp.coroutine

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
"""
        assertEquals("CoroutineContextProvider.kt should have exact content", expectedContent, coroutineContextProviderFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates coroutine module build gradle file`() {
        // Given
        val projectRoot = temporaryDirectory
        val architecturePackageName = "com.example.architecture"

        // When
        classUnderTest.generate(projectRoot, architecturePackageName)

        // Then
        val buildGradleFile = File(projectRoot, "coroutine/build.gradle.kts")
        assertEquals("build.gradle.kts should exist", true, buildGradleFile.exists())
    }

    @Test
    fun `Given valid architecture package when generate then creates coroutine module build gradle file with java library plugin`() {
        // Given
        val projectRoot = temporaryDirectory
        val architecturePackageName = "com.example.architecture"
        val expectedContent = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
"""

        // When
        classUnderTest.generate(projectRoot, architecturePackageName)

        // Then
        val buildGradleFile = File(projectRoot, "coroutine/build.gradle.kts")
        val buildGradleContent = buildGradleFile.readText()
        assertEquals("build.gradle.kts should have exact content", expectedContent, buildGradleContent)
    }
}
