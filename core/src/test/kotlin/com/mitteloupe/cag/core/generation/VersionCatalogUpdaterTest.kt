package com.mitteloupe.cag.core.generation
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class VersionCatalogUpdaterTest {
    private lateinit var classUnderTest: VersionCatalogUpdater

    @Before
    fun setUp() {
        classUnderTest = VersionCatalogUpdater()
    }

    @Test
    fun `Given no catalog file when updateVersionCatalogIfPresent then does nothing`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "noCatalog").toFile()

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, enableCompose = false)

        // Then does nothing
    }

    @Test
    fun `Given no versions section when updateVersionCatalogIfPresent then adds desired plugins and versions`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [plugins]
                    android-application = { id = "com.android.application", version = "1.0.0" }
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            compileSdk = "35"
            minSdk = "24"
            androidGradlePlugin = "8.7.3"

            [plugins]
            android-application = { id = "com.android.application", version = "1.0.0" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            """.trimIndent()

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, enableCompose = false)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given versions section with trailing blanks when updateVersionCatalogIfPresent then trims gaps and adds plugins`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    compileSdk = "35"


                    [plugins]
                    android-application = { id = "com.android.application", version.ref = "agp" }
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            compileSdk = "35"
            minSdk = "24"
            androidGradlePlugin = "8.7.3"

            [plugins]
            android-application = { id = "com.android.application", version.ref = "agp" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            """.trimIndent()

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, enableCompose = false)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given no plugins section, when updateVersionCatalogIfPresent then appends desired plugins and versions with proper separators`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    compileSdk = "35"
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            compileSdk = "35"
            minSdk = "24"
            androidGradlePlugin = "8.7.3"

            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            """.trimIndent() + "\n"

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, enableCompose = false)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given no plugins section, trailing newline when updateVersionCatalogIfPresent then appends with single separator and trailing newline`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    (
                        """
                        [versions]
                        compileSdk = "35"
                        """.trimIndent() + "\n"
                    )
            )
        val expected =
            """
            [versions]
            compileSdk = "35"
            minSdk = "24"
            androidGradlePlugin = "8.7.3"

            [plugins]
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
            """.trimIndent() + "\n"

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, enableCompose = false)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given plugins section with one desired entry missing when updateVersionCatalogIfPresent then appends missing entries`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    agp = "35"

                    [plugins]
                    android-library = { id = "com.android.library", version.ref = "agp" }
                    """.trimIndent()
            )
        val expected =
            """
            [versions]
            agp = "35"
            compileSdk = "35"
            minSdk = "24"

            [plugins]
            android-library = { id = "com.android.library", version.ref = "agp" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
            """.trimIndent()

        // When
        classUnderTest.updateVersionCatalogIfPresent(projectRootDir = projectRoot, enableCompose = false)

        // Then
        assertEquals(expected, catalogFile.readText())
    }

    private fun createProjectWithCatalog(initialContent: String): Pair<File, File> {
        val projectRoot = createTempDirectory(prefix = "projectWithCatalog").toFile()
        val gradleDir = File(projectRoot, "gradle")
        gradleDir.mkdirs()
        val catalogFile = File(gradleDir, "libs.versions.toml")
        catalogFile.writeText(initialContent)
        return projectRoot to catalogFile
    }
}
