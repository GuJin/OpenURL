package tech.gujin.ideaplugin.openurl.activity

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.startup.StartupActivity
import tech.gujin.ideaplugin.openurl.data.OpenURLSettingService
import tech.gujin.ideaplugin.openurl.data.OpenURLState
import tech.gujin.ideaplugin.openurl.utils.ActionUtil
import tech.gujin.ideaplugin.openurl.utils.NotificationUtil
import tech.gujin.ideaplugin.openurl.utils.PluginUtil

class PluginStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        val openURLState = OpenURLSettingService.getOpenURLState(project) ?: return
        checkVersion(openURLState)
        ActionUtil.registerAllAction()
        NotificationUtil.info(project, content = "registerAllAction")
    }

//    override suspend fun execute(project: Project) {
//        val openURLState = OpenURLSettingService.getOpenURLState(project) ?: return
//        checkVersion(openURLState)
//        ActionUtil.registerAllAction()
//        NotificationUtil.info(project, content = "registerAllAction")
//    }

    private fun checkVersion(openURLState: OpenURLState) {
        val savedVersion = openURLState.pluginVersion
        val pluginVersion = PluginUtil.getVersion()

        if (PluginUtil.compareVersion(savedVersion, pluginVersion) < 0) {
            openURLState.pluginVersion = pluginVersion
            doUpgrade(savedVersion, pluginVersion)
        }
    }

    private fun doUpgrade(fromVersion: String?, toVersion: String) {
        if (fromVersion == null) {
            // first init
            return
        }
    }
}