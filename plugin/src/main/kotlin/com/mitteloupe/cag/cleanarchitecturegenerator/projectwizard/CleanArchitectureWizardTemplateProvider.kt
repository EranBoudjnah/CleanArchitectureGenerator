package com.mitteloupe.cag.cleanarchitecturegenerator.projectwizard

import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.Constraint
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.PackageNameWidget
import com.android.tools.idea.wizard.template.StringParameter
import com.android.tools.idea.wizard.template.Template
import com.android.tools.idea.wizard.template.TemplateConstraint
import com.android.tools.idea.wizard.template.TemplateData
import com.android.tools.idea.wizard.template.TextFieldWidget
import com.android.tools.idea.wizard.template.WizardTemplateProvider
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.activityToLayout
import com.android.tools.idea.wizard.template.impl.activities.common.MIN_API
import com.android.tools.idea.wizard.template.impl.defaultPackageNameParameter
import com.android.tools.idea.wizard.template.layoutToActivity
import com.android.tools.idea.wizard.template.stringParameter
import com.android.tools.idea.wizard.template.template
import com.mitteloupe.cag.core.GenerateProjectTemplateRequest
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.Generator
import java.io.File

class CleanArchitectureWizardTemplateProvider : WizardTemplateProvider() {
    override fun getTemplates(): List<Template> = listOf(cleanArchitectureTemplate)

    private val cleanArchitectureTemplate =
        template {
            name = "Clean Architecture"
            description = "A complete Clean Architecture Android project with all modules and sample code"
            category = Category.Activity
            formFactor = FormFactor.Mobile
            minApi = MIN_API
            constraints = listOf(TemplateConstraint.AndroidX, TemplateConstraint.Kotlin)
            screens = listOf(WizardUiContext.NewProject, WizardUiContext.NewProjectExtraDetail)

            val appName =
                stringParameter {
                    name = "My Clean Architecture App"
                    default = "MyCleanArchitectureApp"
                }
            val packageName = defaultPackageNameParameter

            lateinit var layoutName: StringParameter
            val activityClass: StringParameter =
                stringParameter {
                    name = "Activity Name"
                    constraints = listOf(Constraint.CLASS, Constraint.UNIQUE, Constraint.NONEMPTY)
                    suggest = {
                        layoutToActivity(layoutName.value)
                    }
                    default = "LauncherActivity"
                    help = "The name of the activity class to create"
                }
            layoutName =
                stringParameter {
                    name = "Layout Name"
                    constraints = listOf(Constraint.LAYOUT, Constraint.UNIQUE, Constraint.NONEMPTY)
                    suggest = {
                        activityToLayout(activityClass.value)
                    }
                    default = "activity_launcher"
                    help = "The name of the UI layout to create for the activity"
                }

            widgets(
                TextFieldWidget(appName),
                PackageNameWidget(packageName)
            )

            thumb {
                File("empty-activity").resolve("template_empty_activity.png")
            }

            recipe = { data: TemplateData ->
                try {
                    val request =
                        GenerateProjectTemplateRequest(
                            destinationRootDirectory = File("."),
                            projectName = appName.value,
                            packageName = packageName.value,
                            enableCompose = true,
                            enableKtlint = false,
                            enableDetekt = false,
                            enableKtor = false,
                            enableRetrofit = false
                        )

                    Generator().generateProjectTemplate(request)
                } catch (exception: GenerationException) {
                    throw RuntimeException("Failed to generate Clean Architecture project: ${exception.message}", exception)
                }
            }
        }
}
