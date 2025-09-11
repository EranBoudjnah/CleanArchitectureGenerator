package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class DataSourceInterfaceCreatorTest {
    private lateinit var classUnderTest: DataSourceInterfaceCreator
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        classUnderTest = DataSourceInterfaceCreator()
        tempDirectory = createTempDirectory(prefix = "test").toFile()
    }

    @Test
    fun `Given valid parameters when writeDataSourceInterface then creates interface file`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // When
        classUnderTest.writeDataSourceInterface(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        assertTrue(datasourceRoot.exists())

        val sourceRoot = File(datasourceRoot, "source/src/main/java")
        assertTrue(sourceRoot.exists())

        val targetDirectory = File(sourceRoot, "com/example/datasource/test/datasource")
        assertTrue(targetDirectory.exists())

        val interfaceFile = File(targetDirectory, "TestDataSource.kt")
        assertTrue(interfaceFile.exists())
        val expectedContent = """package com.example.datasource.test.datasource

interface TestDataSource {
}
"""
        assertEquals("Interface file should have exact content", expectedContent, interfaceFile.readText())
    }

    @Test
    fun `Given data source name ending with DataSource when writeDataSourceInterface then removes suffix for directory`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // When
        classUnderTest.writeDataSourceInterface(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        val targetDirectory = File(tempDirectory, "datasource/source/src/main/java/com/example/datasource/test/datasource")
        assertTrue(targetDirectory.exists())
    }

    @Test
    fun `Given data source name not ending with DataSource when writeDataSourceInterface then uses full name for directory`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestData"

        // When
        classUnderTest.writeDataSourceInterface(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        val targetDirectory = File(tempDirectory, "datasource/source/src/main/java/com/example/datasource/testdata/datasource")
        assertTrue(targetDirectory.exists())
    }

    @Test
    fun `Given existing interface file when writeDataSourceInterface then does not overwrite`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // Create existing file
        val targetDirectory = File(tempDirectory, "datasource/source/src/main/java/com/example/datasource/test/datasource")
        targetDirectory.mkdirs()
        val existingFile = File(targetDirectory, "TestDataSource.kt")
        existingFile.writeText("existing content")

        // When
        classUnderTest.writeDataSourceInterface(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        assertEquals("existing content", existingFile.readText())
    }

    @Test(expected = GenerationException::class)
    fun `Given directory creation fails when writeDataSourceInterface then throws exception`() {
        // Given
        val destinationRootDirectory = File(tempDirectory, "readonly")
        destinationRootDirectory.mkdirs()
        destinationRootDirectory.setReadOnly()

        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // When
        classUnderTest.writeDataSourceInterface(
            destinationRootDirectory,
            projectNamespace,
            dataSourceName
        )

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given file creation fails when writeDataSourceInterface then throws exception`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        val targetDirectory = File(tempDirectory, "datasource/source/src/main/java/com/example/datasource/test/datasource")
        targetDirectory.mkdirs()
        targetDirectory.setReadOnly()

        // When
        classUnderTest.writeDataSourceInterface(
            destinationRootDirectory,
            projectNamespace,
            dataSourceName
        )

        // Then throws GenerationException
    }
}
