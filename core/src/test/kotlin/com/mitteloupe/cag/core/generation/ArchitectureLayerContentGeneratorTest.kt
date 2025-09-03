package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class ArchitectureLayerContentGeneratorTest {
    private lateinit var classUnderTest: ArchitectureLayerContentGenerator
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        classUnderTest = ArchitectureLayerContentGenerator()
        tempDirectory = createTempDirectory(prefix = "test").toFile()
    }

    @Test(expected = GenerationException::class)
    fun `Given empty architecture package name when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = ""
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given invalid architecture package name when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "..."
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }

    @Test
    fun `Given valid architecture package name when generate then creates layer structure and returns null`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val layers = listOf("domain", "presentation", "ui")
        layers.forEach { layer ->
            val layerSourceRoot = File(architectureRoot, "$layer/src/main/java")
            assertTrue("Layer $layer should exist", layerSourceRoot.exists())

            val packageSegments = architecturePackageName.split(".")
            val packageDirectory =
                packageSegments.fold(layerSourceRoot) { parent, segment ->
                    File(parent, segment)
                }
            assertTrue("Package directory for $layer should exist", packageDirectory.exists())
        }
    }

    @Test
    fun `Given existing layer directories when generate then succeeds`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        val domainLayer = File(architectureRoot, "domain/src/main/java")
        domainLayer.mkdirs()
        val packageDirectory = File(domainLayer, "com/example/architecture")
        packageDirectory.mkdirs()

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val layers = listOf("domain", "presentation", "ui")
        layers.forEach { layer ->
            val layerSourceRoot = File(architectureRoot, "$layer/src/main/java")
            assertTrue("Layer $layer should exist", layerSourceRoot.exists())
        }
    }

    @Test
    fun `Given architecture package with single segment when generate then creates structure`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val domainLayer = File(architectureRoot, "domain/src/main/java/architecture")
        assertTrue("Domain package should exist", domainLayer.exists())
    }

    @Test
    fun `Given architecture package with multiple segments when generate then creates nested structure`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.myapp.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val domainLayer = File(architectureRoot, "domain/src/main/java/com/example/myapp/architecture")
        assertTrue("Nested domain package should exist", domainLayer.exists())

        val presentationLayer = File(architectureRoot, "presentation/src/main/java/com/example/myapp/architecture")
        assertTrue("Nested presentation package should exist", presentationLayer.exists())

        val uiLayer = File(architectureRoot, "ui/src/main/java/com/example/myapp/architecture")
        assertTrue("Nested ui package should exist", uiLayer.exists())
    }

    @Test(expected = GenerationException::class)
    fun `Given architecture root that is a file when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { createNewFile() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }
}
