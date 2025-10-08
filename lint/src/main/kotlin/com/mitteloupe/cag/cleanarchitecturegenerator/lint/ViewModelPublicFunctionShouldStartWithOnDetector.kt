package com.mitteloupe.cag.cleanarchitecturegenerator.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.mitteloupe.cag.cleanarchitecturegenerator.rule.ViewModelPublicFunctionShouldStartWithOnRule
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastVisibility

class ViewModelPublicFunctionShouldStartWithOnDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes() = listOf(UMethod::class.java)

    override fun createUastHandler(context: JavaContext) =
        object : UElementHandler() {
            override fun visitMethod(node: UMethod) {
                val functionName = node.name
                val isValid = ViewModelPublicFunctionShouldStartWithOnRule.isValid(
                    className = node.containingClass?.name,
                    functionName = functionName,
                    isPublic = node.visibility == UastVisibility.PUBLIC
                )

                if (!isValid) {
                    context.report(
                        ISSUE,
                        node,
                        context.getNameLocation(node),
                        ViewModelPublicFunctionShouldStartWithOnRule.violationMessage(functionName)
                    )
                }
            }
    }

    companion object {
        val ISSUE: Issue = Issue.create(
            id = "ViewModelPublicFunctionShouldStartWithOn",
            briefDescription = "Public ViewModel functions should start with 'on'",
            explanation = """
                Public functions in ViewModel classes should start with 'on' to clearly indicate that they represent user or system events, \
                following Clean Architecture naming conventions for better readability and maintainability.
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                ViewModelPublicFunctionShouldStartWithOnDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
