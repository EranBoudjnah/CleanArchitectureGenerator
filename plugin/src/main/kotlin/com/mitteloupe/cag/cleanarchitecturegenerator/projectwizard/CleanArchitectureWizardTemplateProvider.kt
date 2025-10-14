package com.mitteloupe.cag.cleanarchitecturegenerator.projectwizard

import com.android.tools.idea.templates.recipe.FindReferencesRecipeExecutor
import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.CheckBoxWidget
import com.android.tools.idea.wizard.template.EnumWidget
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.Template
import com.android.tools.idea.wizard.template.TemplateConstraint
import com.android.tools.idea.wizard.template.TemplateData
import com.android.tools.idea.wizard.template.WizardTemplateProvider
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.booleanParameter
import com.android.tools.idea.wizard.template.enumParameter
import com.android.tools.idea.wizard.template.impl.activities.common.MIN_API
import com.android.tools.idea.wizard.template.template
import com.intellij.openapi.application.ApplicationInfo
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle
import com.mitteloupe.cag.cleanarchitecturegenerator.IdeBridge
import com.mitteloupe.cag.cleanarchitecturegenerator.filesystem.GeneratorProvider
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.AppSettingsService
import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.option.DependencyInjection
import com.mitteloupe.cag.core.request.GenerateProjectTemplateRequest
import com.mitteloupe.cag.git.Git
import java.io.File
import com.mitteloupe.cag.cleanarchitecturegenerator.model.DependencyInjection as WizardDependencyInjection

private val MEERKAT_PREFIX = "^(?:.* )?2024\\.3\\..*$".toRegex()

class CleanArchitectureWizardTemplateProvider : WizardTemplateProvider() {
    private val ideBridge = IdeBridge()
    private val generatorProvider = GeneratorProvider()
    private val git = Git(gitBinaryPath = AppSettingsService.getInstance().gitPath)

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

            val dependencyInjectionOption =
                enumParameter<WizardDependencyInjection> {
                    name = "Dependency Injection"
                    default = WizardDependencyInjection.Hilt
                    help = "Select the dependency injection library to use in the generated project"
                }

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

            val initializeGitRepository =
                booleanParameter {
                    name = CleanArchitectureGeneratorBundle.message("wizard.parameter.git.init.name")
                    default = false
                    help = CleanArchitectureGeneratorBundle.message("wizard.parameter.git.init.help")
                }

            widgets(
                EnumWidget(dependencyInjectionOption),
                CheckBoxWidget(enableKtlint),
                CheckBoxWidget(enableDetekt),
                CheckBoxWidget(enableCompose),
                CheckBoxWidget(enableKtor),
                CheckBoxWidget(enableRetrofit),
                CheckBoxWidget(initializeGitRepository)
            )

            thumb {
                File("viewmodel-activity").resolve("template_blank_activity.png")
            }

            recipe = { data: TemplateData ->
                if (this !is FindReferencesRecipeExecutor) {
                    val moduleData = (data as ModuleTemplateData)

                    try {
                        val projectRootDirectory = moduleData.rootDir.parentFile
                        createProject(
                            projectRootDirectory = projectRootDirectory,
                            data = moduleData,
                            dependencyInjection = dependencyInjectionOption.value.coreValue,
                            enableCompose = enableCompose.value,
                            enableKtlint = enableKtlint.value,
                            enableDetekt = enableDetekt.value,
                            enableKtor = enableKtor.value,
                            enableRetrofit = enableRetrofit.value,
                            initializeGitRepository = initializeGitRepository.value
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
        dependencyInjection: DependencyInjection,
        enableCompose: Boolean,
        enableKtlint: Boolean,
        enableDetekt: Boolean,
        enableKtor: Boolean,
        enableRetrofit: Boolean,
        initializeGitRepository: Boolean
    ) {
        val selectedMinSdk: Int? =
            try {
                val apis = data.apis
                val getMinApi = apis.javaClass.methods.first { it.name == "getMinApi" }
                val minApi = getMinApi(apis)
                minApi.apiLevelCompat
            } catch (_: Exception) {
                null
            }

        val applicationInfo = ApplicationInfo.getInstance()
        val overrideAndroidGradlePluginVersion =
            if (applicationInfo.fullVersion.matches(MEERKAT_PREFIX)) {
                "8.9.1"
            } else {
                null
            }

        val request =
            GenerateProjectTemplateRequest(
                destinationRootDirectory = projectRootDirectory,
                projectName = readProjectName(projectRootDirectory) ?: projectRootDirectory.name,
                packageName = data.packageName,
                overrideMinimumAndroidSdk = selectedMinSdk,
                overrideAndroidGradlePluginVersion = overrideAndroidGradlePluginVersion,
                dependencyInjection = dependencyInjection,
                enableCompose = enableCompose,
                enableKtlint = enableKtlint,
                enableDetekt = enableDetekt,
                enableKtor = enableKtor,
                enableRetrofit = enableRetrofit
            )

        generatorProvider.prepare(project = null).generate().generateProjectTemplate(request)

        val initializeGitRepository = initializeGitRepository
        if (initializeGitRepository) {
            git.initializeRepository(projectRootDirectory)
        }

        if (AppSettingsService.getInstance().autoAddGeneratedFilesToGit) {
            val gitDirectory = File(projectRootDirectory, ".git")
            if (gitDirectory.exists()) {
                runCatching { git.stageAll(projectRootDirectory) }
            }
        }
    }

    private fun readProjectName(rootDir: File): String? {
        val gradleFile =
            File(rootDir, "settings.gradle.kts").takeIf { it.exists() }
                ?: File(rootDir, "settings.gradle").takeIf { it.exists() }

        if (gradleFile?.exists() == true) {
            val gradleContents = gradleFile.readText()
            val projectNameRegex = Regex("""rootProject\.name\s*=\s*["'](.+?)["']""")
            val match = projectNameRegex.find(gradleContents)
            return match?.groups?.get(1)?.value
        }

        return null
    }

    private val Any.apiLevelCompat: Int
        get() =
            runCatching {
                val field = this::class.java.getDeclaredField("apiLevel")
                field.isAccessible = true
                field.getInt(this)
            }.getOrElse {
                val fallback = this::class.java.getDeclaredField("api")
                fallback.isAccessible = true
                fallback.getInt(this)
            }
}
