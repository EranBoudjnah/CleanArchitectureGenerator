package com.mitteloupe.cag.cli

import com.mitteloupe.cag.cli.request.DataSourceRequest
import com.mitteloupe.cag.cli.request.FeatureRequest
import com.mitteloupe.cag.cli.request.ProjectTemplateRequest
import com.mitteloupe.cag.cli.request.UseCaseRequest
import com.mitteloupe.cag.cli.request.ViewModelRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    AppArgumentProcessorTest.AppArgumentProcessorViewModelsTest::class,
    AppArgumentProcessorTest.AppArgumentProcessorProjectTemplateTest::class,
    AppArgumentProcessorTest.UnknownFlagsValidation::class
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

        @Test
        fun `Given --new-feature with --ktlint when getNewFeatures then returns FeatureRequest with ktlint enabled`() {
            // Given
            val givenArguments = arrayOf("--new-feature", "--name=Quality", "--ktlint")
            val expectedRequests =
                listOf(
                    FeatureRequest(
                        featureName = "Quality",
                        packageName = null,
                        enableKtlint = true,
                        enableDetekt = false
                    )
                )

            // When
            val result = classUnderTest.getNewFeatures(givenArguments)

            // Then
            assertEquals(expectedRequests, result)
        }

        @Test
        fun `Given short flags -nf -kl -d when getNewFeatures then returns FeatureRequest with both enabled`() {
            // Given
            val givenArguments = arrayOf("-nf", "-n=Quality", "-kl", "-d")
            val expectedRequests =
                listOf(
                    FeatureRequest(
                        featureName = "Quality",
                        packageName = null,
                        enableKtlint = true,
                        enableDetekt = true
                    )
                )

            // When
            val result = classUnderTest.getNewFeatures(givenArguments)

            // Then
            assertEquals(expectedRequests, result)
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
            val expectedRequests =
                listOf(
                    FeatureRequest(
                        featureName = "First",
                        packageName = "com.first",
                        enableKtlint = false,
                        enableDetekt = false
                    ),
                    FeatureRequest(
                        featureName = "Second",
                        packageName = null,
                        enableKtlint = false,
                        enableDetekt = false
                    )
                )

            // When
            val result = classUnderTest.getNewFeatures(givenArguments)

            // Then
            assertEquals(expectedRequests, result)
        }

        @Test
        fun `Given short flags when getNewFeatures then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-nf", "-nThird", "-p", "com.third", "-nf", "-n=Fourth", "-pcom.fourth")
            val expectedRequests =
                listOf(
                    FeatureRequest(
                        featureName = "Third",
                        packageName = "com.third",
                        enableKtlint = false,
                        enableDetekt = false
                    ),
                    FeatureRequest(
                        featureName = "Fourth",
                        packageName = "com.fourth",
                        enableKtlint = false,
                        enableDetekt = false
                    )
                )

            // When
            val result = classUnderTest.getNewFeatures(givenArguments)

            // Then
            assertEquals(expectedRequests, result)
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
                assertEquals(
                    "Cannot mix long form (--new-feature) with short form secondary flags (-n). Use --name instead.",
                    exception.message
                )
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
                assertEquals(
                    "Cannot mix short form (-nf) with long form secondary flags (--name). Use -n instead.",
                    exception.message
                )
            }
        }

        @Test
        fun `Given feature with long primary and short secondary and missing name when getNewFeatures then throws mixed form exception`() {
            // Given
            val givenArguments = arrayOf("--new-feature", "-n=Test")

            // When
            try {
                classUnderTest.getNewFeatures(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals(
                    "Cannot mix long form (--new-feature) with short form secondary flags (-n). Use --name instead.",
                    exception.message
                )
            }
        }

        @Test
        fun `Given feature with short primary and long secondary and missing name when getNewFeatures then throws mixed form exception`() {
            // Given
            val givenArguments = arrayOf("-nf", "--name=Test")

            // When
            try {
                classUnderTest.getNewFeatures(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Cannot mix short form (-nf) with long form secondary flags (--name). Use -n instead.", exception.message)
            }
        }

        @Test
        fun `Given --new-feature with --git when getNewFeatures then returns FeatureRequest with git enabled`() {
            // Given
            val givenArguments = arrayOf("--new-feature", "--name=Quality", "--git")
            val expectedRequest =
                FeatureRequest(
                    featureName = "Quality",
                    packageName = null,
                    enableKtlint = false,
                    enableDetekt = false,
                    enableGit = true
                )

            // When
            val result = classUnderTest.getNewFeatures(givenArguments)

            // Then
            assertEquals(listOf(expectedRequest), result)
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
            assertTrue(result[0].enableCompose)
            assertTrue(result[0].enableKtlint)
            assertFalse(result[0].enableDetekt)
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

        @Test
        fun `Given --new-architecture --git when getNewArchitecture then enableGit is true`() {
            // Given
            val givenArguments = arrayOf("--new-architecture", "--git")

            // When
            val result = classUnderTest.getNewArchitecture(givenArguments)

            // Then
            assertEquals(1, result.size)
            assertTrue(result[0].enableGit)
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
                assertEquals(
                    "Cannot mix long form (--new-datasource) with short form secondary flags (-n). Use --name instead.",
                    exception.message
                )
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
                assertEquals(
                    "Cannot mix short form (-nds) with long form secondary flags (--name). Use -n instead.",
                    exception.message
                )
            }
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given data source with long primary and short secondary and missing name when getNewDataSources then throws mixed form exception`() {
            // Given
            val givenArguments = arrayOf("--new-datasource", "-n=Test")

            // When
            try {
                classUnderTest.getNewDataSources(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals(
                    "Cannot mix long form (--new-datasource) with short form secondary flags (-n). Use --name instead.",
                    exception.message
                )
            }
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given data source with short primary and long secondary and missing name when getNewDataSources then throws mixed form exception`() {
            // Given
            val givenArguments = arrayOf("-nds", "--name=Test")

            // When
            try {
                classUnderTest.getNewDataSources(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals(
                    "Cannot mix short form (-nds) with long form secondary flags (--name). Use -n instead.",
                    exception.message
                )
            }
        }

        @Test
        fun `Given --git when getNewDataSources then enableGit is true`() {
            // Given
            val givenArguments = arrayOf("--new-datasource", "--name=My", "--git")
            val expected = DataSourceRequest("MyDataSource", useKtor = false, useRetrofit = false, enableGit = true)

            // When
            val result = classUnderTest.getNewDataSources(givenArguments)

            // Then
            assertEquals(listOf(expected), result)
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
                assertEquals(
                    "Cannot mix long form (--new-use-case) with short form secondary flags (-n). Use --name instead.",
                    exception.message
                )
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
                assertEquals("Cannot mix short form (-nuc) with long form secondary flags (--name). Use -n instead.", exception.message)
            }
        }

        @Test
        fun `Given use case with long primary and short secondary and missing name when getNewUseCases then throws mixed form exception`() {
            // Given
            val givenArguments = arrayOf("--new-use-case", "-n=Test")

            // When
            try {
                classUnderTest.getNewUseCases(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals(
                    "Cannot mix long form (--new-use-case) with short form secondary flags (-n). Use --name instead.",
                    exception.message
                )
            }
        }

        @Test
        fun `Given use case with short primary and long secondary and missing name when getNewUseCases then throws mixed form exception`() {
            // Given
            val givenArguments = arrayOf("-nuc", "--name=Test")

            // When
            try {
                classUnderTest.getNewUseCases(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Cannot mix short form (-nuc) with long form secondary flags (--name). Use -n instead.", exception.message)
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
            val expectedRequest =
                ProjectTemplateRequest(
                    projectName = "MyApp",
                    packageName = "com.example",
                    enableCompose = true,
                    enableKtlint = true,
                    enableDetekt = true,
                    enableKtor = true,
                    enableRetrofit = true
                )

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(expectedRequest), result)
        }

        @Test
        fun `Given project template with short flags when getNewProjectTemplate then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-np", "-n=TestApp", "-p", "com.test", "-nc", "-kl", "-d", "-kt", "-rt")
            val expectedRequest =
                ProjectTemplateRequest(
                    projectName = "TestApp",
                    packageName = "com.test",
                    enableCompose = false,
                    enableKtlint = true,
                    enableDetekt = true,
                    enableKtor = true,
                    enableRetrofit = true
                )

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(expectedRequest), result)
        }

        @Test
        fun `Given project template with only name when getNewProjectTemplate then returns request with defaults`() {
            // Given
            val givenArguments = arrayOf("--new-project", "--name=MinimalApp")
            val expectedRequest =
                ProjectTemplateRequest(
                    projectName = "MinimalApp",
                    packageName = "",
                    enableCompose = true,
                    enableKtlint = false,
                    enableDetekt = false,
                    enableKtor = false,
                    enableRetrofit = false
                )

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(expectedRequest), result)
        }

        @Test
        fun `Given project template with no compose when getNewProjectTemplate then disables compose`() {
            // Given
            val givenArguments = arrayOf("--new-project", "--name=NoComposeApp", "--no-compose")
            val expectedRequest =
                ProjectTemplateRequest(
                    projectName = "NoComposeApp",
                    packageName = "",
                    enableCompose = false,
                    enableKtlint = false,
                    enableDetekt = false,
                    enableKtor = false,
                    enableRetrofit = false
                )

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(expectedRequest), result)
        }

        @Test
        fun `Given multiple project templates when getNewProjectTemplate then returns all requests`() {
            // Given
            val givenArguments = arrayOf("--new-project", "--name=First", "--package=com.first", "--new-project", "--name=Second")
            val expectedRequest1 =
                ProjectTemplateRequest(
                    projectName = "First",
                    packageName = "com.first",
                    enableCompose = true,
                    enableKtlint = false,
                    enableDetekt = false,
                    enableKtor = false,
                    enableRetrofit = false
                )
            val expectedRequest2 =
                ProjectTemplateRequest(
                    projectName = "Second",
                    packageName = "",
                    enableCompose = true,
                    enableKtlint = false,
                    enableDetekt = false,
                    enableKtor = false,
                    enableRetrofit = false
                )

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(expectedRequest1, expectedRequest2), result)
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
                assertEquals(
                    "Cannot mix long form (--new-project) with short form secondary flags (-n). Use --name instead.",
                    exception.message
                )
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
                assertEquals("Cannot mix short form (-np) with long form secondary flags (--name). Use -n instead.", exception.message)
            }
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given project template with long primary and short secondary and missing name when getNewProjectTemplate then throws mixed form exception`() {
            // Given
            val givenArguments = arrayOf("--new-project", "-n=Test")

            // When
            try {
                classUnderTest.getNewProjectTemplate(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals(
                    "Cannot mix long form (--new-project) with short form secondary flags (-n). Use --name instead.",
                    exception.message
                )
            }
        }

        @Test
        @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
        fun `Given project template with short primary and long secondary and missing name when getNewProjectTemplate then throws mixed form exception`() {
            // Given
            val givenArguments = arrayOf("-np", "--name=Test")

            // When
            try {
                classUnderTest.getNewProjectTemplate(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Cannot mix short form (-np) with long form secondary flags (--name). Use -n instead.", exception.message)
            }
        }

        @Test
        fun `Given project template with --git when getNewProjectTemplate then enableGit is true`() {
            // Given
            val givenArguments = arrayOf("--new-project", "--name=GitApp", "--git")
            val expectedRequest =
                ProjectTemplateRequest(
                    projectName = "GitApp",
                    packageName = "",
                    enableCompose = true,
                    enableKtlint = false,
                    enableDetekt = false,
                    enableKtor = false,
                    enableRetrofit = false,
                    enableGit = true
                )

            // When
            val result = classUnderTest.getNewProjectTemplate(givenArguments)

            // Then
            assertEquals(listOf(expectedRequest), result)
        }
    }

    class AppArgumentProcessorViewModelsTest {
        private lateinit var classUnderTest: AppArgumentProcessor

        @Before
        fun setUp() {
            classUnderTest = AppArgumentProcessor()
        }

        @Test
        fun `Given view models with optional paths when getNewViewModels then maps in order`() {
            // Given
            val givenArguments =
                arrayOf("--new-view-model", "--name=First", "--path=/path1", "--new-view-model", "--name=Second")

            // When
            val result = classUnderTest.getNewViewModels(givenArguments)

            // Then
            assertEquals(listOf(ViewModelRequest("First", "/path1"), ViewModelRequest("Second", null)), result)
        }

        @Test
        fun `Given short flags when getNewViewModels then parses correctly`() {
            // Given
            val givenArguments = arrayOf("-nvm", "-nThird", "-p", "/path3", "-nvm", "-n=Fourth", "-p/path4")

            // When
            val result = classUnderTest.getNewViewModels(givenArguments)

            // Then
            assertEquals(listOf(ViewModelRequest("Third", "/path3"), ViewModelRequest("Fourth", "/path4")), result)
        }

        @Test
        fun `Given view model with missing name when getNewViewModels then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-view-model")

            // When
            try {
                classUnderTest.getNewViewModels(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("ViewModel name is required. Use --name=ViewModelName or -n=ViewModelName", exception.message)
            }
        }

        @Test
        fun `Given view model with mixed long and short forms when getNewViewModels then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-view-model", "-n", "TestViewModel")
            val expectedErrorMessage =
                "Cannot mix long form (--new-view-model) with short form secondary flags (-n). Use --name instead."

            // When
            try {
                classUnderTest.getNewViewModels(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals(expectedErrorMessage, exception.message)
            }
        }

        @Test
        fun `Given view model with mixed short and long forms when getNewViewModels then throws exception`() {
            // Given
            val givenArguments = arrayOf("-nvm", "--name", "TestViewModel")

            // When
            try {
                classUnderTest.getNewViewModels(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Cannot mix short form (-nvm) with long form secondary flags (--name). Use -n instead.", exception.message)
            }
        }

        @Test
        fun `Given view model with --git when getNewViewModels then enableGit is true`() {
            // Given
            val givenArguments = arrayOf("--new-view-model", "--name=MyViewModel", "--git")
            val expected = ViewModelRequest("MyViewModel", null, enableGit = true)

            // When
            val result = classUnderTest.getNewViewModels(givenArguments)

            // Then
            assertEquals(listOf(expected), result)
        }
    }

    class UnknownFlagsValidation {
        private lateinit var classUnderTest: AppArgumentProcessor

        @Before
        fun setUp() {
            classUnderTest = AppArgumentProcessor()
        }

        @Test
        fun `Given valid arguments when validateNoUnknownFlags then does not throw exception`() {
            // Given
            val givenArguments = arrayOf("--new-feature", "--name=TestFeature")

            // When & Then
            classUnderTest.validateNoUnknownFlags(givenArguments)
        }

        @Test
        fun `Given help flag when validateNoUnknownFlags then does not throw exception`() {
            // Given
            val givenArguments = arrayOf("--help")

            // When & Then
            classUnderTest.validateNoUnknownFlags(givenArguments)
        }

        @Test
        fun `Given short help flag when validateNoUnknownFlags then does not throw exception`() {
            // Given
            val givenArguments = arrayOf("-h")

            // When & Then
            classUnderTest.validateNoUnknownFlags(givenArguments)
        }

        @Test
        fun `Given unknown flag when validateNoUnknownFlags then throws exception`() {
            // Given
            val givenArguments = arrayOf("--unknown-flag")

            // When
            try {
                classUnderTest.validateNoUnknownFlags(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Unknown flags: --unknown-flag", exception.message)
            }
        }

        @Test
        fun `Given multiple unknown flags when validateNoUnknownFlags then throws exception with all flags`() {
            // Given
            val givenArguments = arrayOf("--unknown-flag1", "--unknown-flag2")

            // When
            try {
                classUnderTest.validateNoUnknownFlags(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Unknown flags: --unknown-flag1, --unknown-flag2", exception.message)
            }
        }

        @Test
        fun `Given valid arguments with unknown flag when validateNoUnknownFlags then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-feature", "--name=TestFeature", "--unknown-flag")

            // When
            try {
                classUnderTest.validateNoUnknownFlags(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Unknown flags: --unknown-flag", exception.message)
            }
        }

        @Test
        fun `Given short unknown flag when validateNoUnknownFlags then throws exception`() {
            // Given
            val givenArguments = arrayOf("-unknown")

            // When
            try {
                classUnderTest.validateNoUnknownFlags(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Unknown flags: -unknown", exception.message)
            }
        }

        @Test
        fun `Given mixed valid and unknown flags when validateNoUnknownFlags then throws exception`() {
            // Given
            val givenArguments = arrayOf("--new-feature", "--name=Test", "--unknown", "value")

            // When
            try {
                classUnderTest.validateNoUnknownFlags(givenArguments)
                fail("Expected IllegalArgumentException to be thrown")
            } catch (exception: IllegalArgumentException) {
                // Then
                assertEquals("Unknown flags: --unknown", exception.message)
            }
        }

        @Test
        fun `Given empty arguments when validateNoUnknownFlags then does not throw exception`() {
            // Given
            val givenArguments = emptyArray<String>()

            // When & Then
            classUnderTest.validateNoUnknownFlags(givenArguments)
        }

        @Test
        fun `Given arguments without flags when validateNoUnknownFlags then does not throw exception`() {
            // Given
            val givenArguments = arrayOf("value1", "value2")

            // When & Then
            classUnderTest.validateNoUnknownFlags(givenArguments)
        }
    }
}
