package io.github.fuzhengqu.cabincanvas.data.real

import android.app.Notification
import android.app.RemoteInput
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.text.format.DateUtils
import androidx.compose.ui.graphics.Color
import io.github.fuzhengqu.cabincanvas.data.CarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Same-process bridge between the NotificationListenerService and the car UI.
 * The service owns the real StatusBarNotification objects; the UI only sees
 * a small immutable snapshot and calls back by key.
 */
object NotificationStore {
    private val _notifications = MutableStateFlow<List<CarNotification>>(emptyList())
    val notifications: StateFlow<List<CarNotification>> = _notifications.asStateFlow()

    private val pending = ConcurrentHashMap<String, StatusBarNotification>()

    @Volatile
    private var service: CarNotificationListenerService? = null

    fun attach(listener: CarNotificationListenerService?) {
        service = listener
    }

    fun requestRebind(context: Context) {
        runCatching {
            NotificationListenerService.requestRebind(
                ComponentName(context, CarNotificationListenerService::class.java),
            )
        }
    }

    fun seed(context: Context, items: Array<StatusBarNotification>?) {
        items?.forEach { onPosted(context, it) }
    }

    fun onPosted(context: Context, sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        if (sbn.isOngoing) return
        if (notification.category == Notification.CATEGORY_TRANSPORT) return
        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val body = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (title.isBlank() && body.isBlank()) return

        val app = appLabel(context, sbn.packageName)
        val color = if (notification.color != 0) {
            Color(notification.color)
        } else {
            appColor(sbn.packageName)
        }
        val canReply = notification.actions?.any { action ->
            action.actionIntent != null && !action.remoteInputs.isNullOrEmpty()
        } == true
        val item = CarNotification(
            app = app,
            title = title.ifBlank { app },
            body = body,
            time = DateUtils.getRelativeTimeSpanString(
                sbn.postTime,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
            ).toString(),
            color = color,
            canReply = canReply,
            key = sbn.key,
            packageName = sbn.packageName,
        )
        pending[sbn.key] = sbn
        _notifications.value = (listOf(item) + _notifications.value.filterNot { it.key == sbn.key }).take(30)
    }

    fun onRemoved(sbn: StatusBarNotification) {
        pending.remove(sbn.key)
        _notifications.value = _notifications.value.filterNot { it.key == sbn.key }
    }

    fun clear() {
        _notifications.value = emptyList()
    }

    fun addDemo(item: CarNotification) {
        _notifications.value = (listOf(item) + _notifications.value).take(30)
    }

    fun reply(context: Context, key: String?, text: String): Boolean {
        if (key == null || text.isBlank()) return false
        val sbn = pending[key] ?: return false
        val action = sbn.notification?.actions?.firstOrNull { candidate ->
            candidate.actionIntent != null && !candidate.remoteInputs.isNullOrEmpty()
        } ?: return false
        val inputs = action.remoteInputs ?: return false
        val results = Bundle().apply {
            inputs.forEach { input -> putCharSequence(input.resultKey, text) }
        }
        return runCatching {
            val intent = Intent()
            RemoteInput.addResultsToIntent(inputs, intent, results)
            action.actionIntent.send(context, 0, intent)
            true
        }.getOrDefault(false)
    }

    private fun appLabel(context: Context, packageName: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName.substringAfterLast('.'))

    private fun appColor(packageName: String): Color {
        val colors = listOf(
            Color(0xFF2F6BFF), Color(0xFF19A974), Color(0xFFE32E27), Color(0xFFFFB020),
            Color(0xFF8B5CF6), Color(0xFF0EA5E9), Color(0xFF6B7280), Color(0xFFEC4899),
        )
        return colors[packageName.hashCode().mod(colors.size)]
    }

    fun notificationTitle(item: CarNotification): String =
        if (TextUtils.isEmpty(item.title)) item.app else item.title
}
