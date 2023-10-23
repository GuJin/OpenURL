package tech.gujin.ideaplugin.openurl.utils

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.util.text.VersionComparatorUtil

object PluginUtil {

    fun getVersion(): String {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId("tech.gujin.ideaplugin.openurl"))
        return plugin?.version ?: "1.0.0"
    }

    fun compareVersion(savedVersion: String?, pluginVersion: String): Int {
        return VersionComparatorUtil.compare(savedVersion,pluginVersion)
    }

}