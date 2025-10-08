package com.mitteloupe.cag.cleanarchitecturegenerator.rule

import com.mitteloupe.cag.cleanarchitecturegenerator.rule.ViewModelPublicFunctionShouldStartWithOnRuleTest.InvalidCases
import com.mitteloupe.cag.cleanarchitecturegenerator.rule.ViewModelPublicFunctionShouldStartWithOnRuleTest.ValidCases
import org.junit.Assert
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Suite

@RunWith(Enclosed::class)
@Suite.SuiteClasses(
    ValidCases::class,
    InvalidCases::class
)
class ViewModelPublicFunctionShouldStartWithOnRuleTest {
    @RunWith(Parameterized::class)
    class ValidCases(
        private val className: String?,
        private val functionName: String,
        private val isPublic: Boolean
    ) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: Given {0} with function named {1}, isPublic={2} then is valid")
            fun testCases() =
                listOf(
                    testCase(className = "MainViewModel", functionName = "onClick", isPublic = true),
                    testCase(className = "MainViewModel", functionName = "on1Start", isPublic = true),
                    testCase(className = "MainViewModel", functionName = "onStart", isPublic = false),
                    testCase(className = "BaseController", functionName = "click", isPublic = true),
                    testCase(className = null, functionName = "click", isPublic = true)
                )

            private fun testCase(
                className: String?,
                functionName: String,
                isPublic: Boolean
            ) = arrayOf<Any?>(className, functionName, isPublic)
        }

        @Test
        fun `When isValid`() {
            // When
            val actualIsValid =
                ViewModelPublicFunctionShouldStartWithOnRule.isValid(
                    className = className,
                    functionName = functionName,
                    isPublic = isPublic
                )

            // Then
            Assert.assertTrue(actualIsValid)
        }
    }

    @RunWith(Parameterized::class)
    class InvalidCases(
        private val className: String?,
        private val functionName: String,
        private val expectedMessage: String
    ) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: Given {0} with public function named {1} then is invalid ({2})")
            fun testCases() =
                listOf(
                    testCase(className = "MainViewModel", functionName = "click"),
                    testCase(className = "TestViewModel", functionName = "on"),
                    testCase(className = "AnotherViewModel", functionName = "OnStart")
                )

            private fun testCase(
                className: String?,
                functionName: String
            ) = arrayOf<Any?>(className, functionName, "ViewModel public function name '$functionName' should start with 'on'")
        }

        @Test
        fun `When isValid`() {
            // When
            val actualIsValid =
                ViewModelPublicFunctionShouldStartWithOnRule.isValid(
                    className = className,
                    functionName = functionName,
                    isPublic = true
                )

            // Then
            Assert.assertFalse(actualIsValid)
        }

        @Test
        fun `When violationMessage`() {
            // When
            val message = ViewModelPublicFunctionShouldStartWithOnRule.violationMessage(functionName)

            // Then
            Assert.assertEquals(expectedMessage, message)
        }
    }
}
