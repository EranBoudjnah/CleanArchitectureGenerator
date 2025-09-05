package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.content.buildGradleWrapperPropertiesFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class GradleWrapperCreatorTest {
    private lateinit var classUnderTest: GradleWrapperCreator

    @Before
    fun setUp() {
        classUnderTest = GradleWrapperCreator()
    }

    @Test
    fun `Given project root when writeGradleWrapperFiles then creates gradle-wrapper-properties file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()

        // When
        classUnderTest.writeGradleWrapperFiles(projectRoot)

        // Then
        val gradleWrapperDirectory = File(projectRoot, "gradle/wrapper")
        val gradleWrapperPropertiesFile = File(gradleWrapperDirectory, "gradle-wrapper.properties")
        val expectedPropertiesContent = buildGradleWrapperPropertiesFile()
        assertEquals(
            "Gradle wrapper properties should have correct content",
            expectedPropertiesContent,
            gradleWrapperPropertiesFile.readText()
        )
    }

    @Test
    fun `Given project root when writeGradleWrapperFiles then creates gradle-wrapper-jar file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()

        // When
        classUnderTest.writeGradleWrapperFiles(projectRoot)

        // Then
        val gradleWrapperDirectory = File(projectRoot, "gradle/wrapper")
        val gradleWrapperJarFile = File(gradleWrapperDirectory, "gradle-wrapper.jar")
        assertTrue("Gradle wrapper jar file should not be empty", gradleWrapperJarFile.length() > 0)
    }

    @Test
    fun `Given project root when writeGradleWrapperFiles then creates gradlew file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()

        // When
        classUnderTest.writeGradleWrapperFiles(projectRoot)

        // Then
        val gradlewFile = File(projectRoot, "gradlew")
        assertTrue("Gradlew file should be executable", gradlewFile.canExecute())
        val expectedGradlewContent = getResourceAsString("gradlew")
        assertEquals(
            "Gradlew should have correct content",
            expectedGradlewContent,
            gradlewFile.readText()
        )
    }

    @Test
    fun `Given project root when writeGradleWrapperFiles then creates gradlew-bat file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()

        // When
        classUnderTest.writeGradleWrapperFiles(projectRoot)

        // Then
        val gradlewBatFile = File(projectRoot, "gradlew.bat")
        val expectedGradlewBatContent = getResourceAsString("gradlew.bat")
        assertEquals(
            "Gradlew bat should have correct content",
            expectedGradlewBatContent,
            gradlewBatFile.readText()
        )
    }

    @Test
    fun `Given existing gradle-wrapper-properties when writeGradleWrapperFiles then does not overwrite existing file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()
        val gradleWrapperDirectory = File(projectRoot, "gradle/wrapper")
        gradleWrapperDirectory.mkdirs()

        val gradleWrapperPropertiesFile = File(gradleWrapperDirectory, "gradle-wrapper.properties")
        val initialContent = "initial content"
        gradleWrapperPropertiesFile.writeText(initialContent)

        // When
        classUnderTest.writeGradleWrapperFiles(projectRoot)

        // Then
        assertEquals("Existing gradle wrapper properties should not be overwritten", initialContent, gradleWrapperPropertiesFile.readText())
    }

    @Test
    fun `Given existing gradlew when writeGradleWrapperFiles then does not overwrite existing file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()

        val gradlewFile = File(projectRoot, "gradlew")
        val initialGradlewContent = "initial gradlew content"
        gradlewFile.writeText(initialGradlewContent)

        // When
        classUnderTest.writeGradleWrapperFiles(projectRoot)

        // Then
        assertEquals("Existing gradlew should not be overwritten", initialGradlewContent, gradlewFile.readText())
    }

    @Test
    fun `Given existing gradlew-bat when writeGradleWrapperFiles then does not overwrite existing file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()

        val gradlewBatFile = File(projectRoot, "gradlew.bat")
        val initialGradlewBatContent = "initial gradlew.bat content"
        gradlewBatFile.writeText(initialGradlewBatContent)

        // When
        classUnderTest.writeGradleWrapperFiles(projectRoot)

        // Then
        assertEquals("Existing gradlew.bat should not be overwritten", initialGradlewBatContent, gradlewBatFile.readText())
    }

    @Test
    fun `Given existing gradle-wrapper-jar when writeGradleWrapperFiles then does not overwrite existing file`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()
        val gradleWrapperDirectory = File(projectRoot, "gradle/wrapper")
        gradleWrapperDirectory.mkdirs()

        val gradleWrapperJarFile = File(gradleWrapperDirectory, "gradle-wrapper.jar")
        val initialJarContent = "initial jar content"
        gradleWrapperJarFile.writeText(initialJarContent)

        // When
        classUnderTest.writeGradleWrapperFiles(projectRoot)

        // Then
        assertEquals("Existing gradle wrapper jar should not be overwritten", initialJarContent, gradleWrapperJarFile.readText())
    }

    private fun getResourceAsString(resourceName: String): String =
        javaClass.classLoader.getResourceAsStream(resourceName)?.bufferedReader()?.readText()
            ?: throw RuntimeException("Resource $resourceName not found in classpath")
}
