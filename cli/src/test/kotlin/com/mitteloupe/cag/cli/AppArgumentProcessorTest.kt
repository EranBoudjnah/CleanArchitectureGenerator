package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import com.mitteloupe.cag.cli.request.ProjectTemplateRequest
import com.mitteloupe.cag.cli.request.UseCaseRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses

@RunWith(Enclosed::class)
@SuiteClasses(
    AppArgumentProcessorTest.Features::class,
    AppArgumentProcessorTest.Help::class,
    AppArgumentProcessorTest.AppArgumentProcessorArchitectureTest::class,
    AppArgumentProcessorTest.AppArgumentProcessorDataSourcesTest::class,
    AppArgumentProcessorTest.AppArgumentProcessorUseCasesTest::class,
    AppArgumentProcessorTest.AppArgumentProcessorProjectTemplateTest::class
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

        @Test
        fun `Given feature with missing name when getNewFeatures then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-feature")

            // When
            try {
                classUnderTest.getNewFeatures(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Feature name is required. Use --name=FeatureName or -n=FeatureName", exception.message)
            }
        }

        @Test
        fun `Given feature with mixed long and short forms when getNewFeatures then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-feature", "-n=Test")

            // When
            try {
                classUnderTest.getNewFeatures(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Feature name is required. Use --name=FeatureName or -n=FeatureName", exception.message)
            }
        }

        @Test
        fun `Given feature with short primary and long secondaries when getNewFeatures then throws exception`() {
            // Given
            val givenArguments = arrayOf("-nf", "--name=Test")

            // When
            try {
                classUnderTest.getNewFeatures(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Feature name is required. Use --name=FeatureName or -n=FeatureName", exception.message)
            }
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

        @Test
        fun `Given --new-architecture --detekt when getNewArchitecture then returns single request with detekt enabled`() {
            // Given
            val givenArguments = arrayOf("--new-architecture", "--detekt")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(true, result[0].enableCompose)
            assertEquals(false, result[0].enableKtlint)
            assertEquals(true, result[0].enableDetekt)
        }

        @Test
        fun `Given --new-architecture --no-compose --detekt when getNewArchitecture then returns correct flags`() {
            // Given
            val givenArguments = arrayOf("--new-architecture", "--no-compose", "--detekt")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(false, result[0].enableCompose)
            assertEquals(false, result[0].enableKtlint)
            assertEquals(true, result[0].enableDetekt)
        }

        @Test
        fun `Given short detekt flag when getNewArchitecture then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-na", "-d")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(true, result[0].enableCompose)
            assertEquals(false, result[0].enableKtlint)
            assertEquals(true, result[0].enableDetekt)
        }

        @Test
        fun `Given --new-architecture --ktlint when getNewArchitecture then returns single request with ktlint enabled`() {
            // Given
            val givenArguments = arrayOf("--new-architecture", "--ktlint")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(true, result[0].enableCompose)
            assertEquals(true, result[0].enableKtlint)
            assertEquals(false, result[0].enableDetekt)
        }

        @Test
        fun `Given --new-architecture --ktlint --detekt when getNewArchitecture then returns single request with both enabled`() {
            // Given
            val givenArguments = arrayOf("--new-architecture", "--ktlint", "--detekt")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(true, result[0].enableCompose)
            assertEquals(true, result[0].enableKtlint)
            assertEquals(true, result[0].enableDetekt)
        }

        @Test
        fun `Given short ktlint flag when getNewArchitecture then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-na", "-kl")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertEquals(true, result[0].enableCompose)
            assertEquals(true, result[0].enableKtlint)
            assertEquals(false, result[0].enableDetekt)
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

        @Test
        fun `Given data source with missing name when getNewDataSources then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-datasource")

            // When
            try {
                classUnderTest.getNewDataSources(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Data source name is required. Use --name=DataSourceName or -n=DataSourceName", exception.message)
            }
        }

        @Test
        fun `Given data source with mixed long and short forms when getNewDataSources then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-datasource", "-n=Test")

            // When
            try {
                classUnderTest.getNewDataSources(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Data source name is required. Use --name=DataSourceName or -n=DataSourceName", exception.message)
            }
        }

        @Test
        fun `Given data source with short primary and long secondaries when getNewDataSources then throws exception`() {
            // Given
            val givenArguments = arrayOf("-nds", "--name=Test")

            // When
            try {
                classUnderTest.getNewDataSources(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Data source name is required. Use --name=DataSourceName or -n=DataSourceName", exception.message)
            }
        }
    }

    class AppArgumentProcessorUseCasesTest {
        private lateinit var classUnderTest: AppArgumentProcessor

        @Before
        fun setUp() {
            classUnderTest = AppArgumentProcessor()
        }

        @Test
        fun `Given use case with all optional parameters when getNewUseCases then returns complete request`() {
            // Given
            val givenArguments = arrayOf("--new-use-case", "--name=GetUser", "--path=user", "--input-type=String", "--output-type=User")

            // When
            val result = classUnderTest.getNewUseCases(givenArguments)

            // Then
            assertEquals(listOf(UseCaseRequest("GetUser", "user", "String", "User")), result)
        }

        @Test
        fun `Given use case with short flags when getNewUseCases then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-nuc", "-n=CreateUser", "-p", "user", "-it", "UserData", "-ot", "User")

            // When
            val result = classUnderTest.getNewUseCases(givenArguments)

            // Then
            assertEquals(listOf(UseCaseRequest("CreateUser", "user", "UserData", "User")), result)
        }

        @Test
        fun `Given use case with only name when getNewUseCases then returns request with null optional parameters`() {
            // Given
            val givenArguments = arrayOf("--new-use-case", "--name=DeleteUser")

            // When
            val result = classUnderTest.getNewUseCases(givenArguments)

            // Then
            assertEquals(listOf(UseCaseRequest("DeleteUser", null, null, null)), result)
        }

        @Test
        fun `Given multiple use cases when getNewUseCases then returns all requests`() {
            // Given
            val givenArguments = arrayOf("--new-use-case", "--name=First", "--new-use-case", "--name=Second", "--path=test")

            // When
            val result = classUnderTest.getNewUseCases(givenArguments)

            // Then
            assertEquals(listOf(UseCaseRequest("First", null, null, null), UseCaseRequest("Second", "test", null, null)), result)
        }

        @Test
        fun `Given use case with empty name when getNewUseCases then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-use-case", "--name=")

            // When
            try {
                classUnderTest.getNewUseCases(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Use case name is required. Use --name=UseCaseName or -n=UseCaseName", exception.message)
            }
        }

        @Test
        fun `Given use case with missing name when getNewUseCases then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-use-case")

            // When
            try {
                classUnderTest.getNewUseCases(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Use case name is required. Use --name=UseCaseName or -n=UseCaseName", exception.message)
            }
        }

        @Test
        fun `Given use case with mixed long and short forms when getNewUseCases then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-use-case", "-n=Test")

            // When
            try {
                classUnderTest.getNewUseCases(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Use case name is required. Use --name=UseCaseName or -n=UseCaseName", exception.message)
            }
        }

        @Test
        fun `Given use case with short primary and long secondaries when getNewUseCases then throws exception`() {
            // Given
            val givenArguments = arrayOf("-nuc", "--name=Test")

            // When
            try {
                classUnderTest.getNewUseCases(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Use case name is required. Use --name=UseCaseName or -n=UseCaseName", exception.message)
            }
        }
    }

    class AppArgumentProcessorProjectTemplateTest {
        private lateinit var classUnderTest: AppArgumentProcessor

        @Before
        fun setUp() {
            classUnderTest = AppArgumentProcessor()
        }

        @Test
        fun `Given project template with all parameters when getNewProjectTemplate then returns complete request`() {
            // Given
            val givenArguments =
                arrayOf("--new-project", "--name=MyApp", "--package=com.example", "--ktlint", "--detekt", "--ktor", "--retrofit")

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(ProjectTemplateRequest("MyApp", "com.example", true, true, true, true, true)), result)
        }

        @Test
        fun `Given project template with short flags when getNewProjectTemplate then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-np", "-n=TestApp", "-p", "com.test", "-nc", "-kl", "-d", "-kt", "-rt")

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(ProjectTemplateRequest("TestApp", "com.test", false, true, true, true, true)), result)
        }

        @Test
        fun `Given project template with only name when getNewProjectTemplate then returns request with defaults`() {
            // Given
            val givenArguments = arrayOf("--new-project", "--name=MinimalApp")

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(ProjectTemplateRequest("MinimalApp", "", true, false, false, false, false)), result)
        }

        @Test
        fun `Given project template with no compose when getNewProjectTemplate then disables compose`() {
            // Given
            val givenArguments = arrayOf("--new-project", "--name=NoComposeApp", "--no-compose")

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(ProjectTemplateRequest("NoComposeApp", "", false, false, false, false, false)), result)
        }

        @Test
        fun `Given multiple project templates when getNewProjectTemplate then returns all requests`() {
            // Given
            val givenArguments = arrayOf("--new-project", "--name=First", "--package=com.first", "--new-project", "--name=Second")

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(
                listOf(
                    ProjectTemplateRequest("First", "com.first", true, false, false, false, false),
                    ProjectTemplateRequest("Second", "", true, false, false, false, false)
                ),
                result
            )
        }

        @Test
        fun `Given project template with empty name when getNewProjectTemplate then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-project", "--name=")

            // When
            try {
                classUnderTest.getNewProjectTemplate(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Project name is required. Use --name=ProjectName or -n=ProjectName", exception.message)
            }
        }

        @Test
        fun `Given project template with missing name when getNewProjectTemplate then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-project")

            // When
            try {
                classUnderTest.getNewProjectTemplate(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Project name is required. Use --name=ProjectName or -n=ProjectName", exception.message)
            }
        }

        @Test
        fun `Given project template with mixed long and short forms when getNewProjectTemplate then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-project", "-n=Test")

            // When
            try {
                classUnderTest.getNewProjectTemplate(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Project name is required. Use --name=ProjectName or -n=ProjectName", exception.message)
            }
        }

        @Test
        fun `Given project template with short primary and long secondaries when getNewProjectTemplate then throws exception`() {
            // Given
            val givenArguments = arrayOf("-np", "--name=Test")

            // When
            try {
                classUnderTest.getNewProjectTemplate(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Project name is required. Use --name=ProjectName or -n=ProjectName", exception.message)
            }
        }
    }
}
