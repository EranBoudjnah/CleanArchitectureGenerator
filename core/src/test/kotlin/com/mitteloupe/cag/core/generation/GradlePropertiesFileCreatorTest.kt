package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class GradlePropertiesFileCreatorTest {
    private lateinit var classUnderTest: GradlePropertiesFileCreator

    private val expectedGradlePropertiesContent =
        """
        # Project-wide Gradle settings.
        # IDE (e.g. Android Studio) users:
        # Gradle settings configured through the IDE *will override*
        # any settings specified in this file.
        # For more details on how to configure your build environment visit
        # http://www.gradle.org/docs/current/userguide/build_environment.html
        # Specifies the JVM arguments used for the daemon process.
        # The setting is particularly useful for tweaking memory settings.
        org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
        # When configured, Gradle will run in incubating parallel mode.
        # This option should only be used with decoupled projects. More details, visit
        # http://www.gradle.org/docs/current/userguide/multi_project_builds.html#sec:decoupled_projects
        # org.gradle.parallel=true
        # AndroidX package structure to make it clearer which packages are bundled with the
        # Android operating system, and which are packaged with your app's APK
        # https://developer.android.com/topic/libraries/support-library/androidx-rn
        android.useAndroidX=true
        # Kotlin code style for this project: "official" or "obsolete":
        kotlin.code.style=official
        # Enables namespacing of each library's R class so that its R class includes only the
        # resources declared in the library itself and none from the library's dependencies,
        # thereby reducing the size of the R class for that library
        android.nonTransitiveRClass=true
        """.trimIndent()

    @Before
    fun setUp() {
        classUnderTest = GradlePropertiesFileCreator(FileCreator(FakeFileSystemBridge()))
    }

    @Test
    fun `Given project root when writeGradlePropertiesFile then creates gradle properties file with correct content`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()

        // When
        classUnderTest.writeGradlePropertiesFile(projectRoot)

        // Then
        val gradlePropertiesFile = File(projectRoot, "gradle.properties")
        val actualContent = gradlePropertiesFile.readText()
        assertEquals(expectedGradlePropertiesContent, actualContent)
    }

    @Test
    fun `Given project root with existing gradle properties when writeGradlePropertiesFile then skips update`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot2").toFile()
        val gradlePropertiesFile = File(projectRoot, "gradle.properties")
        val existingContent = "existing.content=value"
        gradlePropertiesFile.writeText(existingContent)

        // When
        classUnderTest.writeGradlePropertiesFile(projectRoot)

        // Then
        val actualContent = gradlePropertiesFile.readText()
        assertEquals(existingContent, actualContent)
    }
}
