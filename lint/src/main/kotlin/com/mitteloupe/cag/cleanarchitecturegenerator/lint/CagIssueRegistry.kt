package com.mitteloupe.cag.cleanarchitecturegenerator.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class CagIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(ViewModelPublicFunctionShouldStartWithOnDetector.ISSUE)

    override val api: Int = CURRENT_API
}
