package tech.gujin.ideaplugin.openurl.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import tech.gujin.ideaplugin.openurl.data.ButtonConfig
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class ButtonConfigDialog(
        project: Project,
        myTitle: String,
        private val btnConfig: ButtonConfig? = null
) : DialogWrapper(project) {

    private lateinit var tFBtnText: JTextField
    private lateinit var tFBtnUrl: JTextField
    private var doOKAction: ((String, String) -> Boolean)? = null

    init {
        init()
        title = myTitle
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())

        val constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.HORIZONTAL

        constraints.gridy = 0
        constraints.insets = JBUI.insetsLeft(3)
        panel.add(JLabel("Button Text"), constraints)

        constraints.gridy = 1
        constraints.insets = JBUI.emptyInsets()
        tFBtnText = JTextField(40)
        panel.add(tFBtnText, constraints)

        constraints.gridy = 2
        constraints.insets = JBUI.insets(8, 3, 0, 0)
        panel.add(JLabel("Jump URL"), constraints)

        constraints.gridy = 3
        constraints.insets = JBUI.emptyInsets()
        tFBtnUrl = JTextField(40)
        panel.add(tFBtnUrl, constraints)

        btnConfig?.run {
            tFBtnText.text = btnText
            tFBtnUrl.text = btnUrl
        }

        return panel
    }

    override fun doOKAction() {
        if (tFBtnText.text.isBlank()) {
            Messages.showErrorDialog("Button text cannot be empty", "Error")
            return
        }
        if (tFBtnUrl.text.isBlank()) {
            Messages.showErrorDialog("Jump URL cannot be empty", "Error")
            return
        }
        val result = doOKAction?.invoke(tFBtnText.text, tFBtnUrl.text)
        if (result == true) {
            super.doOKAction()
        }
    }

    fun setOkActionListener(function: (String, String) -> Boolean) {
        doOKAction = function
    }

}