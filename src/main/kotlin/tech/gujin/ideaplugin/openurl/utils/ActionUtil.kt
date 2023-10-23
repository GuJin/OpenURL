package tech.gujin.ideaplugin.openurl.utils

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions.*
import tech.gujin.ideaplugin.openurl.action.DynamicButtonAction
import tech.gujin.ideaplugin.openurl.data.ButtonState

object ActionUtil {

    private fun getActionId(btnId: Int): String {
        return "OpenUrlButton#$btnId"
    }

    fun registerAllAction() {
        val actionManager = ActionManager.getInstance()
        for (i in 0 until ButtonState.MAX_BTN_COUNT) {
            if (actionManager.getAction(getActionId(i)) != null) {
                continue
            }
            registerAction(actionManager, i)
        }
    }

    private fun registerAction(actionManager: ActionManager = ActionManager.getInstance(), btnId: Int) {
        val dynamicButtonAction = DynamicButtonAction(btnId)
        execOnAction(actionManager) { add(dynamicButtonAction) }
        actionManager.registerAction(getActionId(btnId), dynamicButtonAction)
    }

    private fun execOnAction(actionManager: ActionManager, exec: DefaultActionGroup.() -> Unit) {
        (actionManager.getAction(GROUP_MAIN_TOOLBAR) as? DefaultActionGroup)?.run { exec.invoke(this) }
        (actionManager.getAction(GROUP_NAVBAR_TOOLBAR) as? DefaultActionGroup)?.run { exec.invoke(this) }
        (actionManager.getAction(GROUP_MAIN_TOOLBAR_RIGHT) as? DefaultActionGroup)?.run { exec.invoke(this) }
    }

}