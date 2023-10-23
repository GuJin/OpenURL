package tech.gujin.ideaplugin.openurl.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageConstants
import com.intellij.openapi.ui.Messages
import tech.gujin.ideaplugin.openurl.data.ButtonState
import tech.gujin.ideaplugin.openurl.data.OpenURLSettingService
import tech.gujin.ideaplugin.openurl.utils.NotificationUtil
import tech.gujin.ideaplugin.openurl.view.ButtonConfigDialog
import tech.gujin.ideaplugin.openurl.view.ManageButtonDialog


class EditButtonAction : AnAction() {

    private var manageButtonDialog: ManageButtonDialog? = null

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val buttonState = OpenURLSettingService.getButtonState(project) ?: return
        val btnConfigMap = buttonState.btnConfigMap
        if (btnConfigMap.isEmpty()) {
            Messages.showErrorDialog("No added buttons", "Edit Failure")
            return
        }

        manageButtonDialog = ManageButtonDialog(project, { doDelete(project, it, buttonState) }, { doEdit(project, it, buttonState) })
        manageButtonDialog!!.show()
        return
    }

    private fun doDelete(project: Project, btnId: Int, buttonState: ButtonState) {
        val buttonConfig = buttonState.btnConfigMap[btnId] ?: return
        val msg = "Button Text\n${buttonConfig.btnText}\n\nJump URL\n${buttonConfig.btnUrl}"
        val result = Messages.showYesNoDialog(project, msg, "Delete", Messages.getQuestionIcon())
        if (result != MessageConstants.YES) {
            return
        }
        buttonState.deleteButtonConfig(btnId)
        NotificationUtil.info(project, content = "Button deleted successfully")
        if (buttonState.btnConfigMap.isEmpty()) {
            manageButtonDialog?.close(0)
            return
        }

        manageButtonDialog?.updateAllItem()
    }

    private fun doEdit(project: Project, btnId: Int, buttonState: ButtonState) {
        val btnConfig = buttonState.btnConfigMap[btnId] ?: return
        val buttonConfigDialog = ButtonConfigDialog(project, "Edit Button", btnConfig)
        buttonConfigDialog.setOkActionListener { btnText, btnUrl ->

            val validCode = buttonState.isBtnValid(btnId, btnText)
            if (validCode == -1) {
                Messages.showErrorDialog("There is already another button with the same text", "Edit Failure")
                return@setOkActionListener false
            }

            buttonState.updateButtonConfig(btnId, btnText, btnUrl)
            manageButtonDialog?.updateItem(btnId)
            NotificationUtil.info(project, content = "Button updated successfully")
            return@setOkActionListener true
        }

        buttonConfigDialog.show()
    }

}