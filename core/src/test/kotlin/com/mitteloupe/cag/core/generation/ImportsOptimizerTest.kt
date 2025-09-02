package com.mitteloupe.cag.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class ImportsOptimizerTest(
    private val testName: String,
    private val input: String,
    private val expected: String
) {
    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun testCases(): Array<Array<Any>> =
            arrayOf(
                testCase(
                    testName = "Given string with imports then returns sorted unique imports",
                    input =
                        """
                        package com.example
                        
                        import java.util.List
                        import java.io.File
                        import java.util.List
                        import kotlin.String
                        
                        class TestClass
                        """.trimIndent(),
                    expected =
                        """
                        import java.io.File
                        import java.util.List
                        import kotlin.String
                        
                        """.trimIndent()
                ),
                testCase(
                    testName = "Given string with no imports then returns newline",
                    input =
                        """
                        package com.example
                        
                        class TestClass {
                            fun test() {}
                        }
                        """.trimIndent(),
                    expected = "\n"
                ),
                testCase(
                    testName = "Given string with duplicate imports then removes duplicates",
                    input =
                        """
                        import java.util.List
                        import java.util.List
                        import java.util.List
                        """.trimIndent(),
                    expected = "import java.util.List\n"
                ),
                testCase(
                    testName = "Given string with unsorted imports then sorts them",
                    input =
                        """
                        import java.util.List
                        import java.io.File
                        import kotlin.String
                        import android.content.Context
                        """.trimIndent(),
                    expected =
                        """
                        import android.content.Context
                        import java.io.File
                        import java.util.List
                        import kotlin.String
                        
                        """.trimIndent()
                ),
                testCase(
                    testName = "Given string with imports and non-import lines then filters only imports",
                    input =
                        """
                        package com.example
                        
                        import java.util.List
                        class TestClass
                        import java.io.File
                        fun test() {}
                        import kotlin.String
                        """.trimIndent(),
                    expected =
                        """
                        import java.io.File
                        import java.util.List
                        import kotlin.String
                        
                        """.trimIndent()
                ),
                testCase(
                    testName = "Given string with whitespace around imports then trims whitespace",
                    input =
                        """
                        import   java.util.List   
                        import    java.io.File
                        import kotlin.String
                        """.trimIndent(),
                    expected =
                        """
                        import java.io.File
                        import java.util.List
                        import kotlin.String
                        
                        """.trimIndent()
                ),
                testCase(
                    testName = "Given empty string when optimizeImports then returns newline",
                    input = "",
                    expected = "\n"
                ),
                testCase(
                    testName = "Given string with only whitespace when optimizeImports then returns newline",
                    input = "   \n  \t  \n  ",
                    expected = "\n"
                )
            )

        private fun testCase(
            testName: String,
            input: String,
            expected: String
        ): Array<Any> = arrayOf(testName, input, expected)
    }

    @Test
    fun `When optimizeImports`() {
        // When
        val result = input.optimizeImports()

        // Then
        assertEquals(testName, expected, result)
    }
}
