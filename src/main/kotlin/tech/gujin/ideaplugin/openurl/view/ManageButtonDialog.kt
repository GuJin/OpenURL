package tech.gujin.ideaplugin.openurl.view

import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.Gray
import tech.gujin.ideaplugin.openurl.data.ButtonConfig
import tech.gujin.ideaplugin.openurl.data.OpenURLSettingService
import java.awt.*
import java.awt.event.ActionEvent
import javax.swing.*


class ManageButtonDialog(
        project: Project,
        private val onDelete: (btnId: Int) -> Unit,
        private val onEdit: (btnId: Int) -> Unit
) : DialogWrapper(project) {

    private var btnConfigMap = OpenURLSettingService.getButtonState(project)?.btnConfigMap ?: LinkedHashMap()
    private var panel: JPanel? = null

    init {
        init()
        title = "Manage Buttons"
        isOKActionEnabled = false
    }

    override fun createCenterPanel(): JComponent {
        panel = JPanel()
        panel!!.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        for (btn in btnConfigMap.values) {
            panel!!.add(ListItemPanel(btn, onDelete, onEdit))
        }

        val jScrollPane = JScrollPane(panel)
        jScrollPane.border = null
        return jScrollPane
    }

    fun updateItem(btnId: Int) {
        val safePanel = panel ?: return
        val index = btnConfigMap.values.indexOfFirst { it.btnId == btnId }
        val buttonConfig = btnConfigMap[btnId] ?: return
        safePanel.run {
            remove(index)
            add(ListItemPanel(buttonConfig, onDelete, onEdit), index)
            revalidate()
            repaint()
        }
    }

    fun updateAllItem() {
        val safePanel = panel ?: return
        val values = btnConfigMap.values.toList()
        val panelSize = safePanel.components.filterIsInstance<ListItemPanel>().size
        val max = maxOf(values.size, panelSize)

        for (i in 0 until max) {
            val buttonConfig = values.getOrNull(i)
            if (buttonConfig != null) {
                if (i < panelSize) {
                    safePanel.remove(i)
                    safePanel.add(ListItemPanel(buttonConfig, onDelete, onEdit), i)
                }
            } else {
                if (i < panelSize) {
                    safePanel.remove(i)
                }
            }
        }

        safePanel.revalidate()
        safePanel.repaint()

        peer.pack()
    }

    override fun createActions(): Array<Action> {
        return arrayOf(CloseAction())
    }


    private inner class CloseAction : AbstractAction(CommonBundle.getCloseButtonText()) {

        override fun actionPerformed(e: ActionEvent?) {
            close(0)
        }
    }


    private inner class ListItemPanel(item: ButtonConfig, onDelete: (btnId: Int) -> Unit, onEdit: (btnId: Int) -> Unit) : JPanel() {

        init {
            layout = GridBagLayout()
            preferredSize = Dimension(500, 45)

            val gbc = GridBagConstraints()
            gbc.fill = GridBagConstraints.BOTH
            gbc.anchor = GridBagConstraints.CENTER

            val labelText = JLabel(item.btnText)
            val labelUrl = JLabel(item.btnUrl)
            val deleteButton = JButton("Delete")
            val editButton = JButton("Edit")

            deleteButton.addActionListener {
                item.btnId?.run(onDelete)
            }
            editButton.addActionListener {
                item.btnId?.run(onEdit)
            }

            gbc.gridx = 0
            gbc.gridy = 0
            gbc.gridheight = 1
            gbc.weightx = 1.0
            gbc.anchor = GridBagConstraints.WEST
            add(labelText, gbc)

            gbc.gridx = 0
            gbc.gridy = 1
            gbc.gridheight = 1
            gbc.weightx = 1.0
            gbc.anchor = GridBagConstraints.WEST
            add(labelUrl, gbc)

            gbc.gridx = 1
            gbc.gridy = 0
            gbc.gridheight = 2
            gbc.weightx = 0.0
            gbc.weighty = 0.0
            add(deleteButton, gbc)

            gbc.gridx = 2
            gbc.gridy = 0
            gbc.gridheight = 2
            gbc.weightx = 0.0
            gbc.weighty = 0.0
            add(editButton, gbc)
        }

        override fun paint(g: Graphics) {
            super.paint(g)
            val g2d = g as? Graphics2D ?: return
            val strokeWidth = 1
            g2d.color = Gray._150
            g2d.stroke = BasicStroke(strokeWidth.toFloat())
            g2d.drawLine(0, height - strokeWidth, width, height - strokeWidth)
        }

    }

}