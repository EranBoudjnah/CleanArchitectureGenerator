package com.mitteloupe.cag.core.generation.format

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.junit.runners.Suite.SuiteClasses

@RunWith(Enclosed::class)
@SuiteClasses(
    PreferredCodeStyleIdentifierTest.PreferredIndentation::class,
    PreferredCodeStyleIdentifierTest.PreferredEndOfLine::class
)
class PreferredCodeStyleIdentifierTest {
    @RunWith(Parameterized::class)
    class PreferredIndentation(
        private val input: String,
        private val expected: String,
        private val description: String
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "{2}")
            fun testCases(): Array<Array<Any>> =
                arrayOf(
                    testCase(
                        input =
                            """
                            class TestClass {
                                fun test() {
                                    val x = 1
                                }
                            }
                            """.trimIndent(),
                        expected = "    ",
                        description = "Given string with spaces indentation when preferredIndentation then returns spaces"
                    ),
                    testCase(
                        input = "class TestClass {\n\tfun test() {\n\t\tval x = 1\n\t}\n}",
                        expected = "\t",
                        description = "Given string with tabs indentation when preferredIndentation then returns tabs"
                    ),
                    testCase(
                        input =
                            """
class TestClass {
    fun test() {
    """ + "\t" + """val x = 1
    }
}""",
                        expected = "    ",
                        description = "Given string with mixed indentation when preferredIndentation then returns first found indentation"
                    ),
                    testCase(
                        input = "class TestClass { fun test() {} }",
                        expected = "    ",
                        description = "Given string with no indentation when preferredIndentation then returns default"
                    ),
                    testCase(
                        input = "\n\n\n",
                        expected = "    ",
                        description = "Given string with only blank lines when preferredIndentation then returns default"
                    ),
                    testCase(
                        input = "",
                        expected = "    ",
                        description = "Given string with empty content when preferredIndentation then returns default"
                    )
                )

            private fun testCase(
                input: String,
                expected: String,
                description: String
            ): Array<Any> = arrayOf(input, expected, description)
        }

        @Test
        fun `When preferredIndentation`() {
            // When
            val result = input.preferredIndentation()

            // Then
            assertEquals(description, expected, result)
        }
    }

    @RunWith(Parameterized::class)
    class PreferredEndOfLine(
        private val input: String,
        private val expected: String,
        private val description: String
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "{2}")
            fun testCases(): Array<Array<Any>> =
                arrayOf(
                    testCase(
                        input = "line1\r\nline2\r\nline3",
                        expected = "\r\n",
                        description = "Given string with CRLF line endings when preferredEndOfLine then returns CRLF"
                    ),
                    testCase(
                        input = "line1\nline2\nline3",
                        expected = "\n",
                        description = "Given string with LF line endings when preferredEndOfLine then returns LF"
                    ),
                    testCase(
                        input = "line1\r\nline2\nline3",
                        expected = "\r\n",
                        description = "Given string with mixed line endings when preferredEndOfLine then returns CRLF"
                    ),
                    testCase(
                        input = "single line",
                        expected = "\n",
                        description = "Given string with no line endings when preferredEndOfLine then returns LF"
                    ),
                    testCase(
                        input = "",
                        expected = "\n",
                        description = "Given empty string when preferredEndOfLine then returns LF"
                    ),
                    testCase(
                        input = "line1\rline2\rline3",
                        expected = "\n",
                        description = "Given string with only CR when preferredEndOfLine then returns LF"
                    )
                )

            private fun testCase(
                input: String,
                expected: String,
                description: String
            ): Array<Any> = arrayOf(input, expected, description)
        }

        @Test
        fun `When preferredEndOfLine`() {
            // When
            val result = input.preferredEndOfLine()

            // Then
            assertEquals(description, expected, result)
        }
    }
}
