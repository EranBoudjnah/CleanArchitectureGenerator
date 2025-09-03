package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses

@RunWith(Enclosed::class)
@SuiteClasses(
    AppArgumentProcessorTest.Features::class,
    AppArgumentProcessorTest.Help::class
)
class AppArgumentProcessorTest {
    class Help {
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

    class Features {
        private lateinit var classUnderTest: AppArgumentProcessor

        @Before
        fun setUp() {
            classUnderTest = AppArgumentProcessor()
        }

        @Test
        fun `Given features with optional packages when getNewFeatures then maps in order`() {
            // Given
            val givenArguments =
                arrayOf("--new-feature", "--name=First", "--package=com.first", "--new-feature", "--name=Second")

            // When
            val result = classUnderTest.getNewFeatures(givenArguments)

            // Then
            assertEquals(listOf(FeatureRequest("First", "com.first"), FeatureRequest("Second", null)), result)
        }

        @Test
        fun `Given short flags when getNewFeatures then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-nf", "-nThird", "-p", "com.third", "-nf", "-n=Fourth", "-pcom.fourth")

            // When
            val result = classUnderTest.getNewFeatures(givenArguments)

            // Then
            assertEquals(listOf(FeatureRequest("Third", "com.third"), FeatureRequest("Fourth", "com.fourth")), result)
        }
    }

    class AppArgumentProcessorArchitectureTest {
        private lateinit var classUnderTest: AppArgumentProcessor

        @Before
        fun setUp() {
            classUnderTest = AppArgumentProcessor()
        }

        @Test
        fun `Given --new-architecture when getNewArchitecture then returns single request with compose enabled`() {
            // Given
            val givenArguments = arrayOf("--new-architecture")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(true, result[0].enableCompose)
        }

        @Test
        fun `Given --new-architecture --no-compose when getNewArchitecture then returns single request with compose disabled`() {
            // Given
            val givenArguments = arrayOf("--new-architecture", "--no-compose")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(false, result[0].enableCompose)
        }

        @Test
        fun `Given short flags when getNewArchitecture then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-na", "-nc")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(false, result[0].enableCompose)
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
            val givenArguments = arrayOf("--new-datasource", "--name=My", "--with=ktor")

            // When
            val result = classUnderTest.getNewDataSources(givenArguments)

            // Then
            assertEquals(listOf(DataSourceRequest("MyDataSource", useKtor = true, useRetrofit = false)), result)
        }

        @Test
        fun `Given -w retrofit when getNewDataSources then returns single request with retrofit`() {
            // Given
            val givenArguments = arrayOf("-nds", "-n=My", "-w", "retrofit")

            // When
            val result = classUnderTest.getNewDataSources(givenArguments)

            // Then
            assertEquals(listOf(DataSourceRequest("MyDataSource", useKtor = false, useRetrofit = true)), result)
        }

        @Test
        fun `Given with both token when getNewDataSources then ignores unknown token`() {
            // Given
            val givenArguments = arrayOf("-nds", "-n=Your", "-w=both")

            // When
            val result = classUnderTest.getNewDataSources(givenArguments)

            // Then
            assertEquals(listOf(DataSourceRequest("YourDataSource", useKtor = false, useRetrofit = false)), result)
        }

        @Test
        fun `Given with comma separated when getNewDataSources then returns single request with both`() {
            // Given
            val givenArguments = arrayOf("-nds", "-n=Your", "-wktor,retrofit")

            // When
            val result = classUnderTest.getNewDataSources(givenArguments)

            // Then
            assertEquals(listOf(DataSourceRequest("YourDataSource", useKtor = true, useRetrofit = true)), result)
        }
    }
}
