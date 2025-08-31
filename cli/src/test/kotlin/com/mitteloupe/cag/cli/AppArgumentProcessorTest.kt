package com.mitteloupe.cag.cli

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppArgumentProcessorHelpTest {
    private lateinit var classUnderTest: AppArgumentProcessor

    @Before
    fun setUp() {
        classUnderTest = AppArgumentProcessor()
    }

    @Test
    fun `Given --help when isHelpRequested then returns true`() {
        // Given
        val givenArguments = arrayOf("--help")

        // When
        val result = classUnderTest.isHelpRequested(givenArguments)

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `Given -h when isHelpRequested then returns true`() {
        // Given
        val givenArguments = arrayOf("-h")

        // When
        val result = classUnderTest.isHelpRequested(givenArguments)

        // Then
        assertEquals(true, result)
    }
}

class AppArgumentProcessorFeaturesTest {
    private lateinit var classUnderTest: AppArgumentProcessor

    @Before
    fun setUp() {
        classUnderTest = AppArgumentProcessor()
    }

    @Test
    fun `Given features with optional packages when getNewFeatures then maps in order`() {
        // Given
        val givenArguments = arrayOf("--new-feature=First", "--package=com.first", "--new-feature", "Second")

        // When
        val result = classUnderTest.getNewFeatures(givenArguments)

        // Then
        assertEquals(listOf(FeatureRequest("First", "com.first"), FeatureRequest("Second", null)), result)
    }

    @Test
    fun `Given short flags when getNewFeatures then parses correctly`() {
        // Given
        val givenArguments = arrayOf("-nfThird", "-p", "com.third", "-nf=Fourth", "-pcom.fourth")

        // When
        val result = classUnderTest.getNewFeatures(givenArguments)

        // Then
        assertEquals(listOf(FeatureRequest("Third", "com.third"), FeatureRequest("Fourth", "com.fourth")), result)
    }
}

class AppArgumentProcessorDataSourcesTest {
    private lateinit var classUnderTest: AppArgumentProcessor

    @Before
    fun setUp() {
        classUnderTest = AppArgumentProcessor()
    }

    @Test
    fun `Given --with ktor when getNewDataSources then returns single request with ktor`() {
        // Given
        val givenArguments = arrayOf("--new-datasource=My", "--with=ktor")

        // When
        val result = classUnderTest.getNewDataSources(givenArguments)

        // Then
        assertEquals(listOf(DataSourceRequest("MyDataSource", useKtor = true, useRetrofit = false)), result)
    }

    @Test
    fun `Given -w retrofit when getNewDataSources then returns single request with retrofit`() {
        // Given
        val givenArguments = arrayOf("--new-datasource", "My", "-w", "retrofit")

        // When
        val result = classUnderTest.getNewDataSources(givenArguments)

        // Then
        assertEquals(listOf(DataSourceRequest("MyDataSource", useKtor = false, useRetrofit = true)), result)
    }

    @Test
    fun `Given with both token when getNewDataSources then ignores unknown token`() {
        // Given
        val givenArguments = arrayOf("-nds=Your", "--with=both")

        // When
        val result = classUnderTest.getNewDataSources(givenArguments)

        // Then
        assertEquals(listOf(DataSourceRequest("YourDataSource", useKtor = false, useRetrofit = false)), result)
    }

    @Test
    fun `Given with comma separated when getNewDataSources then returns single request with both`() {
        // Given
        val givenArguments = arrayOf("-ndsYour", "-wktor,retrofit")

        // When
        val result = classUnderTest.getNewDataSources(givenArguments)

        // Then
        assertEquals(listOf(DataSourceRequest("YourDataSource", useKtor = true, useRetrofit = true)), result)
    }
}
