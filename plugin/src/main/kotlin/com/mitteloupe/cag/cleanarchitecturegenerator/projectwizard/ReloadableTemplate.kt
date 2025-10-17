package com.mitteloupe.cag.cleanarchitecturegenerator.projectwizard

import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.Recipe
import com.android.tools.idea.wizard.template.Template
import com.android.tools.idea.wizard.template.TemplateBuilder
import com.android.tools.idea.wizard.template.TemplateConstraint
import com.android.tools.idea.wizard.template.Thumb
import com.android.tools.idea.wizard.template.Widget
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.template
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.AppSettingsService

class ReloadableTemplate(
    private val factory: TemplateBuilder.() -> Unit
) : Template {
    private val appSettingsService = AppSettingsService.getInstance()

    private var lastDefaultDependencyInjection: String? = null

    private var cachedTemplate: Template = template(factory).also { updateConfigurationIfChanged() }

    private val upToDateTemplate: Template
        get() {
            if (updateConfigurationIfChanged()) {
                cachedTemplate = template(factory)
            }
            return cachedTemplate
        }

    override val name: String
        get() = upToDateTemplate.name
    override val description: String
        get() = upToDateTemplate.description
    override val documentationUrl: String?
        get() = upToDateTemplate.documentationUrl
    override val widgets: Collection<Widget<*>>
        get() = upToDateTemplate.widgets
    override val recipe: Recipe
        get() = upToDateTemplate.recipe
    override val uiContexts: Collection<WizardUiContext>
        get() = upToDateTemplate.uiContexts
    override val minSdk: Int
        get() = upToDateTemplate.minSdk
    override val category: Category
        get() = upToDateTemplate.category
    override val formFactor: FormFactor
        get() = upToDateTemplate.formFactor
    override val constraints: Collection<TemplateConstraint>
        get() = upToDateTemplate.constraints
    override val useGenericInstrumentedTests: Boolean
        get() = upToDateTemplate.useGenericInstrumentedTests
    override val useGenericLocalTests: Boolean
        get() = upToDateTemplate.useGenericLocalTests

    override fun thumb(): Thumb = upToDateTemplate.thumb()

    private fun updateConfigurationIfChanged(): Boolean {
        val defaultDependencyInjection = appSettingsService.defaultDependencyInjection
        return if (defaultDependencyInjection != lastDefaultDependencyInjection) {
            lastDefaultDependencyInjection = defaultDependencyInjection
            true
        } else {
            false
        }
    }
}
