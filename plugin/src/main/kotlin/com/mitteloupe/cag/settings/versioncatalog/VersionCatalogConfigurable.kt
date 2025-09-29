package com.mitteloupe.cag.settings.versioncatalog

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.mitteloupe.cag.cleanarchitecturegenerator.CleanArchitectureGeneratorBundle
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import java.awt.Component
import javax.swing.DefaultCellEditor
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

private const val COLUMN_INDEX_LIBRARIES = 0
private const val COLUMN_INDEX_VERSION = 1

open class VersionCatalogConfigurable(
    private val settingsService: VersionCatalogSettingsService,
    configurableDisplayName: String,
    configurableId: String,
    private val description: String
) : BoundSearchableConfigurable(
        configurableDisplayName,
        configurableId
    ) {
    private lateinit var table: JTable
    private lateinit var versionsModel: VersionsTableModel

    override fun createPanel() =
        panel {
            if (!description.isNullOrBlank()) {
                row {
                    text(description)
                }
            }
            val currentMap = settingsService.getCurrentValues()
            versionsModel = VersionsTableModel(currentMap)

            val newTable = JBTable(versionsModel)
            table =
                newTable.apply {
                    setShowGrid(false)
                    rowHeight = JBUI.scale(24)
                    columnModel.getColumn(COLUMN_INDEX_LIBRARIES).apply {
                        preferredWidth = JBUI.scale(260)
                        cellRenderer =
                            object : DefaultTableCellRenderer() {
                                override fun getTableCellRendererComponent(
                                    table: JTable?,
                                    value: Any?,
                                    isSelected: Boolean,
                                    hasFocus: Boolean,
                                    row: Int,
                                    column: Int
                                ): Component {
                                    val cellRendererComponent =
                                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                                    if (cellRendererComponent is JComponent) {
                                        cellRendererComponent.toolTipText = value?.toString()
                                    }
                                    return cellRendererComponent
                                }
                            }
                    }
                    columnModel.getColumn(COLUMN_INDEX_VERSION).apply {
                        preferredWidth = JBUI.scale(200)
                        cellRenderer =
                            DefaultTableCellRenderer().apply {
                                horizontalAlignment = SwingConstants.RIGHT
                            }
                        cellEditor =
                            DefaultCellEditor(
                                JBTextField().apply { horizontalAlignment = SwingConstants.RIGHT }
                            )
                    }
                }

            val defaultValues = buildDefaultValues()

            SwingUtilities.invokeLater { adjustRowHeights(table) }
            versionsModel.addTableModelListener { SwingUtilities.invokeLater { adjustRowHeights(table) } }

            val hasModifiedSelectionPredicate =
                object : ComponentPredicate() {
                    override fun addListener(listener: (Boolean) -> Unit) {
                        table.selectionModel.addListSelectionListener { listener(invoke()) }
                        versionsModel.addTableModelListener { listener(invoke()) }
                    }

                    override fun invoke(): Boolean =
                        table.selectedRows.isNotEmpty() && versionsModel.deviatesFromDefaults(table.selectedRows, defaultValues)
                }

            val deviatesFromDefaultsPredicate =
                object : ComponentPredicate() {
                    override fun addListener(listener: (Boolean) -> Unit) {
                        versionsModel.addTableModelListener { listener(invoke()) }
                    }

                    override fun invoke(): Boolean = versionsModel.hasAnyRows() && versionsModel.deviatesFromDefaults(defaultValues)
                }

            row {
                scrollCell(table)
                    .align(Align.FILL)
                    .resizableColumn()
            }.resizableRow()

            row {
                @Suppress("UnstableApiUsage")
                placeholder().resizableColumn()

                button(
                    CleanArchitectureGeneratorBundle.message("settings.versions.reset.selected")
                ) {
                    val selectedRows = table.selectedRows
                    if (selectedRows.isNotEmpty()) {
                        versionsModel.resetRowsToDefaults(selectedRows, defaultValues)
                    }
                }
                    .applyToComponent {
                        toolTipText =
                            CleanArchitectureGeneratorBundle.message("settings.versions.reset.selected.tooltip")
                    }
                    .enabledIf(hasModifiedSelectionPredicate)

                button(
                    CleanArchitectureGeneratorBundle.message("settings.versions.reset.all")
                ) {
                    versionsModel.resetAllToDefaults(defaultValues)
                }
                    .applyToComponent {
                        toolTipText = CleanArchitectureGeneratorBundle.message("settings.versions.reset.all.tooltip")
                    }
                    .enabledIf(deviatesFromDefaultsPredicate)
            }.layout(RowLayout.PARENT_GRID)

            onApply {
                val newMap = versionsModel.collectValues()
                settingsService.replaceAll(newMap)
                val valuesToReload = settingsService.getCurrentValues()
                versionsModel.reload(valuesToReload)
                SwingUtilities.invokeLater {
                    autoSizeColumnToContent(table, COLUMN_INDEX_VERSION)
                    adjustRowHeights(table)
                }
            }
            onReset {
                val values = settingsService.getCurrentValues()
                versionsModel.reload(values)
                SwingUtilities.invokeLater {
                    autoSizeColumnToContent(table, COLUMN_INDEX_VERSION)
                    adjustRowHeights(table)
                }
            }
            onIsModified {
                versionsModel.isModified()
            }
        }
}

private class VersionsTableModel(values: Map<String, String>) : AbstractTableModel() {
    private data class Row(
        val versionKey: String,
        val libraries: String,
        val originalVersion: String,
        val editedVersion: String
    )

    private val rows = mutableListOf<Row>()

    init {
        reload(values)
    }

    fun reload(versions: Map<String, String>) {
        rows.clear()

        val versionKeyToLibraries: Map<String, List<String>> =
            buildMap {
                LibraryConstants.ALL_LIBRARIES
                    .filter { it.version != null }
                    .groupBy(
                        keySelector = { it.version!!.key },
                        valueTransform = { it.module }
                    )
                    .forEach { (versionKey, modules) ->
                        put(versionKey, (get(versionKey).orEmpty() + modules).distinct())
                    }

                PluginConstants.ALL_PLUGINS
                    .groupBy(
                        keySelector = { it.version.key },
                        valueTransform = { it.id }
                    )
                    .forEach { (versionKey, pluginIds) ->
                        put(versionKey, (get(versionKey).orEmpty() + pluginIds).distinct())
                    }
            }

        val rowsBuilt =
            versionKeyToLibraries.entries
                .sortedBy { it.key }
                .map { (versionKey, libraries) ->
                    val html = "<html>" + libraries.sorted().joinToString(separator = "<br/>") + "</html>"
                    val versionForLibraries = versions[versionKey].orEmpty()
                    Row(
                        versionKey = versionKey,
                        libraries = html,
                        originalVersion = versionForLibraries,
                        editedVersion = versionForLibraries
                    )
                }

        rows.addAll(rowsBuilt)
        fireTableDataChanged()
    }

    fun isModified(): Boolean = rows.any { it.originalVersion != it.editedVersion }

    fun collectValues(): Map<String, String> = rows.associate { row -> row.versionKey to row.editedVersion }

    fun hasAnyRows(): Boolean = rows.isNotEmpty()

    override fun getRowCount(): Int = rows.size

    override fun getColumnCount(): Int = 2

    override fun getColumnName(columnIndex: Int): String =
        when (columnIndex) {
            COLUMN_INDEX_LIBRARIES -> CleanArchitectureGeneratorBundle.message("settings.versions.column.key")
            COLUMN_INDEX_VERSION -> CleanArchitectureGeneratorBundle.message("settings.versions.column.value")
            else -> error("Unexpected column index $columnIndex")
        }

    override fun isCellEditable(
        rowIndex: Int,
        columnIndex: Int
    ): Boolean = columnIndex == COLUMN_INDEX_VERSION

    override fun getValueAt(
        rowIndex: Int,
        columnIndex: Int
    ): Any =
        when (columnIndex) {
            COLUMN_INDEX_LIBRARIES -> rows[rowIndex].libraries
            COLUMN_INDEX_VERSION -> rows[rowIndex].editedVersion
            else -> error("Unexpected column index $columnIndex")
        }

    override fun setValueAt(
        value: Any?,
        rowIndex: Int,
        columnIndex: Int
    ) {
        if (columnIndex == COLUMN_INDEX_VERSION) {
            rows[rowIndex] = rows[rowIndex].copy(editedVersion = (value as? String).orEmpty())
        }
        fireTableCellUpdated(rowIndex, columnIndex)
    }

    fun resetRowsToDefaults(
        rowIndices: IntArray,
        defaultValues: Map<String, String>
    ) {
        rowIndices
            .filter { index -> index in 0 until rows.size }
            .forEach { index ->
                val row = rows[index]
                val defaultValue = row.defaultValue(defaultValues)
                rows[index] = row.copy(editedVersion = defaultValue)
            }
        fireTableDataChanged()
    }

    fun resetAllToDefaults(defaultValues: Map<String, String>) {
        rows.indices.forEach { index ->
            val row = rows[index]
            val defaultValue = row.defaultValue(defaultValues)
            rows[index] = row.copy(editedVersion = defaultValue)
        }
        fireTableDataChanged()
    }

    fun deviatesFromDefaults(defaultValues: Map<String, String>): Boolean =
        rows.any { row -> row.editedVersion != row.defaultValue(defaultValues) }

    fun deviatesFromDefaults(
        rowIndices: IntArray,
        defaultValues: Map<String, String>
    ): Boolean =
        rowIndices.any { index ->
            if (index in rows.indices) {
                val row = rows[index]
                row.editedVersion != row.defaultValue(defaultValues)
            } else {
                false
            }
        }

    private fun Row.defaultValue(defaultValues: Map<String, String>): String = defaultValues[versionKey] ?: originalVersion
}

private fun autoSizeColumnToContent(
    table: JTable,
    columnIndex: Int,
    extraPadding: Int = JBUI.scale(16)
) {
    if (columnIndex < 0 || columnIndex >= table.columnModel.columnCount) {
        return
    }
    val column = table.columnModel.getColumn(columnIndex)

    val header = table.tableHeader
    var preferredWidth = 0
    if (header != null) {
        val headerRenderer = column.headerRenderer ?: header.defaultRenderer
        val headerComponent =
            headerRenderer.getTableCellRendererComponent(
                table,
                column.headerValue,
                false,
                false,
                -1,
                columnIndex
            )
        preferredWidth = headerComponent.preferredSize.width
    }

    repeat(table.rowCount) { row ->
        val cellRenderer = table.getCellRenderer(row, columnIndex)
        val component =
            cellRenderer.getTableCellRendererComponent(
                table,
                table.getValueAt(row, columnIndex),
                false,
                false,
                row,
                columnIndex
            )
        preferredWidth = maxOf(preferredWidth, component.preferredSize.width)
    }

    column.preferredWidth = preferredWidth + extraPadding
}

private fun adjustRowHeights(
    table: JTable,
    extraPadding: Int = JBUI.scale(4)
) {
    if (table.rowCount == 0) {
        return
    }

    repeat(table.rowCount) { row ->
        val cellRenderer = table.getCellRenderer(row, COLUMN_INDEX_LIBRARIES)
        val cellRendererComponent =
            cellRenderer.getTableCellRendererComponent(
                table,
                table.getValueAt(row, COLUMN_INDEX_LIBRARIES),
                false,
                false,
                row,
                COLUMN_INDEX_LIBRARIES
            )
        val preferred = cellRendererComponent.preferredSize.height + extraPadding
        if (preferred > 0 && table.getRowHeight(row) != preferred) {
            table.setRowHeight(row, preferred)
        }
    }
}

private fun buildDefaultValues(): Map<String, String> =
    buildMap {
        LibraryConstants.ALL_LIBRARIES
            .mapNotNull { it.version }
            .forEach { version -> put(version.key, version.version) }

        PluginConstants.ALL_PLUGINS
            .forEach { plugin -> put(plugin.version.key, plugin.version.version) }
    }
