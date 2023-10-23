package tech.gujin.ideaplugin.openurl.utils

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object NotificationUtil {

    fun info(project: Project, title: String = "Open url", content: String) {
        val manager = NotificationGroupManager.getInstance()
        manager.getNotificationGroup("OpenUrlNotificationGroup")
                .createNotification(title = title, content = content, type = NotificationType.INFORMATION)
                .notify(project)
    }

}