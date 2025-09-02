package com.mitteloupe.cag.cleanarchitecturegenerator

import com.mitteloupe.cag.cleanarchitecturegenerator.test.filesystem.FakeFileSystemWrapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ModelClassFinderTest {
    private lateinit var classUnderTest: ModelClassFinder
    private lateinit var fakeFileSystem: FakeFileSystemWrapper
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        tempDirectory = Files.createTempDirectory("ModelClassFinderTest").toFile()
        fakeFileSystem = FakeFileSystemWrapper(tempDirectory)
        classUnderTest = ModelClassFinder(fakeFileSystem)
    }

    @After
    fun tearDown() {
        tempDirectory.deleteRecursively()
    }

    @Test
    fun `Given class with package declaration when extractClassesFromContent then returns fully qualified name`() {
        // Given
        val content =
            """
            package com.example.domain.model
            
            class UserModel
            """.trimIndent()

        // When
        val actualResult = classUnderTest.extractClassesFromContent(content)

        // Then
        assertEquals(1, actualResult.size)
        assertEquals("com.example.domain.model.UserModel", actualResult[0])
    }

    @Test
    fun `Given class without package declaration when extractClassesFromContent then returns simple name`() {
        // Given
        val content =
            """
            class UserModel
            """.trimIndent()

        // When
        val actualResult = classUnderTest.extractClassesFromContent(content)

        // Then
        assertEquals(1, actualResult.size)
        assertEquals("UserModel", actualResult[0])
    }

    @Test
    fun `Given multiple classes in same package when extractClassesFromContent then returns all fully qualified names`() {
        // Given
        val content =
            """
            package com.example.domain.model
            
            class UserModel
            interface UserRepository
            class UserUseCase
            """.trimIndent()
        val expected =
            arrayOf(
                "com.example.domain.model.UserModel",
                "com.example.domain.model.UserRepository",
                "com.example.domain.model.UserUseCase"
            )

        // When
        val actualResult = classUnderTest.extractClassesFromContent(content)

        // Then
        assertEquals(3, actualResult.size)
        assertThat(actualResult, contains(*expected))
    }

    @Test
    fun `Given different directories when findModelClasses then returns consistent results`() {
        // Given
        fakeFileSystem.createDirectory("feature1/domain/model")
        fakeFileSystem.createFile("feature1/domain/model/UserModel.kt", "class UserModel")

        val useCaseDirectory = fakeFileSystem.createFakeFile("feature1/domain/usecase")
        val originalRightClickedDirectory = fakeFileSystem.createFakeFile("feature1/domain/presentation")

        // When
        val modelClassesFromUseCaseDir = classUnderTest.findModelClasses(useCaseDirectory)
        val modelClassesFromOriginalDir = classUnderTest.findModelClasses(originalRightClickedDirectory)

        // Then
        assertEquals(modelClassesFromUseCaseDir, modelClassesFromOriginalDir)
    }

    @Test
    fun `Given usecase directory when findModelClasses then finds model classes in sibling model directory`() {
        // Given
        fakeFileSystem.createDirectory("feature1/domain/model")
        fakeFileSystem.createFile(
            "feature1/domain/model/UserModel.kt",
            """
            package com.example.domain.model

            class UserModel
            """.trimIndent()
        )
        val useCaseDirectory = fakeFileSystem.createFakeFile("feature1/domain/usecase")
        val expected = arrayOf("com.example.domain.model.UserModel")

        // When
        val actualResult = classUnderTest.findModelClasses(useCaseDirectory)

        // Then
        assertThat(actualResult, `is`(not(empty())))
        assertThat(actualResult, contains(*expected))
    }
}
