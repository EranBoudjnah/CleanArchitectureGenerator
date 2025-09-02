package com.mitteloupe.cag.cleanarchitecturegenerator.validation

import com.mitteloupe.cag.cleanarchitecturegenerator.test.filesystem.FakeFileSystemWrapper
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SymbolValidatorFileSystemTest {
    private lateinit var classUnderTest: SymbolValidator
    private lateinit var fakeFileSystem: FakeFileSystemWrapper
    private lateinit var temporaryDirectory: File

    @Before
    fun setUp() {
        temporaryDirectory = Files.createTempDirectory("SymbolValidatorFileSystemTest").toFile()
        fakeFileSystem = FakeFileSystemWrapper(temporaryDirectory)
        classUnderTest = SymbolValidator(fakeFileSystem)
    }

    @After
    fun tearDown() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun `Given primitive type when isValidSymbolInContext then returns true`() {
        // Given
        val type = "String"

        // When
        val result = classUnderTest.isValidSymbolInContext(type, temporaryDirectory)

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given collection type when isValidSymbolInContext then returns true`() {
        // Given
        val type = "List<String>"

        // When
        val result = classUnderTest.isValidSymbolInContext(type, temporaryDirectory)

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given custom type that exists in module when isValidSymbolInContext then returns true`() {
        // Given
        fakeFileSystem.createFile("build.gradle.kts", "")
        fakeFileSystem.createDirectory("src/main/kotlin")
        fakeFileSystem.createFile(
            "src/main/kotlin/UserModel.kt",
            "class UserModel"
        )
        val type = "UserModel"

        // When
        val result = classUnderTest.isValidSymbolInContext(type, temporaryDirectory)

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given custom type that does not exist in module when isValidSymbolInContext then returns false`() {
        // Given
        fakeFileSystem.createFile("build.gradle.kts", "")
        fakeFileSystem.createDirectory("src/main/kotlin")
        fakeFileSystem.createFile(
            "src/main/kotlin/UserModel.kt",
            "class UserModel"
        )
        val type = "NonExistentType"

        // When
        val result = classUnderTest.isValidSymbolInContext(type, temporaryDirectory)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given module without source directory when isValidSymbolInContext then returns false`() {
        // Given
        fakeFileSystem.createFile("build.gradle.kts", "")
        // No source directory created
        val type = "UserModel"

        // When
        val result = classUnderTest.isValidSymbolInContext(type, temporaryDirectory)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given module with build gradle kts when isValidSymbolInContext then finds module root`() {
        // Given
        fakeFileSystem.createFile("build.gradle.kts", "")
        fakeFileSystem.createDirectory("src/main/kotlin")
        fakeFileSystem.createFile(
            "src/main/kotlin/UserModel.kt",
            "class UserModel"
        )
        val type = "UserModel"

        // When
        val result = classUnderTest.isValidSymbolInContext(type, temporaryDirectory)

        // Then
        assertTrue(result)
    }
}
