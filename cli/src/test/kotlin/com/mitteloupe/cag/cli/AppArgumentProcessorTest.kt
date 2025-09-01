package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import com.mitteloupe.cag.cli.request.UseCaseRequest
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

class AppArgumentProcessorUseCasesTest {
    private lateinit var classUnderTest: AppArgumentProcessor

    @Before
    fun setUp() {
        classUnderTest = AppArgumentProcessor()
    }

    @Test
    fun `Given use case with optional path when getNewUseCases then maps in order`() {
        // Given
        val givenArguments = arrayOf("--new-use-case=FirstUseCase", "--path=com.first", "--new-use-case", "SecondUseCase")

        // When
        val result = classUnderTest.getNewUseCases(givenArguments)

        // Then
        assertEquals(listOf(UseCaseRequest("FirstUseCase", "com.first"), UseCaseRequest("SecondUseCase", null)), result)
    }

    @Test
    fun `Given short flags when getNewUseCases then parses correctly`() {
        // Given
        val givenArguments = arrayOf("-nucThirdUseCase", "-p", "com.third", "-nuc=FourthUseCase", "-pcom.fourth")

        // When
        val result = classUnderTest.getNewUseCases(givenArguments)

        // Then
        assertEquals(listOf(UseCaseRequest("ThirdUseCase", "com.third"), UseCaseRequest("FourthUseCase", "com.fourth")), result)
    }

    @Test
    fun `Given use case without path when getNewUseCases then returns use case with null path`() {
        // Given
        val givenArguments = arrayOf("--new-use-case=SimpleUseCase")

        // When
        val result = classUnderTest.getNewUseCases(givenArguments)

        // Then
        assertEquals(listOf(UseCaseRequest("SimpleUseCase", null)), result)
    }

    @Test
    fun `Given use case with absolute path when getNewUseCases then returns use case with absolute path`() {
        // Given
        val givenArguments = arrayOf("--new-use-case=AbsoluteUseCase", "--path=/absolute/path")

        // When
        val result = classUnderTest.getNewUseCases(givenArguments)

        // Then
        assertEquals(listOf(UseCaseRequest("AbsoluteUseCase", "/absolute/path")), result)
    }

    @Test
    fun `Given use case with relative path when getNewUseCases then returns use case with relative path`() {
        // Given
        val givenArguments = arrayOf("--new-use-case=RelativeUseCase", "--path=./relative/path")

        // When
        val result = classUnderTest.getNewUseCases(givenArguments)

        // Then
        assertEquals(listOf(UseCaseRequest("RelativeUseCase", "./relative/path")), result)
    }

    @Test
    fun `Given multiple use cases with mixed path options when getNewUseCases then maps all correctly`() {
        // Given
        val givenArguments =
            arrayOf(
                "--new-use-case=FirstUseCase",
                "--path=path1",
                "-nuc=SecondUseCase",
                "-nucThirdUseCase",
                "-p",
                "path3"
            )

        // When
        val result = classUnderTest.getNewUseCases(givenArguments)

        // Then
        assertEquals(
            listOf(
                UseCaseRequest("FirstUseCase", "path1"),
                UseCaseRequest("SecondUseCase", null),
                UseCaseRequest("ThirdUseCase", "path3")
            ),
            result
        )
    }
}
