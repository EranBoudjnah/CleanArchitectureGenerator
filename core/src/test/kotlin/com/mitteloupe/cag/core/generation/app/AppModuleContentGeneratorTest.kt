package com.mitteloupe.cag.core.generation.app

import com.mitteloupe.cag.core.DirectoryFinder
import com.mitteloupe.cag.core.fake.FakeFileSystemBridge
import com.mitteloupe.cag.core.generation.filesystem.FileCreator
import com.mitteloupe.cag.core.option.DependencyInjection
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class AppModuleContentGeneratorTest {
    private lateinit var classUnderTest: AppModuleContentGenerator

    @Before
    fun setUp() {
        val fileCreator = FileCreator(FakeFileSystemBridge())
        val directoryFinder = DirectoryFinder()
        classUnderTest = AppModuleContentGenerator(fileCreator, directoryFinder)
    }

    @Test
    fun `Given no compose when writeAppModule then generates MainActivity file`() {
        val startDirectory = createTempDirectory(prefix = "appModule").toFile()
        val appName = "TestApp"
        val namespace = "com.company.testapp"
        val enableCompose = false
        val expectedContent = """package com.company.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.company.testapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
"""

        // When
        classUnderTest.writeAppModule(
            startDirectory = startDirectory,
            appName = appName,
            projectNamespace = namespace,
            dependencyInjection = DependencyInjection.Hilt,
            enableCompose = enableCompose
        )
        val mainActivityFile = File(startDirectory, "app/src/main/java/com/company/testapp/MainActivity.kt")

        // Then
        assertEquals(expectedContent, mainActivityFile.readText())
    }

    @Test
    fun `Given compose when writeAppModule then generates MainActivity file`() {
        val startDirectory = createTempDirectory(prefix = "appModule").toFile()
        val appName = "TestApp"
        val namespace = "com.company.testapp"
        val enableCompose = true
        val expectedContent = $$"""package $$namespace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.company.testapp.ui.theme.TestAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestAppTheme {
        Greeting("Android")
    }
}
"""

        // When
        classUnderTest.writeAppModule(
            startDirectory = startDirectory,
            appName = appName,
            projectNamespace = namespace,
            dependencyInjection = DependencyInjection.Hilt,
            enableCompose = enableCompose
        )
        val mainActivityFile = File(startDirectory, "app/src/main/java/com/company/testapp/MainActivity.kt")

        // Then
        assertEquals(expectedContent, mainActivityFile.readText())
    }

    @Test
    fun `Given no compose when writeAppModule then generates HappinessTrackerApplication file`() {
        val startDirectory = createTempDirectory(prefix = "appModule").toFile()
        val appName = "HappinessTracker"
        val namespace = "com.happycorp.happinesstracker"
        val enableCompose = false
        val expectedContent = """package $namespace

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HappinessTrackerApplication : Application()
"""

        // When
        classUnderTest.writeAppModule(
            startDirectory = startDirectory,
            appName = appName,
            projectNamespace = namespace,
            dependencyInjection = DependencyInjection.Hilt,
            enableCompose = enableCompose
        )
        val mainActivityFile =
            File(startDirectory, "app/src/main/java/com/happycorp/happinesstracker/HappinessTrackerApplication.kt")

        // Then
        assertEquals(expectedContent, mainActivityFile.readText())
    }

    @Test
    fun `Given Koin when writeAppModule then generates HappinessTrackerApplication file with Koin`() {
        val startDirectory = createTempDirectory(prefix = "appModule").toFile()
        val appName = "HappinessTracker"
        val namespace = "com.happycorp.happinesstracker"
        val dependencyInjection = DependencyInjection.Koin
        val enableCompose = false
        val expectedContent = """
package com.happycorp.happinesstracker

import android.app.Application
import com.happycorp.happinesstracker.di.architectureModule
import com.happycorp.happinesstracker.di.sampleFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

class HappinessTrackerApplication : Application() {
    private fun initKoin(config : KoinAppDeclaration? = null){
        startKoin {
            includes(config)
            modules(architectureModule, sampleFeatureModule)
        }
    }

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@HappinessTrackerApplication)
            androidLogger()
        }
    }
}
"""

        // When
        classUnderTest.writeAppModule(
            startDirectory = startDirectory,
            appName = appName,
            projectNamespace = namespace,
            dependencyInjection = dependencyInjection,
            enableCompose = enableCompose
        )
        val mainActivityFile =
            File(startDirectory, "app/src/main/java/com/happycorp/happinesstracker/HappinessTrackerApplication.kt")

        // Then
        assertEquals(expectedContent, mainActivityFile.readText())
    }
}
