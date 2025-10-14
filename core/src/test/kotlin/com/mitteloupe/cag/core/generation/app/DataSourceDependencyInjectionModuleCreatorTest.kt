package com.mitteloupe.cag.core.generation.app

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class DataSourceDependencyInjectionModuleCreatorTest {
    private lateinit var classUnderTest: DataSourceDependencyInjectionModuleCreator

    @Before
    fun setUp() {
        classUnderTest = DataSourceDependencyInjectionModuleCreator(FileCreator(FakeFileSystemBridge()))
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
        val givenNamespace = "com.example.app"
        val targetDirectory = File(appRoot, "com/example/app/di")

        // When
        classUnderTest.writeDataSourceDependencyInjectionModule(
            destinationRootDirectory = projectRoot,
            projectNamespace = givenNamespace,
            dataSourceName = "ExampleDataSource"
        )

        // Then
        val file = File(targetDirectory, "ExampleDataSourceModule.kt")
        assertTrue(file.exists())
        val content = file.readText()
        assertThat(content, containsString("package com.example.app.di"))
        assertThat(
            content,
            containsString("object ExampleDataSourceModule")
        )
        assertThat(
            content,
            containsString(
                "fun providesExampleDataSource(): ExampleDataSource = ExampleDataSourceImpl()"
            )
        )
    }

    @Test(expected = GenerationException::class)
    fun `Given cannot create directory when writeDataSourceModule then throws exception`() {
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
            classUnderTest.writeDataSourceDependencyInjectionModule(
                destinationRootDirectory = projectRoot,
                projectNamespace = givenNamespace,
                dataSourceName = "ExampleDataSource"
            )
        } finally {
            blockingFile.delete()
        }

        // Then throws GenerationException
    }
}
