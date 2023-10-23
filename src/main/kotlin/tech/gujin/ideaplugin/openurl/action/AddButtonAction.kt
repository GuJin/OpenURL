package tech.gujin.ideaplugin.openurl.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import tech.gujin.ideaplugin.openurl.data.ButtonState
import tech.gujin.ideaplugin.openurl.data.OpenURLSettingService
import tech.gujin.ideaplugin.openurl.utils.ActionUtil
import tech.gujin.ideaplugin.openurl.utils.NotificationUtil
import tech.gujin.ideaplugin.openurl.view.ButtonConfigDialog


class AddButtonAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return

        val buttonState = OpenURLSettingService.getButtonState(project) ?: return

        if (buttonState.outOfMax()) {
            Messages.showErrorDialog("Maximum number of buttons cannot exceed ${ButtonState.MAX_BTN_COUNT}", "Add Failure")
            return
        }

        val dialog = ButtonConfigDialog(project, "Add Button")

        dialog.setOkActionListener { btnText, btnUrl ->

            val validCode = buttonState.isBtnValid(null, btnText)
            if (validCode == -1) {
                Messages.showErrorDialog("Cannot add buttons with the same text", "Add Failure")
                return@setOkActionListener false
            }

            val btnId = buttonState.createNewBtn(btnText, btnUrl)
            if (btnId == null) {
                Messages.showErrorDialog("Maximum number of buttons cannot exceed ${ButtonState.MAX_BTN_COUNT}", "Add Failure")
                return@setOkActionListener false
            }

            NotificationUtil.info(project, content = "Create button successful")
            return@setOkActionListener true
        }

        dialog.show()

    }

}