package tech.gujin.ideaplugin.openurl.settings

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Nls
import tech.gujin.ideaplugin.openurl.data.ButtonConfig
import tech.gujin.ideaplugin.openurl.data.ButtonState
import tech.gujin.ideaplugin.openurl.data.OpenURLSettingService
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.DefaultCellEditor
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

class OpenUrlConfigurable() : SearchableConfigurable {

    constructor(project: Project) : this() {
        this.project = project
    }

    private var project: Project? = null
    private var panel: JPanel? = null
    private var buttonModel: DefaultTableModel? = null
    private var buttonTable: JBTable? = null

    override fun getId(): String {
        return ID
    }

    override fun getDisplayName(): @Nls String {
        return DISPLAY_NAME
    }

    override fun createComponent(): JComponent {
        val model = object : DefaultTableModel(arrayOf("Button Text", "Jump URL"), 0) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return true
            }
        }
        val table = JBTable(model)
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        table.rowHeight = JBUI.scale(28)
        table.showHorizontalLines = false
        table.showVerticalLines = false
        table.intercellSpacing = JBUI.emptySize()
        table.emptyText.text = "No buttons"
        table.columnModel.getColumn(0).preferredWidth = JBUI.scale(160)
        table.columnModel.getColumn(1).preferredWidth = JBUI.scale(360)
        table.columnModel.getColumn(0).cellRenderer = PaddedCellRenderer()
        table.columnModel.getColumn(1).cellRenderer = PaddedCellRenderer()
        table.columnModel.getColumn(0).cellEditor = PaddedCellEditor()
        table.columnModel.getColumn(1).cellEditor = PaddedCellEditor()

        buttonModel = model
        buttonTable = table

        val tablePanel = ToolbarDecorator.createDecorator(table)
            .setAddActionName("Add Button")
            .setAddAction { addButtonAndSelect() }
            .setAddActionUpdater { model.rowCount < ButtonState.MAX_BTN_COUNT }
            .setRemoveActionName("Delete Button")
            .setRemoveAction { removeSelectedButton() }
            .setRemoveActionUpdater { table.selectedRow >= 0 }
            .setMoveUpActionName("Move Up")
            .setMoveUpAction { moveSelectedButton(-1) }
            .setMoveUpActionUpdater { table.selectedRow > 0 }
            .setMoveDownActionName("Move Down")
            .setMoveDownAction { moveSelectedButton(1) }
            .setMoveDownActionUpdater { table.selectedRow >= 0 && table.selectedRow < table.rowCount - 1 }
            .setPreferredSize(Dimension(JBUI.scale(620), JBUI.scale(280)))
            .createPanel()

        panel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(12)
            add(tablePanel, BorderLayout.CENTER)
        }

        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        return readRowsFromTable(commitEditing = false) != readRowsFromState()
    }

    override fun apply() {
        val effectiveProject = getEffectiveProject()
            ?: throw ConfigurationException("Open a project to configure Open URL.")
        val rows = readValidatedRows()
        val buttonState = OpenURLSettingService.getButtonState(effectiveProject)
            ?: throw ConfigurationException("Open URL settings are not available.")

        buttonState.btnConfigMap.clear()
        rows.forEachIndexed { index, row ->
            buttonState.btnConfigMap[index] = ButtonConfig(index, index, row.text, row.url)
        }
    }

    override fun reset() {
        val model = buttonModel ?: return
        stopTableEditing()
        model.rowCount = 0
        readRowsFromState().forEach { row ->
            model.addRow(arrayOf(row.text, row.url))
        }
    }

    override fun disposeUIResources() {
        panel = null
        buttonModel = null
        buttonTable = null
    }

    private fun addButtonAndSelect() {
        val model = buttonModel ?: return
        val table = buttonTable ?: return
        stopTableEditing()
        if (model.rowCount >= ButtonState.MAX_BTN_COUNT) {
            Messages.showErrorDialog("Maximum number of buttons cannot exceed ${ButtonState.MAX_BTN_COUNT}", "Add Failure")
            return
        }
        model.addRow(arrayOf("New Button", ""))
        val row = model.rowCount - 1
        table.selectionModel.setSelectionInterval(row, row)
        table.scrollRectToVisible(table.getCellRect(row, 0, true))
        table.requestFocusInWindow()
        table.editCellAt(row, 0)
    }

    private fun removeSelectedButton() {
        val model = buttonModel ?: return
        val table = buttonTable ?: return
        val selectedRow = table.selectedRow
        if (selectedRow < 0) {
            return
        }
        stopTableEditing()
        model.removeRow(table.convertRowIndexToModel(selectedRow))
        if (model.rowCount > 0) {
            val nextRow = minOf(selectedRow, model.rowCount - 1)
            table.selectionModel.setSelectionInterval(nextRow, nextRow)
        }
    }

    private fun moveSelectedButton(direction: Int) {
        val model = buttonModel ?: return
        val table = buttonTable ?: return
        val selectedRow = table.selectedRow
        val targetRow = selectedRow + direction
        if (selectedRow < 0 || targetRow !in 0 until model.rowCount) {
            return
        }
        stopTableEditing()
        model.moveRow(selectedRow, selectedRow, targetRow)
        table.selectionModel.setSelectionInterval(targetRow, targetRow)
    }

    private fun readRowsFromTable(commitEditing: Boolean): List<ButtonRow> {
        val model = buttonModel ?: return emptyList()
        val table = buttonTable
        var editingModelRow = -1
        var editingModelColumn = -1
        var editingValue: Any? = null
        if (commitEditing) {
            stopTableEditing()
        } else if (table != null && table.isEditing) {
            editingModelRow = table.convertRowIndexToModel(table.editingRow)
            editingModelColumn = table.convertColumnIndexToModel(table.editingColumn)
            editingValue = table.cellEditor.cellEditorValue
        }
        return (0 until model.rowCount).map { row ->
            ButtonRow(
                text = stringValue(tableValue(model, row, 0, editingModelRow, editingModelColumn, editingValue)),
                url = stringValue(tableValue(model, row, 1, editingModelRow, editingModelColumn, editingValue))
            )
        }
    }

    private fun readValidatedRows(): List<ButtonRow> {
        val rows = readRowsFromTable(commitEditing = true)
        rows.forEachIndexed { index, row ->
            if (row.text.isBlank()) {
                throw ConfigurationException("Button text cannot be empty at row ${index + 1}.")
            }
            if (row.url.isBlank()) {
                throw ConfigurationException("Jump URL cannot be empty at row ${index + 1}.")
            }
        }

        val duplicate = rows
            .groupBy { it.text }
            .values
            .firstOrNull { it.size > 1 }
            ?.first()
        if (duplicate != null) {
            throw ConfigurationException("There is already another button with the same text: ${duplicate.text}")
        }
        return rows
    }

    private fun tableValue(
        model: DefaultTableModel,
        row: Int,
        column: Int,
        editingModelRow: Int,
        editingModelColumn: Int,
        editingValue: Any?
    ): Any? {
        return if (row == editingModelRow && column == editingModelColumn) {
            editingValue
        } else {
            model.getValueAt(row, column)
        }
    }

    private fun readRowsFromState(): List<ButtonRow> {
        val effectiveProject = getEffectiveProject() ?: return emptyList()
        val buttonState = OpenURLSettingService.getButtonState(effectiveProject) ?: return emptyList()
        return buttonState.btnConfigMap.values.map {
            ButtonRow(
                text = it.btnText.orEmpty(),
                url = it.btnUrl.orEmpty()
            )
        }
    }

    private fun getEffectiveProject(): Project? {
        project?.let { return it }
        val openProjects = ProjectManager.getInstance().openProjects
        return openProjects.singleOrNull()
    }

    private fun stopTableEditing() {
        val table = buttonTable ?: return
        if (table.cellEditor != null) {
            table.cellEditor.stopCellEditing()
        }
    }

    private data class ButtonRow(
        val text: String,
        val url: String
    )

    private class PaddedCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): JComponent {
            val component = super.getTableCellRendererComponent(table, value, isSelected, false, row, column) as JComponent
            component.border = JBUI.Borders.empty(0, 6)
            component.background = if (isSelected) table.selectionBackground else table.background
            component.foreground = if (isSelected) table.selectionForeground else table.foreground
            return component
        }
    }

    private class PaddedCellEditor : DefaultCellEditor(JTextField()) {
        init {
            val textField = component as JTextField
            textField.border = JBUI.Borders.empty(0, 6)
            textField.background = UIUtil.getTableBackground()
            textField.foreground = UIUtil.getTableForeground()
        }
    }

    companion object {
        const val ID = "tech.gujin.ideaplugin.openurl.settings"
        private const val DISPLAY_NAME = "Open URL"

        private fun stringValue(value: Any?): String {
            return value?.toString()?.trim().orEmpty()
        }
    }
}
