package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class BasePackageResolverTest {
    private lateinit var classUnderTest: BasePackageResolver

    @MockK(relaxed = true)
    lateinit var event: AnActionEvent

    @MockK(relaxed = true)
    lateinit var project: Project

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        classUnderTest = BasePackageResolver()
        every { event.project } returns project

        mockkStatic(ModuleManager::class)
        mockkStatic(ModuleUtilCore::class)
        mockkStatic(ModuleRootManager::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Given module has namespace when determineBasePackage then returns namespace with dot`() {
        // Given
        val module = mockk<Module>()
        val virtualFile = mockk<VirtualFile>()
        every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
        every { ModuleUtilCore.findModuleForFile(virtualFile, project) } returns module

        val moduleRootManager = mockk<ModuleRootManager>()
        every { ModuleRootManager.getInstance(module) } returns moduleRootManager
        val contentRoot = mockk<VirtualFile>()
        every { moduleRootManager.contentRoots } returns arrayOf(contentRoot)
        val givenNamespace = "com.example.app"
        every { contentRoot.path } returns createTemporaryModuleDirectory(withNamespace = givenNamespace).path
        val expectedPackageNamePrefix = "$givenNamespace."

        // When
        val actualResult = classUnderTest.determineBasePackage(event)

        // Then
        assertEquals(expectedPackageNamePrefix, actualResult)
    }

    @Test
    fun `Given no selected module, another module has namespace when determineBasePackage then returns that namespace with dot`() {
        // Given
        every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns null

        val moduleManager = mockk<ModuleManager>()
        every { ModuleManager.getInstance(project) } returns moduleManager
        val moduleWithNs = mockk<Module>(relaxed = true)
        every { moduleManager.modules } returns arrayOf(moduleWithNs)

        val moduleRootManager = mockk<ModuleRootManager>()
        every { ModuleRootManager.getInstance(moduleWithNs) } returns moduleRootManager
        val contentRoot = mockk<VirtualFile>()
        every { moduleRootManager.contentRoots } returns arrayOf(contentRoot)
        val givenNamespace = "com.sample.app"
        every { contentRoot.path } returns createTemporaryModuleDirectory(withNamespace = givenNamespace).path
        val expectedPackageNamePrefix = "$givenNamespace."

        // When
        val result = classUnderTest.determineBasePackage(event)

        // Then
        assertEquals(expectedPackageNamePrefix, result)
    }

    @Test
    fun `Given no namespace when determineBasePackage then returns null`() {
        // Given
        every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns null
        every { ModuleManager.getInstance(project).modules } returns emptyArray()

        // When
        val result = classUnderTest.determineBasePackage(event)

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
