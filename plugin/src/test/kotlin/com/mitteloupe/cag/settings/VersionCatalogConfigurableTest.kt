package com.mitteloupe.cag.settings

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog.VersionCatalogConfigurable
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.versioncatalog.VersionCatalogSettingsService
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Component
import java.awt.Container
import javax.swing.JTable
import javax.swing.SwingUtilities.invokeAndWait

private const val COLUMN_INDEX_LIBRARIES = 0
private const val COLUMN_INDEX_VERSION = 1

class VersionCatalogConfigurableTest {
    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given one library when creating panel then table has two columns`() {
        // Given
        val groupVersion = "1.7.3"
        val initialValues = mapOf("kotlinxCoroutines" to groupVersion)
        val expectedColumnCount = 2

        // When
        val (table, _) = givenConfigurableWithValuesWhenPanelCreated(initialValues)
        val actualColumnCount = table.columnCount

        // Then
        assertEquals(expectedColumnCount, actualColumnCount)
    }

    @Test
    @Suppress("MaxLineLength", "ktlint:standard:max-line-length")
    fun `Given initial values grouped by common version keys when creating panel then table groups modules and plugins`() {
        // Given
        val library1 = "org.jetbrains.kotlinx:kotlinx-coroutines-core"
        val library2 = "org.jetbrains.kotlinx:kotlinx-coroutines-test"
        val plugin1 = "org.jetbrains.kotlin.jvm"
        val plugin2 = "org.jetbrains.kotlin.android"
        val plugin3 = "org.jetbrains.kotlin.plugin.compose"

        val librariesVersion = "1.7.3"
        val pluginsVersion = "2.2.10"
        val initialValues =
            mapOf(
                "kotlinxCoroutines" to librariesVersion,
                "kotlin" to pluginsVersion
            )

        // When
        val (table, _) = givenConfigurableWithValuesWhenPanelCreated(initialValues)
        val pluginsRow = table.indexOfRowContainingAll(listOf(plugin1, plugin2, plugin3))
        val actualPluginsVersion = table.actualVersionAt(pluginsRow)
        val librariesRow = table.indexOfRowContainingAll(listOf(library1, library2))
        val actualLibrariesVersion = table.actualVersionAt(librariesRow)

        // Then
        assertEquals(pluginsVersion, actualPluginsVersion)
        assertEquals(librariesVersion, actualLibrariesVersion)
    }

    @Test
    fun `Given no edits when isModified then returns false`() {
        // Given
        val values = mapOf("androidxCoreKtx" to "1.12.0")
        val (_, panel) = givenConfigurableWithValuesWhenPanelCreated(values)

        // When
        val actualModified = panel.isModified()

        // Then
        assertFalse(actualModified)
    }

    @Test
    fun `Given initial version value when edited then isModified is true`() {
        // Given
        val module = "androidx.core:core-ktx"
        val (table, panel) = givenConfigurableWithValuesWhenPanelCreated(mapOf("androidxCoreKtx" to "1.12.0"))
        val targetRow = table.indexOfRowContaining(module)

        // When
        table.editedVersion(targetRow, "9.9.9")

        // Then
        assertTrue(panel.isModified())
    }

    @Test
    fun `Given initial version value when edited then version updated`() {
        // Given
        val module = "androidx.core:core-ktx"
        val (table, _) = givenConfigurableWithValuesWhenPanelCreated(mapOf("androidxCoreKtx" to "1.12.0"))
        val targetRow = table.indexOfRowContaining(module)
        val newVersion = "9.9.9"

        // When
        table.editedVersion(targetRow, newVersion)
        val actualVersion = table.actualVersionAt(targetRow)

        // Then
        assertEquals(newVersion, actualVersion)
    }

    @Test
    fun `Given edited version when apply then service receives collected values`() {
        // Given
        val kotlinVersionedPlugin1 = "org.jetbrains.kotlin.jvm"
        val kotlinVersionedPlugin2 = "org.jetbrains.kotlin.android"
        val oldVersion = "2.2.10"
        val initialState = mapOf("kotlin" to oldVersion)
        val service = InMemorySettingsService(initialState)
        val configurable = versionCatalogConfigurable(service)
        val panel = configurable.createPanel()
        val table = panel.descendantTable()
        val targetRow = table.indexOfRowContainingAll(listOf(kotlinVersionedPlugin1, kotlinVersionedPlugin2))
        val newVersion = "1.8.0"
        table.editedVersion(targetRow, newVersion)

        // When
        invokeAndWait { panel.apply() }
        val actualState = service.getCurrentValues()

        // Then
        assertEquals(newVersion, actualState["kotlin"])
    }

    @Test
    fun `Given edited version when apply then isModified becomes false`() {
        // Given
        val kotlinVersionedPlugin1 = "org.jetbrains.kotlin.jvm"
        val kotlinVersionedPlugin2 = "org.jetbrains.kotlin.android"
        val initialState = mapOf("kotlin" to "2.2.10")
        val service = InMemorySettingsService(initialState)
        val configurable = versionCatalogConfigurable(service)
        val panel = configurable.createPanel()
        val table = panel.descendantTable()
        val targetRow = table.indexOfRowContainingAll(listOf(kotlinVersionedPlugin1, kotlinVersionedPlugin2))
        table.editedVersion(targetRow, "1.8.0")

        // When
        invokeAndWait { panel.apply() }

        // Then
        assertFalse(panel.isModified())
    }

    @Test
    fun `Given changed values and then reset when reset called then isModified false`() {
        // Given
        val module = LibraryConstants.MATERIAL.module
        val originalVersion = "1.11.0"
        val service = InMemorySettingsService(mapOf("material" to originalVersion))
        val configurable = versionCatalogConfigurable(service)
        val panel = configurable.createPanel()
        val table = panel.descendantTable()
        val targetRow = table.indexOfRowContaining(module)
        table.editedVersion(targetRow, "1.99.0")

        // When
        invokeAndWait { configurable.reset() }

        // Then
        assertFalse(panel.isModified())
    }

    @Test
    fun `Given changed values and then reset when reset called then values return to current settings and isModified false`() {
        // Given
        val module = LibraryConstants.MATERIAL.module
        val originalVersion = "1.11.0"
        val service = InMemorySettingsService(mapOf("material" to originalVersion))
        val configurable = versionCatalogConfigurable(service)
        val panel = configurable.createPanel()
        val table = panel.descendantTable()
        val targetRow = table.indexOfRowContaining(module)
        table.editedVersion(targetRow, "1.99.0")

        // When
        invokeAndWait { configurable.reset() }
        configurable.apply()
        val actualVersion = service.getCurrentValues()["material"]

        // Then
        assertEquals(originalVersion, actualVersion)
    }

    private fun givenConfigurableWithValuesWhenPanelCreated(values: Map<String, String>): Pair<JTable, DialogPanel> {
        val service = InMemorySettingsService(values)
        val configurable = versionCatalogConfigurable(service)

        val panel = configurable.createPanel()

        val table = panel.descendantTable()

        return Pair(table, panel)
    }

    private fun versionCatalogConfigurable(service: InMemorySettingsService): VersionCatalogConfigurable =
        VersionCatalogConfigurable(settingsService = service, configurableDisplayName = "", configurableId = "", description = "")

    private fun JTable.indexOfRowContaining(module: String): Int {
        var result: Int? = null
        invokeAndWait {
            result =
                (0 until rowCount).firstOrNull { r ->
                    (getValueAt(r, COLUMN_INDEX_LIBRARIES) as? String)?.contains(module) == true
                }
        }
        return result ?: error("Could not find row for module: $module")
    }

    private fun JTable.indexOfRowContainingAll(modules: List<String>): Int {
        var result: Int? = null
        invokeAndWait {
            result =
                (0 until rowCount).firstOrNull { rowIndex ->
                    val text = (getValueAt(rowIndex, COLUMN_INDEX_LIBRARIES) as? String).orEmpty()
                    modules.all { module -> text.contains(module) }
                }
        }
        return result ?: throw AssertionError("Could not find grouped row containing all modules: $modules")
    }

    private fun JTable.editedVersion(
        rowIndex: Int,
        newValue: String
    ) {
        invokeAndWait {
            val started = editCellAt(rowIndex, COLUMN_INDEX_VERSION)
            assertTrue("Failed to start editing cell at row=$rowIndex col=1", started)
            val editor = cellEditor
            val editorComponent = editorComponent as JBTextField
            editorComponent.text = newValue
            editor.stopCellEditing()
        }
    }

    private fun JTable.actualVersionAt(rowIndex: Int): String {
        var value = ""
        invokeAndWait {
            value = getValueAt(rowIndex, COLUMN_INDEX_VERSION) as String
        }
        return value
    }

    private fun Component.descendantTable(): JTable {
        if (this is JTable) {
            return this
        }
        if (this is Container) {
            repeat(componentCount) { index ->
                val child = getComponent(index)
                try {
                    return child.descendantTable()
                } catch (_: AssertionError) {
                }
            }
        }
        throw AssertionError("Expected to find a JTable in the configurable panel")
    }

    private class InMemorySettingsService(initial: Map<String, String>) : VersionCatalogSettingsService() {
        init {
            replaceAll(initial)
        }
    }
}
