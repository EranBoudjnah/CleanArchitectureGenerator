package com.mitteloupe.cag.cli.configuration

import com.mitteloupe.cag.cli.configuration.model.DependencyInjection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class VersionConfigLoaderTest {
    private lateinit var classUnderTest: ClientConfigurationLoader

    @Before
    fun setUp() {
        classUnderTest = ClientConfigurationLoader()
    }

    @Test
    fun `Given INI text with new and existing sections when parse then parses values`() {
        // Given
        val text =
            """
            # Comment
            [new.versions]
            kotlin=2.2.99
            composeBom=2099.01.01

            [existing.versions]
            retrofit=2.99.0
            ktor=9.9.9
            """.trimIndent()
        val expectedClientConfiguration =
            ClientConfiguration(
                newProjectVersions =
                    mapOf(
                        "kotlin" to "2.2.99",
                        "composeBom" to "2099.01.01"
                    ),
                existingProjectVersions =
                    mapOf(
                        "retrofit" to "2.99.0",
                        "ktor" to "9.9.9"
                    )
            )

        // When
        val actualConfiguration = classUnderTest.parse(text)

        // Then
        assertEquals(expectedClientConfiguration, actualConfiguration)
    }

    @Test
    fun `Given INI with git section when parse then parses git booleans`() {
        // Given
        val text =
            """
            [git]
            autoInitialize=true
            autoStage=false
            """.trimIndent()

        // When
        val actualConfiguration = classUnderTest.parse(text)

        // Then
        assertTrue(actualConfiguration.git.autoInitialize!!)
        assertFalse(actualConfiguration.git.autoStage!!)
    }

    @Test
    fun `Given INI with injection section when parse then parses git booleans`() {
        // Given
        val text =
            """
            [dependencyInjection]
            library=Koin
            """.trimIndent()
        val expectedLibrary = DependencyInjection.Koin

        // When
        val actualConfiguration = classUnderTest.parse(text)

        // Then
        assertEquals(expectedLibrary, actualConfiguration.dependencyInjection.library)
    }

    @Test
    fun `Given home, project configurations when loadFromFiles then project overrides home`() {
        // Given
        val temporaryDirectory = createTempDirectory(prefix = "cag-test-").toFile()
        try {
            val homeFile =
                File(temporaryDirectory, "home.cagrc").apply {
                    writeText(
                        """
                        [new.versions]
                        composeBom=2000.01.01
                        ktor=1.0.0
                        [existing.versions]
                        retrofit=2.0.0
                        [git]
                        autoInitialize=true
                        autoStage=false
                        """.trimIndent()
                    )
                }

            val projectFile =
                File(temporaryDirectory, ".cagrc").apply {
                    writeText(
                        """
                        [new.versions]
                        composeBom=2025.08.01
                        [existing.versions]
                        retrofit=2.11.0
                        okhttp3=4.12.0
                        [git]
                        autoInitialize=false
                        autoStage=true
                        """.trimIndent()
                    )
                }
            val expectedConfiguration =
                ClientConfiguration(
                    newProjectVersions =
                        mapOf(
                            "composeBom" to "2025.08.01",
                            "ktor" to "1.0.0"
                        ),
                    existingProjectVersions =
                        mapOf(
                            "retrofit" to "2.11.0",
                            "okhttp3" to "4.12.0"
                        ),
                    git = GitConfiguration(autoInitialize = false, autoStage = true)
                )

            // When
            val actualConfiguration = classUnderTest.loadFromFiles(projectFile = projectFile, homeFile = homeFile)

            // Then
            assertEquals(expectedConfiguration, actualConfiguration)
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }
}
