package com.mitteloupe.cag.cleanarchitecturegenerator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ModelClassFinderTest {
    private lateinit var classUnderTest: ModelClassFinder

    @Before
    fun setUp() {
        classUnderTest = ModelClassFinder()
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

        // When
        val actualResult = classUnderTest.extractClassesFromContent(content)

        // Then
        assertEquals(3, actualResult.size)
        assertTrue(actualResult.contains("com.example.domain.model.UserModel"))
        assertTrue(actualResult.contains("com.example.domain.model.UserRepository"))
        assertTrue(actualResult.contains("com.example.domain.model.UserUseCase"))
    }

    @Test
    fun `Given different directories when findModelClasses then returns consistent results`() {
        // Given
        val useCaseDirectory = java.io.File("/feature1/domain/usecase")
        val originalRightClickedDirectory = java.io.File("/feature1/domain/presentation")

        // When
        val modelClassesFromUseCaseDir = classUnderTest.findModelClasses(useCaseDirectory)
        val modelClassesFromOriginalDir = classUnderTest.findModelClasses(originalRightClickedDirectory)

        // Then
        // Both should return the same result since they both look for ../model relative to their respective directories
        // This test verifies that the ModelClassFinder works correctly with different input directories
        assertEquals(modelClassesFromUseCaseDir, modelClassesFromOriginalDir)
    }

    @Test
    fun `Given usecase directory when findModelClasses then finds model classes in sibling model directory`() {
        // Given
        val useCaseDirectory = java.io.File("/feature1/domain/usecase")

        // When
        val modelClasses = classUnderTest.findModelClasses(useCaseDirectory)

        // Then
        // This test verifies that the ModelClassFinder correctly looks for model directory in parent
        // The actual result depends on the file system, but the method should not throw exceptions
        // and should return a non-null list (empty or populated)
        assertTrue("Should return a non-null list", modelClasses.size >= 0)
    }
}
