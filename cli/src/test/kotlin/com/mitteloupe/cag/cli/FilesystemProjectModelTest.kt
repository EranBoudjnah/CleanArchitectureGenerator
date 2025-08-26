package com.mitteloupe.cag.cli

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class FilesystemProjectModelTest {
    private lateinit var classUnderTest: FilesystemProjectModel

    private lateinit var projectRoot: File

    @Before
    fun setUp() {
        projectRoot = createTempDirectory(prefix = "projectRoot").toFile()
        classUnderTest = FilesystemProjectModel(projectRoot)
    }

    @Test
    fun `Given single module project and no selected module when selectedModuleRootDir then returns that module`() {
        // Given
        val singleModule = File(projectRoot, "app").apply { mkdirs() }
        createBuildGradleKotlinFile(singleModule, "com.example.app")
        val expectedCanonicalFile = singleModule.canonicalFile

        // When
        val result = classUnderTest.selectedModuleRootDir()

        // Then
        assertEquals(expectedCanonicalFile, result?.canonicalFile)
    }

    @Test
    fun `Given multi-module project and no selected module when selectedModuleRootDir then returns null`() {
        // Given
        val moduleA = File(projectRoot, "moduleA").apply { mkdirs() }
        createBuildGradleKotlinFile(moduleA, "com.example.moduleA")
        val moduleB = File(projectRoot, "moduleB").apply { mkdirs() }
        createBuildGradleKotlinFile(moduleB, "com.example.moduleB")

        // When
        val result = classUnderTest.selectedModuleRootDir()

        // Then
        assertNull(result)
    }

    @Test
    fun `Given explicit selected module when selectedModuleRootDir then returns that directory`() {
        // Given
        val moduleDir = File(projectRoot, "feature").apply { mkdirs() }
        createBuildGradleKotlinFile(moduleDir, "com.example.feature")
        val expectedCanonicalFile = moduleDir.canonicalFile

        // When
        val result = classUnderTest.selectedModuleRootDir()

        // Then
        assertEquals(expectedCanonicalFile, result?.canonicalFile)
    }

    @Test
    fun `Given nested modules when allModuleRootDirs then returns all gradle module directories`() {
        // Given
        val moduleRoot = File(projectRoot, "lib").apply { mkdirs() }
        createBuildGradleKotlinFile(moduleRoot, "com.example.lib")
        val nestedModule = File(moduleRoot, "deep").apply { mkdirs() }
        createBuildGradleKotlinFile(nestedModule, "com.example.lib.deep")
        val expectedCanonicalFiles = setOf(moduleRoot.canonicalFile)

        // When
        val result = classUnderTest.allModuleRootDirs().map { it.canonicalFile }.toSet()

        // Then
        assertEquals(expectedCanonicalFiles, result)
    }

    @Test
    fun `Given directories without build folders are traversed but build dirs are skipped`() {
        // Given
        val moduleRoot = File(projectRoot, "lib").apply { mkdirs() }
        createBuildGradleKotlinFile(moduleRoot, "com.example.lib")
        val buildDir = File(projectRoot, "build").apply { mkdirs() }
        val nestedInsideBuild = File(buildDir, "shouldNotScan").apply { mkdirs() }
        createBuildGradleKotlinFile(nestedInsideBuild, "com.example.ignored")
        val nestedModule = File(moduleRoot, "sub").apply { mkdirs() }
        createBuildGradleKotlinFile(nestedModule, "com.example.sub")
        val expectedCanonicalFiles = setOf(moduleRoot.canonicalFile)

        // When
        val result = classUnderTest.allModuleRootDirs().map { it.canonicalFile }.toSet()

        // Then
        assertEquals(expectedCanonicalFiles, result)
    }

    private fun createBuildGradleKotlinFile(
        path: File,
        namespace: String
    ) {
        File(path, "build.gradle.kts").writeText(
            """
            android {
                namespace = "$namespace"
            }
            """.trimIndent()
        )
    }
}
