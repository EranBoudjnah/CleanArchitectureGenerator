package com.mitteloupe.cag.cleanarchitecturegenerator.projectwizard

import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.CheckBoxWidget
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.Template
import com.android.tools.idea.wizard.template.TemplateConstraint
import com.android.tools.idea.wizard.template.TemplateData
import com.android.tools.idea.wizard.template.WizardTemplateProvider
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.booleanParameter
import com.android.tools.idea.wizard.template.impl.activities.common.MIN_API
import com.android.tools.idea.wizard.template.impl.defaultPackageNameParameter
import com.android.tools.idea.wizard.template.template
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle
import com.mitteloupe.cag.core.GenerateProjectTemplateRequest
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.Generator
import java.io.File

class CleanArchitectureWizardTemplateProvider : WizardTemplateProvider() {
    override fun getTemplates(): List<Template> = listOf(cleanArchitectureTemplate)

    private val cleanArchitectureTemplate =
        template {
            name = CleanArchitectureGeneratorBundle.message("wizard.template.name")
            description = CleanArchitectureGeneratorBundle.message("wizard.template.description")
            category = Category.Application
            formFactor = FormFactor.Mobile
            minApi = MIN_API
            constraints = listOf(TemplateConstraint.AndroidX, TemplateConstraint.Kotlin)
            screens = listOf(WizardUiContext.NewProject, WizardUiContext.NewProjectExtraDetail)

            val packageName = defaultPackageNameParameter

            val enableKtlint =
                booleanParameter {
                    name = CleanArchitectureGeneratorBundle.message("wizard.parameter.ktlint.name")
                    default = false
                    help = CleanArchitectureGeneratorBundle.message("wizard.parameter.ktlint.help")
                }

            val enableDetekt =
                booleanParameter {
                    name = CleanArchitectureGeneratorBundle.message("wizard.parameter.detekt.name")
                    default = false
                    help = CleanArchitectureGeneratorBundle.message("wizard.parameter.detekt.help")
                }

            val enableCompose =
                booleanParameter {
                    name = CleanArchitectureGeneratorBundle.message("wizard.parameter.compose.name")
                    default = true
                    help = CleanArchitectureGeneratorBundle.message("wizard.parameter.compose.help")
                }

            widgets(
                CheckBoxWidget(enableKtlint),
                CheckBoxWidget(enableDetekt),
                CheckBoxWidget(enableCompose)
            )

            thumb {
                File("empty-activity").resolve("template_empty_activity.png")
            }

            recipe = { data: TemplateData ->
                try {
                    val request =
                        GenerateProjectTemplateRequest(
                            destinationRootDirectory = File("."),
                            projectName = (data as ModuleTemplateData).rootDir.name,
                            packageName = packageName.value,
                            enableCompose = enableCompose.value,
                            enableKtlint = enableKtlint.value,
                            enableDetekt = enableDetekt.value,
                            enableKtor = false,
                            enableRetrofit = false
                        )

                    Generator().generateProjectTemplate(request)
                } catch (exception: GenerationException) {
                    throw RuntimeException(
                        CleanArchitectureGeneratorBundle.message("wizard.error.generation.failed", exception.message ?: ""),
                        exception
                    )
                }
            }
        }
}
