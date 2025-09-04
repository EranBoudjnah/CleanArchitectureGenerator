package com.mitteloupe.cag.core.content

fun buildMainActivityKotlinFile(
    projectNamespace: String,
    enableCompose: Boolean
): String {
    val packageName = projectNamespace.trimEnd('.')
    val themeName = packageName.split('.').last().capitalized

    return if (enableCompose) {
        """
        package $packageName

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
        import $packageName.ui.theme.${themeName}Theme

        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                    ${themeName}Theme {
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
            ${themeName}Theme {
                Greeting("Android")
            }
        }
        """.trimIndent()
    } else {
        """
        package $packageName

        import android.os.Bundle
        import androidx.appcompat.app.AppCompatActivity
        import $packageName.databinding.ActivityMainBinding

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
}
