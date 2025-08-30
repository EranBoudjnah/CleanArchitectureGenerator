package com.mitteloupe.cag.cli

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ArgumentParserFeatureNamesTest {
    private lateinit var classUnderTest: ArgumentParser

    @Before
    fun setUp() {
        classUnderTest = ArgumentParser()
    }

    @Test
    fun `Given no args when parseFeatureNames then returns empty list`() {
        // Given
        val givenArguments = emptyArray<String>()

        // When
        val result = classUnderTest.parseFeatureNames(givenArguments)

        // Then
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `Given --new-feature when parseFeatureNames then returns feature name`() {
        // Given
        val givenArguments = arrayOf("--new-feature=FeatureName")

        // When
        val result = classUnderTest.parseFeatureNames(givenArguments)

        // Then
        assertEquals(listOf("FeatureName"), result)
    }

    @Test
    fun `Given --new-feature space value when parseFeatureNames then returns feature name`() {
        // Given
        val givenArguments = arrayOf("--new-feature", "FeatureName")

        // When
        val result = classUnderTest.parseFeatureNames(givenArguments)

        // Then
        assertEquals(listOf("FeatureName"), result)
    }

    @Test
    fun `Given -nf when parseFeatureNames then returns feature name`() {
        // Given
        val givenArguments = arrayOf("-nf=FeatureName")

        // When
        val result = classUnderTest.parseFeatureNames(givenArguments)

        // Then
        assertEquals(listOf("FeatureName"), result)
    }

    @Test
    fun `Given -nf space value when parseFeatureNames then returns feature name`() {
        // Given
        val givenArguments = arrayOf("-nf", "FeatureName")

        // When
        val result = classUnderTest.parseFeatureNames(givenArguments)

        // Then
        assertEquals(listOf("FeatureName"), result)
    }

    @Test
    fun `Given -nfAttached when parseFeatureNames then returns feature name`() {
        // Given
        val givenArguments = arrayOf("-nfFeatureName")

        // When
        val result = classUnderTest.parseFeatureNames(givenArguments)

        // Then
        assertEquals(listOf("FeatureName"), result)
    }

    @Test
    fun `Given multiple flags when parseFeatureNames then returns all features in order`() {
        // Given
        val givenArguments = arrayOf("--new-feature=First", "-nf=Second", "--new-feature=Third")

        // When
        val result = classUnderTest.parseFeatureNames(givenArguments)

        // Then
        assertEquals(listOf("First", "Second", "Third"), result)
    }
}

class ArgumentParserDataSourceTest {
    private lateinit var classUnderTest: ArgumentParser

    @Before
    fun setUp() {
        classUnderTest = ArgumentParser()
    }

    @Test
    fun `Given --new-datasource when parseDataSourceNames then returns datasource name`() {
        // Given
        val givenArguments = arrayOf("--new-datasource=My")
        val givenDataSourceNamePrefix = "My"
        val expected = listOf("$givenDataSourceNamePrefix$DATA_SOURCE_NAME_SUFFIX")

        // When
        val result = classUnderTest.parseDataSourceNames(givenArguments)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given --new-datasource space value when parseDataSourceNames then returns datasource name`() {
        // Given
        val givenArguments = arrayOf("--new-datasource", "My")
        val expected = listOf("My$DATA_SOURCE_NAME_SUFFIX")

        // When
        val result = classUnderTest.parseDataSourceNames(givenArguments)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given -nds when parseDataSourceNames then returns datasource name`() {
        // Given
        val givenDataSourceNamePrefix = "Your"
        val givenArguments = arrayOf("-nds=$givenDataSourceNamePrefix")
        val expected = listOf("$givenDataSourceNamePrefix$DATA_SOURCE_NAME_SUFFIX")

        // When
        val result = classUnderTest.parseDataSourceNames(givenArguments)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given -nds space value when parseDataSourceNames then returns datasource name`() {
        // Given
        val givenArguments = arrayOf("-nds", "Your")
        val expected = listOf("Your$DATA_SOURCE_NAME_SUFFIX")

        // When
        val result = classUnderTest.parseDataSourceNames(givenArguments)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given -ndsAttached when parseDataSourceNames then returns datasource name`() {
        // Given
        val givenArguments = arrayOf("-ndsYour")
        val expected = listOf("Your$DATA_SOURCE_NAME_SUFFIX")

        // When
        val result = classUnderTest.parseDataSourceNames(givenArguments)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given multiple flags when parseDataSourceNames then returns all in order`() {
        // Given
        val givenDataSourceNamePrefix1 = "First"
        val givenDataSourceNamePrefix2 = "Second"
        val givenDataSourceNamePrefix3 = "Third"
        val givenArguments =
            arrayOf(
                "--new-datasource=$givenDataSourceNamePrefix1",
                "-nds=$givenDataSourceNamePrefix2",
                "--new-datasource=$givenDataSourceNamePrefix3"
            )
        val expected =
            listOf(
                "$givenDataSourceNamePrefix1$DATA_SOURCE_NAME_SUFFIX",
                "$givenDataSourceNamePrefix2$DATA_SOURCE_NAME_SUFFIX",
                "$givenDataSourceNamePrefix3$DATA_SOURCE_NAME_SUFFIX"
            )

        // When
        val result = classUnderTest.parseDataSourceNames(givenArguments)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given name without suffix when ensureDataSourceSuffix then appends suffix`() {
        // Given
        val givenDataSourceNamePrefix = "My"
        val givenArguments = arrayOf("--new-datasource=$givenDataSourceNamePrefix")

        // When
        val result = classUnderTest.parseDataSourceNames(givenArguments)

        // Then
        assertEquals(listOf("${givenDataSourceNamePrefix}$DATA_SOURCE_NAME_SUFFIX"), result)
    }

    @Test
    fun `Given name with suffix when ensureDataSourceSuffix then returns same`() {
        // Given
        val givenFullDataSourceName = "AwesomeDataSource"
        val givenArguments = arrayOf("--new-datasource=$givenFullDataSourceName")

        // When
        val result = classUnderTest.parseDataSourceNames(givenArguments)

        // Then
        assertEquals(listOf(givenFullDataSourceName), result)
    }

    companion object {
        private const val DATA_SOURCE_NAME_SUFFIX = "DataSource"
    }
}

class ArgumentParserFeaturePackagesTest {
    private lateinit var classUnderTest: ArgumentParser

    @Before
    fun setUp() {
        classUnderTest = ArgumentParser()
    }

    @Test
    fun `Given single feature without package when parseFeaturePackages then returns null`() {
        // Given
        val givenArguments = arrayOf("--new-feature=FeatureName")

        // When
        val result = classUnderTest.parseFeaturePackages(givenArguments)

        // Then
        assertEquals(listOf<String?>(null), result)
    }

    @Test
    fun `Given feature with package when parseFeaturePackages then returns package`() {
        // Given
        val givenArguments = arrayOf("--new-feature=FeatureName", "--package=com.example.feature")

        // When
        val result = classUnderTest.parseFeaturePackages(givenArguments)

        // Then
        assertEquals(listOf("com.example.feature"), result)
    }

    @Test
    fun `Given feature with space separated package when parseFeaturePackages then returns package`() {
        // Given
        val givenArguments = arrayOf("--new-feature=FeatureName", "--package", "com.example.feature")

        // When
        val result = classUnderTest.parseFeaturePackages(givenArguments)

        // Then
        assertEquals(listOf("com.example.feature"), result)
    }

    @Test
    fun `Given short feature with attached package when parseFeaturePackages then returns package`() {
        // Given
        val givenArguments = arrayOf("-nf=First", "-pcom.first")

        // When
        val result = classUnderTest.parseFeaturePackages(givenArguments)

        // Then
        assertEquals(listOf("com.first"), result)
    }

    @Test
    fun `Given mixed short flags when parseFeaturePackages then maps in order`() {
        // Given
        val givenArguments = arrayOf("-nf=First", "-p=com.first", "--new-feature=Second")

        // When
        val result = classUnderTest.parseFeaturePackages(givenArguments)

        // Then
        assertEquals(listOf("com.first", null), result)
    }
}
