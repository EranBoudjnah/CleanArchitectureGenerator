package com.mitteloupe.cag.core

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNull
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
    fun `Given no catalog file when updateVersionCatalogIfPresent then returns null`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "noCatalog").toFile()

        // When
        val result =
            classUnderTest.updateVersionCatalogIfPresent(
                projectRootDir = projectRoot,
                sectionRequirements = emptyList()
            )

        // Then
        assertNull(result)
    }

    @Test
    fun `Given no versions section when updateVersionCatalogIfPresent then inserts at start with single separator after`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [plugins]
                    android-application = { id = "com.android.application", version = "1.0.0" }
                    """.trimIndent()
            )
        val section =
            SectionTransaction(
                sectionHeader = "versions",
                insertPositionIfMissing = CatalogInsertPosition.Start,
                requirements =
                    listOf(
                        SectionRequirement("^\\s*compileSdk\\s*=.*$".toRegex(), "compileSdk = \"35\""),
                        SectionRequirement("^\\s*minSdk\\s*=.*$".toRegex(), "minSdk = \"24\"")
                    )
            )

        val expected =
            """
            [versions]
            compileSdk = "35"
            minSdk = "24"

            [plugins]
            android-application = { id = "com.android.application", version = "1.0.0" }
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateVersionCatalogIfPresent(
                projectRootDir = projectRoot,
                sectionRequirements = listOf(section)
            )

        // Then
        assertNull(result)
        assertThat(catalogFile.readText(), equalTo(expected))
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given versions section with trailing blanks when updateVersionCatalogIfPresent then adds entries without gaps and keeps single separator`() {
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
        val section =
            SectionTransaction(
                sectionHeader = "versions",
                insertPositionIfMissing = CatalogInsertPosition.Start,
                requirements =
                    listOf(
                        SectionRequirement("^\\s*compileSdk\\s*=.*$".toRegex(), "compileSdk = \"35\""),
                        SectionRequirement("^\\s*minSdk\\s*=.*$".toRegex(), "minSdk = \"24\"")
                    )
            )

        val expected =
            """
            [versions]
            compileSdk = "35"
            minSdk = "24"

            [plugins]
            android-application = { id = "com.android.application", version.ref = "agp" }
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateVersionCatalogIfPresent(
                projectRootDir = projectRoot,
                sectionRequirements = listOf(section)
            )

        // Then
        assertNull(result)
        assertThat(catalogFile.readText(), equalTo(expected))
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given no plugins section, no trailing newline when updateVersionCatalogIfPresent with END then appends with single separator and trailing newline`() {
        // Given
        val (projectRoot, catalogFile) =
            createProjectWithCatalog(
                initialContent =
                    """
                    [versions]
                    compileSdk = "35"
                    """.trimIndent()
            )
        val section =
            SectionTransaction(
                sectionHeader = "plugins",
                insertPositionIfMissing = CatalogInsertPosition.End,
                requirements =
                    listOf(
                        SectionRequirement(
                            "^\\s*android-library\\s*=.*$".toRegex(),
                            "android-library = { id = \"com.android.library\", version.ref = \"agp\" }"
                        ),
                        SectionRequirement(
                            "^\\s*kotlin-jvm\\s*=.*$".toRegex(),
                            "kotlin-jvm = { id = \"org.jetbrains.kotlin.jvm\", version.ref = \"kotlin\" }"
                        )
                    )
            )

        val expected =
            """
            [versions]
            compileSdk = "35"

            [plugins]
            android-library = { id = "com.android.library", version.ref = "agp" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            """.trimIndent() + "\n"

        // When
        val result =
            classUnderTest.updateVersionCatalogIfPresent(
                projectRootDir = projectRoot,
                sectionRequirements = listOf(section)
            )

        // Then
        assertNull(result)
        assertThat(catalogFile.readText(), equalTo(expected))
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given no plugins section, trailing newline when updateVersionCatalogIfPresent with END then appends with single separator and trailing newline`() {
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
        val section =
            SectionTransaction(
                sectionHeader = "plugins",
                insertPositionIfMissing = CatalogInsertPosition.End,
                requirements =
                    listOf(
                        SectionRequirement(
                            "^\\s*android-library\\s*=.*$".toRegex(),
                            "android-library = { id = \"com.android.library\", version.ref = \"agp\" }"
                        ),
                        SectionRequirement(
                            "^\\s*kotlin-jvm\\s*=.*$".toRegex(),
                            "kotlin-jvm = { id = \"org.jetbrains.kotlin.jvm\", version.ref = \"kotlin\" }"
                        )
                    )
            )

        val expected =
            """
            [versions]
            compileSdk = "35"

            [plugins]
            android-library = { id = "com.android.library", version.ref = "agp" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            """.trimIndent() + "\n"

        // When
        val result =
            classUnderTest.updateVersionCatalogIfPresent(
                projectRootDir = projectRoot,
                sectionRequirements = listOf(section)
            )

        // Then
        assertNull(result)
        assertThat(catalogFile.readText(), equalTo(expected))
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given plugins section with one required entry missing when updateVersionCatalogIfPresent with END then appends missing entry and keeps single separator`() {
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
        val section =
            SectionTransaction(
                sectionHeader = "plugins",
                insertPositionIfMissing = CatalogInsertPosition.End,
                requirements =
                    listOf(
                        SectionRequirement(
                            "^\\s*android-library\\s*=.*$".toRegex(),
                            "android-library = { id = \"com.android.library\", version.ref = \"agp\" }"
                        ),
                        SectionRequirement(
                            "^\\s*kotlin-jvm\\s*=.*$".toRegex(),
                            "kotlin-jvm = { id = \"org.jetbrains.kotlin.jvm\", version.ref = \"kotlin\" }"
                        )
                    )
            )

        val expected =
            """
            [versions]
            agp = "35"

            [plugins]
            android-library = { id = "com.android.library", version.ref = "agp" }
            kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            """.trimIndent()

        // When
        val result =
            classUnderTest.updateVersionCatalogIfPresent(
                projectRootDir = projectRoot,
                sectionRequirements = listOf(section)
            )

        // Then
        assertNull(result)
        assertThat(catalogFile.readText(), equalTo(expected))
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
