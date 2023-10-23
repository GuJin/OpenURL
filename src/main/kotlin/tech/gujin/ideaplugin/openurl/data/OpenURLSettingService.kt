package tech.gujin.ideaplugin.openurl.data

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import java.io.Serializable

@State(name = "OpenURLSetting", storages = [Storage(value = "open_url_setting.xml")])
class OpenURLSettingService : PersistentStateComponent<OpenURLState> {

    companion object {

        fun getOpenURLState(project: Project?): OpenURLState? {
            return getInstance(project)?.openURLState
        }

        fun getButtonConfig(project: Project?, btnId: Int): ButtonConfig? {
            return getInstance(project)?.openURLState?.buttonState?.btnConfigMap?.get(btnId)
        }

        fun getButtonState(project: Project?): ButtonState? {
            return getInstance(project)?.openURLState?.buttonState
        }

        private fun getInstance(project: Project?): OpenURLSettingService? {
            return project?.getService(OpenURLSettingService::class.java)
        }
    }

    var openURLState: OpenURLState = OpenURLState(null, ButtonState())
        private set

    override fun getState(): OpenURLState {
        return openURLState
    }

    override fun loadState(state: OpenURLState) {
        this.openURLState = state
    }

}

data class OpenURLState(
        var pluginVersion: String? = null,
        var buttonState: ButtonState? = null
) : Serializable

data class ButtonState(
        var btnConfigMap: LinkedHashMap<Int, ButtonConfig> = LinkedHashMap()
) {

    companion object {
        const val MAX_BTN_COUNT = 8
    }

    fun isBtnValid(btnId: Int?, btnText: String): Int {
        val sameTextBtn = btnConfigMap.values.find { it.btnText == btnText && it.btnId != btnId }
        if (sameTextBtn != null) {
            return -1
        }
        return 0
    }

    fun outOfMax(): Boolean {
        return btnConfigMap.size > MAX_BTN_COUNT
    }

    fun createNewBtn(btnText: String, btnUrl: String): Int? {
        if (outOfMax()) {
            return null
        }
        val lastKey = btnConfigMap.entries.lastOrNull()?.key
        val key = if (lastKey == null) {
            0
        } else {
            lastKey + 1
        }
        btnConfigMap[key] = ButtonConfig(key, key, btnText, btnUrl)
        return key
    }

    fun updateButtonConfig(btnId: Int, btnText: String, btnUrl: String) {
        btnConfigMap[btnId] = ButtonConfig(btnId, btnId, btnText, btnUrl)
    }

    fun deleteButtonConfig(btnId: Int) {
        btnConfigMap.remove(btnId)
        adjustKeys(btnConfigMap)
    }

    private fun adjustKeys(map: LinkedHashMap<Int, ButtonConfig>) {
        if (map.isEmpty()) {
            return
        }
        var keyCounter = 0
        val tempMap = LinkedHashMap<Int, ButtonConfig>()
        for ((_, value) in map) {
            tempMap[keyCounter] = value.also { it.btnId = keyCounter }
            keyCounter++
        }

        map.clear()
        map.putAll(tempMap)
    }
}

data class ButtonConfig(
        var btnId: Int? = null,
        var sort: Int? = btnId,
        var btnText: String? = null,
        var btnUrl: String? = null
) : Serializable