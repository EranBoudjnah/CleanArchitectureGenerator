package com.mitteloupe.cag.cleanarchitecturegenerator.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.mitteloupe.cag.cleanarchitecturegenerator.rule.ViewModelFunctionShouldNotContainUiTermsRule
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

class ViewModelFunctionShouldNotContainUiTermsInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor =
        object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                val functionName = function.name.orEmpty()
                val result =
                    ViewModelFunctionShouldNotContainUiTermsRule.validate(
                        className = function.containingClassOrObject?.name,
                        functionName = functionName
                    )

                if (result is ViewModelFunctionShouldNotContainUiTermsRule.Result.Invalid) {
                    holder.registerProblem(
                        function.nameIdentifier ?: function,
                        ViewModelFunctionShouldNotContainUiTermsRule.violationMessage(result.offendingValue),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        ReplaceUiTermWithActionQuickFix(result)
                    )
                }
            }
        }

    private class ReplaceUiTermWithActionQuickFix(
        private val result: ViewModelFunctionShouldNotContainUiTermsRule.Result.Invalid
    ) : LocalQuickFix {
        override fun getFamilyName() = "Replace '${result.offendingValue}' with 'Action'"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor
        ) {
            val function = descriptor.psiElement.parent as? KtNamedFunction ?: return
            val identifier = function.nameIdentifier ?: return
            val newName = result.fixFunctionName()
            identifier.replace(KtPsiFactory(project).createIdentifier(newName))
        }
    }
}
