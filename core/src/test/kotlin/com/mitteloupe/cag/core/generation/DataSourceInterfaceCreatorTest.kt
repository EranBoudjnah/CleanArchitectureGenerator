package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        val result =
            classUnderTest.writeDataSourceInterface(
                destinationRootDirectory,
                projectNamespace,
                dataSourceName
            )

        // Then
        assertNull(result)

        val datasourceRoot = File(destinationRootDirectory, "datasource")
        assertTrue(datasourceRoot.exists())

        val sourceRoot = File(datasourceRoot, "source/src/main/java")
        assertTrue(sourceRoot.exists())

        val targetDirectory = File(sourceRoot, "com/example/datasource/test/datasource")
        assertTrue(targetDirectory.exists())

        val interfaceFile = File(targetDirectory, "TestDataSource.kt")
        assertTrue(interfaceFile.exists())
        assertTrue(interfaceFile.readText().contains("interface TestDataSource"))
    }

    @Test
    fun `Given data source name ending with DataSource when writeDataSourceInterface then removes suffix for directory`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // When
        val result =
            classUnderTest.writeDataSourceInterface(
                destinationRootDirectory,
                projectNamespace,
                dataSourceName
            )

        // Then
        assertNull(result)

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
        val result =
            classUnderTest.writeDataSourceInterface(
                destinationRootDirectory,
                projectNamespace,
                dataSourceName
            )

        // Then
        assertNull(result)

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
        val result =
            classUnderTest.writeDataSourceInterface(
                destinationRootDirectory,
                projectNamespace,
                dataSourceName
            )

        // Then
        assertNull(result)

        assertEquals("existing content", existingFile.readText())
    }

    @Test
    fun `Given project namespace with trailing dot when writeDataSourceInterface then trims dot`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example."
        val dataSourceName = "TestDataSource"

        // When
        val result =
            classUnderTest.writeDataSourceInterface(
                destinationRootDirectory,
                projectNamespace,
                dataSourceName
            )

        // Then
        assertNull(result)

        val targetDirectory = File(tempDirectory, "datasource/source/src/main/java/com/example/datasource/test/datasource")
        assertTrue(targetDirectory.exists())

        val interfaceFile = File(targetDirectory, "TestDataSource.kt")
        assertTrue(interfaceFile.readText().contains("package com.example.datasource.test.datasource"))
    }

    @Test
    fun `Given directory creation fails when writeDataSourceInterface then returns error`() {
        // Given
        val destinationRootDirectory = File(tempDirectory, "readonly")
        destinationRootDirectory.mkdirs()
        destinationRootDirectory.setReadOnly()

        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        // When
        val result =
            classUnderTest.writeDataSourceInterface(
                destinationRootDirectory,
                projectNamespace,
                dataSourceName
            )

        // Then
        assertTrue(result!!.startsWith(ERROR_PREFIX))
        assertTrue(result.contains("Failed to create directory"))
    }

    @Test
    fun `Given file creation fails when writeDataSourceInterface then returns error`() {
        // Given
        val destinationRootDirectory = tempDirectory
        val projectNamespace = "com.example"
        val dataSourceName = "TestDataSource"

        val targetDirectory = File(tempDirectory, "datasource/source/src/main/java/com/example/datasource/test/datasource")
        targetDirectory.mkdirs()
        targetDirectory.setReadOnly()

        // When
        val result =
            classUnderTest.writeDataSourceInterface(
                destinationRootDirectory,
                projectNamespace,
                dataSourceName
            )

        // Then
        assertTrue(result!!.startsWith(ERROR_PREFIX))
        assertTrue(result.contains("Failed to create file"))
    }
}
