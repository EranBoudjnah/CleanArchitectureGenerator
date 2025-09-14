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
    private lateinit var classUnderTest: NamespaceResolver

    @MockK(relaxed = false)
    lateinit var projectModel: ProjectModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        classUnderTest = NamespaceResolver()
    }

    @Test
    fun `Given module has namespace when determineBasePackage then returns namespace`() {
        // Given
        val namespace = "com.example.app"
        every { projectModel.selectedModuleRootDir() } returns
            createTemporaryAppModuleDirectory(namespace = namespace)

        // When
        val actualResult = classUnderTest.determineBasePackage(projectModel)

        // Then
        assertEquals(namespace, actualResult)
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given no selected module, other modules with namespace when determineBasePackage then returns the app module namespace`() {
        // Given
        every { projectModel.selectedModuleRootDir() } returns null
        val otherNamespace1 = "com.sample.app.other"
        val otherNamespace2 = "com.sample.app.another"
        val namespace = "com.sample.app"
        every { projectModel.allModuleRootDirs() } returns
            listOf(
                createTemporaryGenericModuleDirectory(namespace = otherNamespace1),
                createTemporaryAppModuleDirectory(namespace = namespace),
                createTemporaryGenericModuleDirectory(namespace = otherNamespace2)
            )

        // When
        val result = classUnderTest.determineBasePackage(projectModel)

        // Then
        assertEquals(namespace, result)
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

    @Test
    fun `Given module uses version catalog alias when determineBasePackage then returns namespace`() {
        // Given
        val namespace = "com.example.catalog"
        every { projectModel.selectedModuleRootDir() } returns
            createTemporaryAppModuleDirectoryWithCatalog(namespace = namespace)

        // When
        val actualResult = classUnderTest.determineBasePackage(projectModel)

        // Then
        assertEquals(namespace, actualResult)
    }

    @Test
    fun `Given module declares app plugin apply false when determineBasePackage then returns null`() {
        // Given
        every { projectModel.selectedModuleRootDir() } returns
            createTemporaryAppModuleDirectoryApplyFalse(namespace = "com.example.appfalse")
        every { projectModel.allModuleRootDirs() } returns emptyList()

        // When
        val actualResult = classUnderTest.determineBasePackage(projectModel)

        // Then
        assertNull(actualResult)
    }

    @Test
    fun `Given module uses version catalog alias with apply false when determineBasePackage then returns null`() {
        // Given
        every { projectModel.selectedModuleRootDir() } returns
            createTemporaryAppModuleDirectoryWithCatalogApplyFalse(namespace = "com.example.catalogfalse")
        every { projectModel.allModuleRootDirs() } returns emptyList()

        // When
        val actualResult = classUnderTest.determineBasePackage(projectModel)

        // Then
        assertNull(actualResult)
    }

    private fun createTemporaryAppModuleDirectory(namespace: String): File =
        createTemporaryModuleDirectory(
            namespace = namespace,
            firstBlock =
                """
                plugins {
                    id("com.android.application")
                }

                """.trimIndent()
        )

    private fun createTemporaryGenericModuleDirectory(namespace: String): File =
        createTemporaryModuleDirectory(namespace = namespace, firstBlock = "")

    private fun createTemporaryModuleDirectory(
        namespace: String,
        firstBlock: String
    ): File =
        createTemporaryDirectory().apply {
            File(this, "build.gradle.kts").writeText(
                """
                $firstBlock
                android {
                    namespace = "$namespace"
                }
                """.trimIndent()
            )
        }

    private fun createTemporaryAppModuleDirectoryWithCatalog(namespace: String): File =
        createTemporaryModuleDirectory(
            namespace = namespace,
            firstBlock =
                """
                plugins {
                    alias(libs.plugins.android.application)
                }

                """.trimIndent()
        ).apply {
            createVersionCatalog(this)
        }

    private fun createTemporaryAppModuleDirectoryApplyFalse(namespace: String): File =
        createTemporaryModuleDirectory(
            namespace = namespace,
            firstBlock =
                """
                plugins {
                    id("com.android.application") apply false
                }

                """.trimIndent()
        )

    private fun createTemporaryAppModuleDirectoryWithCatalogApplyFalse(namespace: String): File {
        createTemporaryModuleDirectory(
            namespace = namespace,
            firstBlock =
                """
                plugins {
                    alias(libs.plugins.android.application) apply false
                }

                """.trimIndent()
        )

        return createTemporaryDirectory().apply {
            createVersionCatalog(this)
        }
    }

    private fun createVersionCatalog(directory: File) {
        val gradleDirectory = File(directory, "gradle")
        gradleDirectory.mkdirs()
        File(gradleDirectory, "libs.versions.toml")
            .writeText(
                """
                [versions]
                agp = "8.5.2"

                [plugins]
                android-application = { id = "com.android.application", version.ref = "agp" }
                """.trimIndent()
            )
    }

    private fun createTemporaryDirectory() = createTempDirectory(prefix = "moduleDir").toFile()
}
