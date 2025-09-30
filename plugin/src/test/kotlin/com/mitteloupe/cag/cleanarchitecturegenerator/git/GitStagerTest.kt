package com.mitteloupe.cag.cleanarchitecturegenerator.git

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class GitStagerTest {
    private lateinit var classUnderTest: GitStager

    private lateinit var projectRoot: File

    @MockK
    private lateinit var executor: ProcessExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        projectRoot = createTempDirectory(prefix = "projRoot_").toFile()
        classUnderTest = GitStager(executor)
    }

    @After
    fun cleanUp() {
        projectRoot.deleteRecursively()
    }

    @Test
    fun `Given files under project root When flush Then runs git add with relative paths`() {
        // Given
        val relativePath = "src"
        val relativeDirectory = File(projectRoot, relativePath).apply { mkdirs() }
        val filename1 = "A.kt"
        val file1 = File(relativeDirectory, filename1).apply { writeText("a") }
        val filename2 = "B.txt"
        val file2 = File(projectRoot, filename2).apply { writeText("b") }
        val givenFiles = listOf(file1, file2)
        val expectedArguments = listOf("git", "add", "--", "$relativePath/$filename1", filename2)
        every { executor.run(projectRoot, expectedArguments) } returns Unit

        // When
        classUnderTest.stageAll(projectRoot, givenFiles)

        // Then
        verify { executor.run(projectRoot, expectedArguments) }
    }

    @Test
    fun `Given empty queue When flush Then does not run git`() {
        // When
        classUnderTest.stageAll(projectRoot, emptyList())

        // Then
        verify(exactly = 0) { executor.run(any(), any()) }
    }

    @Test
    fun `Given file outside project root When flush Then uses absolute path`() {
        // Given
        val externalDirectory = createTempDirectory(prefix = "external_").toFile()
        val filename = "C.kt"
        val externalFile = File(externalDirectory, filename).apply { writeText("c") }
        val givenFiles = listOf(externalFile)
        val expectedArguments = listOf("git", "add", "--", "../${externalDirectory.name}/$filename")
        every { executor.run(projectRoot, expectedArguments) } returns Unit

        try {
            // When
            classUnderTest.stageAll(projectRoot, givenFiles)

            // Then
            verify { executor.run(projectRoot, expectedArguments) }
        } finally {
            externalDirectory.deleteRecursively()
        }
    }
}
