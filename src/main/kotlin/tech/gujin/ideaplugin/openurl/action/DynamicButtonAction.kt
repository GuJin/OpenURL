package tech.gujin.ideaplugin.openurl.action

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import tech.gujin.ideaplugin.openurl.data.OpenURLSettingService
import tech.gujin.ideaplugin.openurl.utils.NotificationUtil


class DynamicButtonAction(
        private val btnId: Int
) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val btnUrl = OpenURLSettingService.getButtonConfig(e.project, btnId)?.btnUrl ?: return
        BrowserUtil.browse(btnUrl)
    }

    override fun displayTextInToolbar(): Boolean {
        return true
    }

    override fun update(e: AnActionEvent) {
        NotificationUtil.info(e.project!!, content = "update")
        val btnConfig = OpenURLSettingService.getButtonConfig(e.project, btnId)
        e.presentation.run {
            if (btnConfig == null) {
                isVisible = false
                return
            }
            isVisible = true
            text = btnConfig.btnText
            description = "Open in browser"
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

}