package com.mitteloupe.cag.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class DirectoryFinderTest {
    private lateinit var classUnderTest: DirectoryFinder
    private lateinit var temporaryDirectory: File

    @Before
    fun setUp() {
        temporaryDirectory = createTempDirectory(prefix = "DirectoryFinderTest").toFile()
        classUnderTest = DirectoryFinder()
    }

    @Test
    fun `Given directory that matches predicate when findDirectory then returns that directory`() {
        // Given
        val targetDirectory = File(temporaryDirectory, "target")
        targetDirectory.mkdirs()
        val predicate: (File) -> Boolean = { it.name == "target" }

        // When
        val result = classUnderTest.findDirectory(targetDirectory, predicate)

        // Then
        assertEquals(targetDirectory, result)
    }

    @Test
    fun `Given directory that does not match predicate when findDirectory then searches parent directories`() {
        // Given
        val parentDirectory = File(temporaryDirectory, "parent")
        val childDirectory = File(parentDirectory, "child")
        childDirectory.mkdirs()
        val predicate: (File) -> Boolean = { it.name == "parent" }

        // When
        val result = classUnderTest.findDirectory(childDirectory, predicate)

        // Then
        assertEquals(parentDirectory, result)
    }

    @Test
    fun `Given directory hierarchy when findDirectory then finds first matching parent`() {
        // Given
        val rootDirectory = File(temporaryDirectory, "root")
        val middleDirectory = File(rootDirectory, "middle")
        val leafDirectory = File(middleDirectory, "leaf")
        leafDirectory.mkdirs()
        val predicate: (File) -> Boolean = { it.name == "middle" }

        // When
        val result = classUnderTest.findDirectory(leafDirectory, predicate)

        // Then
        assertEquals(middleDirectory, result)
    }

    @Test
    fun `Given directory that reaches root without match when findDirectory then returns null`() {
        // Given
        val childDirectory = File(temporaryDirectory, "child")
        childDirectory.mkdirs()
        val predicate: (File) -> Boolean = { it.name == "nonexistent" }

        // When
        val result = classUnderTest.findDirectory(childDirectory, predicate)

        // Then
        assertNull(result)
    }

    @Test
    fun `Given root directory when findDirectory then returns null if no match`() {
        // Given
        val predicate: (File) -> Boolean = { it.name == "nonexistent" }

        // When
        val result = classUnderTest.findDirectory(temporaryDirectory, predicate)

        // Then
        assertNull(result)
    }

    @Test
    fun `Given root directory that matches predicate when findDirectory then returns root`() {
        // Given
        val predicate: (File) -> Boolean = { it.name == temporaryDirectory.name }

        // When
        val result = classUnderTest.findDirectory(temporaryDirectory, predicate)

        // Then
        assertEquals(temporaryDirectory, result)
    }
}
