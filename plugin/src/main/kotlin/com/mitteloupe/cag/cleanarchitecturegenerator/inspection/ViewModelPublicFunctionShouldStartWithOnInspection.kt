package com.mitteloupe.cag.cleanarchitecturegenerator.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.mitteloupe.cag.cleanarchitecturegenerator.rule.ViewModelPublicFunctionShouldStartWithOnRule
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.isPublic

class ViewModelPublicFunctionShouldStartWithOnInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor =
        object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                val functionName = function.name.orEmpty()
                val isValid =
                    ViewModelPublicFunctionShouldStartWithOnRule.isValid(
                        className = function.containingClassOrObject?.name,
                        functionName = functionName,
                        isPublic = function.isPublic
                    )
                if (!isValid) {
                    holder.registerProblem(
                        function.nameIdentifier ?: function,
                        ViewModelPublicFunctionShouldStartWithOnRule.violationMessage(functionName)
                    )
                }
            }
        }
}
