package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.optimizeImports

fun buildMainActivityKotlinFile(
    appName: String,
    projectNamespace: String,
    enableCompose: Boolean
): String {
    val result =
        if (enableCompose) {
            val optimizedImports =
                """
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
import $projectNamespace.ui.theme.${appName}Theme
""".optimizeImports()
            """
            package $projectNamespace

            $optimizedImports
            class MainActivity : ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContent {
                        ${appName}Theme {
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
                    text = "Hello ${'$'}name!",
                    modifier = modifier
                )
            }

            @Preview(showBackground = true)
            @Composable
            fun GreetingPreview() {
                ${appName}Theme {
                    Greeting("Android")
                }
            }
            """.trimIndent()
        } else {
            val optimizedImports =
                """
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import $projectNamespace.databinding.ActivityMainBinding
""".optimizeImports()
            """
            package $projectNamespace

            $optimizedImports

            class MainActivity : AppCompatActivity() {
                private lateinit var binding: ActivityMainBinding

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    binding = ActivityMainBinding.inflate(layoutInflater)
                    setContentView(binding.root)
                }
            }
            """.trimIndent()
        }

    return result
}
