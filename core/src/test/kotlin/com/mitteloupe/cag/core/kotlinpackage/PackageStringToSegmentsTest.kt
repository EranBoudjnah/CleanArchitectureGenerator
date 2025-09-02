package com.mitteloupe.cag.core.kotlinpackage

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class PackageStringToSegmentsTest(
    private val packageName: String,
    private val expectedSegments: List<String>
) {
    companion object {
        @JvmStatic
        @Parameters(name = "Given {0} then returns {1}")
        fun testCases(): Collection<Array<Any>> =
            listOf(
                testCase(
                    packageName = "com.example.myapp.feature",
                    expectedSegments = listOf("com", "example", "myapp", "feature")
                ),
                testCase(
                    packageName = "com",
                    expectedSegments = listOf("com")
                ),
                testCase(
                    packageName = "",
                    expectedSegments = emptyList()
                ),
                testCase(
                    packageName = "com..example..myapp",
                    expectedSegments = listOf("com", "example", "myapp")
                ),
                testCase(
                    packageName = "...com.example",
                    expectedSegments = listOf("com", "example")
                ),
                testCase(
                    packageName = "com.example...",
                    expectedSegments = listOf("com", "example")
                ),
                testCase(
                    packageName = "...",
                    expectedSegments = emptyList()
                ),
                testCase(
                    packageName = "com. example .myapp",
                    expectedSegments = listOf("com", " example ", "myapp")
                ),
                testCase(
                    packageName = "com.example_myapp.feature-test",
                    expectedSegments = listOf("com", "example_myapp", "feature-test")
                ),
                testCase(
                    packageName = "com.example123.myapp456",
                    expectedSegments = listOf("com", "example123", "myapp456")
                )
            )

        private fun testCase(
            packageName: String,
            expectedSegments: List<String>
        ): Array<Any> = arrayOf(packageName, expectedSegments)
    }

    @Test
    fun `When toSegments`() {
        // When
        val result = packageName.toSegments()

        // Then
        assertEquals(expectedSegments, result)
    }
}
