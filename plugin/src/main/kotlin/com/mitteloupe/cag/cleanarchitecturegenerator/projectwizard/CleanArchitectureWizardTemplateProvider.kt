package com.mitteloupe.cag.cleanarchitecturegenerator.projectwizard

import com.android.tools.idea.templates.recipe.FindReferencesRecipeExecutor
import com.android.tools.idea.wizard.template.BooleanParameter
import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.CheckBoxWidget
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.Template
import com.android.tools.idea.wizard.template.TemplateConstraint
import com.android.tools.idea.wizard.template.TemplateData
import com.android.tools.idea.wizard.template.WizardTemplateProvider
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.booleanParameter
import com.android.tools.idea.wizard.template.impl.activities.common.MIN_API
import com.android.tools.idea.wizard.template.template
import com.intellij.openapi.application.ApplicationManager
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle
import com.mitteloupe.cag.cleanarchitecturegenerator.IdeBridge
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.GeneratorProvider
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.request.GenerateProjectTemplateRequest
import java.io.File
import java.lang.reflect.Field
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CleanArchitectureWizardTemplateProvider : WizardTemplateProvider() {
    private val ideBridge = IdeBridge()
    private val generatorProvider = GeneratorProvider()

    private val processedRequests = ConcurrentHashMap<String, Boolean>()

    private val executionId = UUID.randomUUID().toString()

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

            val enableKtor =
                booleanParameter {
                    name = CleanArchitectureGeneratorBundle.message("wizard.parameter.ktor.name")
                    default = false
                    help = CleanArchitectureGeneratorBundle.message("wizard.parameter.ktor.help")
                }

            val enableRetrofit =
                booleanParameter {
                    name = CleanArchitectureGeneratorBundle.message("wizard.parameter.retrofit.name")
                    default = false
                    help = CleanArchitectureGeneratorBundle.message("wizard.parameter.retrofit.help")
                }

            widgets(
                CheckBoxWidget(enableKtlint),
                CheckBoxWidget(enableDetekt),
                CheckBoxWidget(enableCompose),
                CheckBoxWidget(enableKtor),
                CheckBoxWidget(enableRetrofit)
            )

            thumb {
                File("viewmodel-activity").resolve("template_blank_activity.png")
            }

            recipe = { data: TemplateData ->
                val moduleData = (data as ModuleTemplateData)

                if (processedRequests.putIfAbsent(executionId, true) == null) {
                    try {
                        val projectRootDirectory = moduleData.rootDir.parentFile
                        createProject(
                            projectRootDirectory,
                            data,
                            enableCompose,
                            enableKtlint,
                            enableDetekt,
                            enableKtor,
                            enableRetrofit
                        )
                        ideBridge.refreshIde(projectRootDirectory)
                    } catch (exception: GenerationException) {
                        throw RuntimeException(
                            CleanArchitectureGeneratorBundle.message(
                                "wizard.error.generation.failed",
                                exception.message.orEmpty()
                            ),
                            exception
                        )
                    }
                }
            }
        }

    private fun RecipeExecutor.createProject(
        projectRootDirectory: File,
        data: ModuleTemplateData,
        enableCompose: BooleanParameter,
        enableKtlint: BooleanParameter,
        enableDetekt: BooleanParameter,
        enableKtor: BooleanParameter,
        enableRetrofit: BooleanParameter
    ) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val request =
                GenerateProjectTemplateRequest(
                    requestId = executionId,
                    destinationRootDirectory = projectRootDirectory,
                    projectName = readProjectName(projectRootDirectory.name),
                    packageName = data.packageName,
                    enableCompose = enableCompose.value,
                    enableKtlint = enableKtlint.value,
                    enableDetekt = enableDetekt.value,
                    enableKtor = enableKtor.value,
                    enableRetrofit = enableRetrofit.value
                )

            generatorProvider.prepare(project = null).generate().generateProjectTemplate(request)
        }
    }

    private fun RecipeExecutor.readProjectName(fallbackProjectName: String): String {
        val projectNameFromContext =
            try {
                val findReferencesExecutor = this as? FindReferencesRecipeExecutor
                if (findReferencesExecutor != null) {
                    val contextField: Field = FindReferencesRecipeExecutor::class.java.getDeclaredField("context")
                    contextField.isAccessible = true
                    val context = contextField.get(findReferencesExecutor)
                    val projectField = context?.javaClass?.getDeclaredField("project")
                    projectField?.isAccessible = true
                    val project = projectField?.get(context)
                    val nameField = project?.javaClass?.getDeclaredField("name")
                    nameField?.isAccessible = true
                    nameField?.get(project) as? String
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

        return projectNameFromContext?.takeIf { it.isNotEmpty() }
            ?: fallbackProjectName
    }
}
