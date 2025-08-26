package com.mitteloupe.cag.core

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class BasePackageResolverTest {
    private lateinit var classUnderTest: BasePackageResolver

    @MockK(relaxed = false)
    lateinit var projectModel: ProjectModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        classUnderTest = BasePackageResolver()
    }

    @Test
    fun `Given module has namespace when determineBasePackage then returns namespace with dot`() {
        // Given
        val givenNamespace = "com.example.app"
        every { projectModel.selectedModuleRootDir() } returns createTemporaryModuleDirectory(withNamespace = givenNamespace)

        val expectedPackageNamePrefix = "$givenNamespace."

        // When
        val actualResult = classUnderTest.determineBasePackage(projectModel)

        // Then
        assertEquals(expectedPackageNamePrefix, actualResult)
    }

    @Test
    fun `Given no selected module, another module has namespace when determineBasePackage then returns that namespace with dot`() {
        // Given
        every { projectModel.selectedModuleRootDir() } returns null
        val givenNamespace = "com.sample.app"
        every { projectModel.allModuleRootDirs() } returns
            listOf(createTemporaryModuleDirectory(withNamespace = givenNamespace))
        val expectedPackageNamePrefix = "$givenNamespace."

        // When
        val result = classUnderTest.determineBasePackage(projectModel)

        // Then
        assertEquals(expectedPackageNamePrefix, result)
    }

    @Test
    fun `Given no namespace when determineBasePackage then returns null`() {
        // Given
        every { projectModel.selectedModuleRootDir() } returns null
        every { projectModel.allModuleRootDirs() } returns emptyList()

        // When
        val result = classUnderTest.determineBasePackage(projectModel)

        // Then
        assertNull(result)
    }

    private fun createTemporaryModuleDirectory(withNamespace: String): File {
        val directory = createTempDirectory(prefix = "moduleDir").toFile()
        File(directory, "build.gradle.kts").writeText(
            """
            android {
                namespace = "$withNamespace"
            }
            """.trimIndent()
        )
        return directory
    }
}
