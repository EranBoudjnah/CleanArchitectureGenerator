package com.mitteloupe.cag.cleanarchitecturegenerator

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Assert.assertEquals
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
    fun `Given complex model content when extractClassesFromContent then handles all class types`() {
        // Given
        val content =
            """
            package com.example.domain.model
            
            class UserModel
            interface UserRepository
            class UserUseCase
            interface UserValidator
            """.trimIndent()

        // When
        val actualResult = classUnderTest.extractClassesFromContent(content)

        // Then
        assertEquals(4, actualResult.size)
        val expected =
            arrayOf(
                "com.example.domain.model.UserModel",
                "com.example.domain.model.UserRepository",
                "com.example.domain.model.UserUseCase",
                "com.example.domain.model.UserValidator"
            )
        assertThat(actualResult, contains(*expected))
    }

    @Test
    fun `Given nested package structure when extractClassesFromContent then handles correctly`() {
        // Given
        val content =
            """
            package com.example.feature.user.domain.model
            
            class UserModel
            interface UserRepository
            """.trimIndent()
        val expected =
            arrayOf(
                "com.example.feature.user.domain.model.UserModel",
                "com.example.feature.user.domain.model.UserRepository"
            )

        // When
        val actualResult = classUnderTest.extractClassesFromContent(content)

        // Then
        assertEquals(2, actualResult.size)
        assertThat(actualResult, contains(*expected))
    }

    @Test
    fun `Given mixed content with comments when extractClassesFromContent then extracts only classes`() {
        // Given
        val content =
            """
            package com.example.domain.model
            
            // This is a comment
            class UserModel
            /* Another comment */
            interface UserRepository
            // Empty line
            
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
}
