package io.github.fuzhengqu.cabincanvas.data.real

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class CarNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationStore.attach(this)
        NotificationStore.seed(this, activeNotifications)
    }

    override fun onListenerDisconnected() {
        NotificationStore.attach(null)
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn != null) NotificationStore.onPosted(this, sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn != null) NotificationStore.onRemoved(sbn)
    }
}
