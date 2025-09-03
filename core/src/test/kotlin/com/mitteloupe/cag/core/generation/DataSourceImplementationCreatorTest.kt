package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class DataSourceImplementationCreatorTest {
    private lateinit var classUnderTest: DataSourceImplementationCreator
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        classUnderTest = DataSourceImplementationCreator()
        tempDirectory = createTempDirectory(prefix = "test").toFile()
    }

    @Test
    fun `Given valid parameters when writeDataSourceImplementation then creates implementation file`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // When
        classUnderTest.writeDataSourceImplementation(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        val datasourceRoot = File(destinationRootDirectory, "datasource")
        assertTrue(datasourceRoot.exists())

        val implementationRoot = File(datasourceRoot, "implementation/src/main/java")
        assertTrue(implementationRoot.exists())

        val targetDirectory = File(implementationRoot, "com/example/datasource/test/datasource")
        assertTrue(targetDirectory.exists())

        val implementationFile = File(targetDirectory, "TestDataSourceImpl.kt")
        assertTrue(implementationFile.exists())
        assertTrue(implementationFile.readText().contains("class TestDataSourceImpl"))
    }

    @Test
    fun `Given data source name ending with DataSource when writeDataSourceImplementation then removes suffix for directory`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // When
        classUnderTest.writeDataSourceImplementation(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        val targetDirectory = File(tempDirectory, "datasource/implementation/src/main/java/com/example/datasource/test/datasource")
        assertTrue(targetDirectory.exists())
    }

    @Test
    fun `Given data source name not ending with DataSource when writeDataSourceImplementation then uses full name for directory`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestData"

        // When
        classUnderTest.writeDataSourceImplementation(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        val targetDirectory = File(tempDirectory, "datasource/implementation/src/main/java/com/example/datasource/testdata/datasource")
        assertTrue(targetDirectory.exists())
    }

    @Test
    fun `Given existing implementation file when writeDataSourceImplementation then does not overwrite`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        val targetDirectory = File(tempDirectory, "datasource/implementation/src/main/java/com/example/datasource/test/datasource")
        targetDirectory.mkdirs()
        val existingFile = File(targetDirectory, "TestDataSourceImpl.kt")
        existingFile.writeText("existing content")

        // When
        classUnderTest.writeDataSourceImplementation(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        assertEquals("existing content", existingFile.readText())
    }

    @Test
    fun `Given project namespace with trailing dot when writeDataSourceImplementation then trims dot`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example."
        val dataSourceName = "TestDataSource"

        // When
        classUnderTest.writeDataSourceImplementation(destinationRootDirectory, projectNamespace, dataSourceName)

        // Then
        val targetDirectory = File(tempDirectory, "datasource/implementation/src/main/java/com/example/datasource/test/datasource")
        assertTrue(targetDirectory.exists())

        val implementationFile = File(targetDirectory, "TestDataSourceImpl.kt")
        assertTrue(implementationFile.readText().contains("package com.example.datasource.test.datasource"))
    }

    @Test(expected = GenerationException::class)
    fun `Given directory creation fails when writeDataSourceImplementation then throws exception`() {
        // Given
        val destinationRootDirectory = File(tempDirectory, "readonly")
        destinationRootDirectory.mkdirs()
        destinationRootDirectory.setReadOnly()

        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // When
        classUnderTest.writeDataSourceImplementation(
            destinationRootDirectory,
            projectNamespace,
            dataSourceName
        )

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given file creation fails when writeDataSourceImplementation then throws exception`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        val targetDirectory = File(tempDirectory, "datasource/implementation/src/main/java/com/example/datasource/test/datasource")
        targetDirectory.mkdirs()
        targetDirectory.setReadOnly()

        // When
        classUnderTest.writeDataSourceImplementation(
            destinationRootDirectory,
            projectNamespace,
            dataSourceName
        )

        // Then throws GenerationException
    }
}
