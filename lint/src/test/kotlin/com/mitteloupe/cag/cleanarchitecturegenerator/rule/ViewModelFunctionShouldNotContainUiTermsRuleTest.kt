package com.mitteloupe.cag.cleanarchitecturegenerator.rule

import com.mitteloupe.cag.cleanarchitecturegenerator.rule.ViewModelFunctionShouldNotContainUiTermsRuleTest.Fixes
import com.mitteloupe.cag.cleanarchitecturegenerator.rule.ViewModelFunctionShouldNotContainUiTermsRuleTest.InvalidCases
import com.mitteloupe.cag.cleanarchitecturegenerator.rule.ViewModelFunctionShouldNotContainUiTermsRuleTest.ValidCases
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.junit.runners.Suite.SuiteClasses

@RunWith(Enclosed::class)
@SuiteClasses(
    ValidCases::class,
    InvalidCases::class,
    Fixes::class
)
class ViewModelFunctionShouldNotContainUiTermsRuleTest {
    @RunWith(Parameterized::class)
    class ValidCases(
        private val className: String?,
        private val functionName: String
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "{index}: Given class {0} with function {1} then is valid")
            fun testCases() = listOf(
                testCase("MainViewModel", "onStart"),
                testCase("MainViewModel", "loadData"),
                testCase("MainViewModel", "submitForm"),
                testCase("MainController", "onClick"),
                testCase(null, "clicked"),
                testCase("SomeOtherClass", "onSwipe")
            )

            private fun testCase(className: String?, functionName: String) =
                arrayOf<Any?>(className, functionName)
        }

        @Test
        fun `When validate then is Valid`() {
            // When
            val result = ViewModelFunctionShouldNotContainUiTermsRule.validate(className, functionName)

            // Then
            Assert.assertTrue(result is ViewModelFunctionShouldNotContainUiTermsRule.Result.Valid)
        }
    }

    @RunWith(Parameterized::class)
    class InvalidCases(
        private val className: String,
        private val functionName: String,
        private val expectedUiTerm: String
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "{index}: Given class {0} with function {1} then is invalid (UI term: {2})")
            fun testCases() = listOf(
                testCase("MainViewModel", "click", "click"),
                testCase("MainViewModel", "onScrolled", "Scrolled"),
                testCase("LoginViewModel", "onTap", "Tap"),
                testCase("HomeViewModel", "handleSwipe", "Swipe"),
                testCase("ProfileViewModel", "longPressedExit", "longPressed"),
                testCase("SettingsViewModel", "onTouchEvent", "Touch")
            )

            private fun testCase(className: String, functionName: String, offendingValue: String) =
                arrayOf<Any>(className, functionName, offendingValue)
        }

        @Test
        fun `When validate then is Invalid`() {
            // Given
            val expectedResult = ViewModelFunctionShouldNotContainUiTermsRule.Result.Invalid(functionName, expectedUiTerm)

            // When
            val result = ViewModelFunctionShouldNotContainUiTermsRule.validate(className, functionName)

            // Then
            assertEquals(expectedResult, result)
            val invalidResult = result as ViewModelFunctionShouldNotContainUiTermsRule.Result.Invalid
            assertEquals(expectedUiTerm, invalidResult.offendingValue)
        }

        @Test
        fun `When violationMessage then returns expected message`() {
            // Given
            val expectedMessage = "Function name contains UI term '$expectedUiTerm', consider replacing it with 'Action'"

            // When
            val actualMessage = ViewModelFunctionShouldNotContainUiTermsRule.violationMessage(expectedUiTerm)

            // Then
            assertEquals(expectedMessage, actualMessage)
        }
    }

    @RunWith(Parameterized::class)
    class Fixes(
        private val className: String,
        private val functionName: String,
        private val expectedFixedName: String
    ) {
        companion object {
            @JvmStatic
            @Parameters(name = "{index}: Given class {0} with function {1} then is renamed to {2}")
            fun testCases() = listOf(
                testCase("MainViewModel", "click", "action"),
                testCase("MainViewModel", "onScrolled", "onAction"),
                testCase("LoginViewModel", "onTap", "onAction"),
                testCase("HomeViewModel", "handleSwipe", "handleAction"),
                testCase("ProfileViewModel", "longPressedExit", "actionExit"),
                testCase("SettingsViewModel", "onTouchEvent", "onActionEvent")
            )

            private fun testCase(className: String, functionName: String, expectedFixedName: String) =
                arrayOf<Any>(className, functionName, expectedFixedName)
        }

        @Test
        fun `When fixFunctionName then is Invalid`() {
            // Given
            val result =
                ViewModelFunctionShouldNotContainUiTermsRule.validate(
                    className,
                    functionName
                ) as ViewModelFunctionShouldNotContainUiTermsRule.Result.Invalid

            // When
            val actualFixedName = result.fixFunctionName()

            // Then
            assertEquals(expectedFixedName, actualFixedName)
        }

        @Test
        fun `When violationMessage then returns expected message`() {
            // Given
            val expectedMessage = "Function name contains UI term '$expectedFixedName', consider replacing it with 'Action'"

            // When
            val actualMessage = ViewModelFunctionShouldNotContainUiTermsRule.violationMessage(expectedFixedName)

            // Then
            assertEquals(expectedMessage, actualMessage)
        }
    }
}
