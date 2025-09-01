package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.ERROR_PREFIX
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class DataSourceModuleCreatorTest {
    private lateinit var classUnderTest: DataSourceModuleCreator

    @Before
    fun setUp() {
        classUnderTest = DataSourceModuleCreator()
    }

    @Test
    fun `Given implementation module exists when writeDataSourceModule then creates module file with content`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot").toFile()
        val appRoot = File(projectRoot, "app/src/main/java")
        appRoot.mkdirs()
        File(projectRoot, "app/build.gradle.kts").writeText(
            """
            plugins {
                id("com.android.application")
            }
            """.trimIndent()
        )
        val givenNamespace = "com.example.app."
        val targetDir = File(appRoot, "com/example/app/di")

        // When
        val result =
            classUnderTest.writeDataSourceModule(
                destinationRootDirectory = projectRoot,
                projectNamespace = givenNamespace,
                dataSourceName = "ExampleDataSource"
            )

        // Then
        assertNull(result)
        val file = File(targetDir, "ExampleDataSourceModule.kt")
        assertTrue(file.exists())
        val content = file.readText()
        assertThat(content, CoreMatchers.containsString("package com.example.app.di"))
        assertThat(
            content,
            CoreMatchers.containsString("object ExampleDataSourceModule")
        )
        assertThat(
            content,
            CoreMatchers.containsString(
                "fun providesExampleDataSource(): ExampleDataSource = ExampleDataSourceImpl()"
            )
        )
    }

    @Test
    fun `Given cannot create directory when writeDataSourceModule then returns error`() {
        // Given
        val projectRoot = createTempDirectory(prefix = "projectRoot2").toFile()
        val appRoot = File(projectRoot, "app/src/main/java")
        appRoot.mkdirs()
        File(projectRoot, "app/build.gradle.kts").writeText(
            """
            plugins {
                id("com.android.application")
            }
            """.trimIndent()
        )
        val givenNamespace = "com.example.app."
        val blockingFile = File(appRoot, "com/example/app/di")
        blockingFile.parentFile?.mkdirs()
        blockingFile.writeText("block")

        try {
            // When
            val result =
                classUnderTest.writeDataSourceModule(
                    destinationRootDirectory = projectRoot,
                    projectNamespace = givenNamespace,
                    dataSourceName = "ExampleDataSource"
                )

            // Then
            assertNotNull(result)
            checkNotNull(result)
            assertThat(result, CoreMatchers.startsWith(ERROR_PREFIX))
        } finally {
            blockingFile.delete()
        }
    }
}
