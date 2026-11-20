package io.github.fuzhengqu.cabincanvas.data.real

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Self-contained verification for the notification reply bridge. The posted
 * notification carries a real platform RemoteInput; replying from the car UI
 * sends the text back to ReplyEchoReceiver, which records and logs it.
 *
 * A platform Notification.Action is used on purpose: some AndroidX versions
 * do not expose remoteInputs through NotificationCompat on all OEM builds.
 */
object NotificationReplyTest {
    const val CHANNEL_ID = "cabincanvas_reply_test"
    const val ACTION = "io.github.fuzhengqu.cabincanvas.REPLY_ECHO"
    const val KEY = "reply_text"
    const val NOTIFICATION_ID = 1001

    @Volatile
    var lastReply: String? = null
        private set

    fun post(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        ensureChannel(context)
        val intent = Intent(context, ReplyEchoReceiver::class.java).setAction(ACTION)
        val pending = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val remoteInput = RemoteInput.Builder(KEY).setLabel("回复内容").build()
        val action = Notification.Action.Builder(
            Icon.createWithResource(context, android.R.drawable.ic_menu_send),
            "回复",
            pending,
        ).addRemoteInput(remoteInput).build()
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("CabinCanvas 回复测试")
            .setContentText("在车机通知中心点“快捷回复”，发送后查看日志")
            .setStyle(
                Notification.BigTextStyle().bigText(
                    "在车机通知中心点“快捷回复”，输入文字并发送。ReplyEchoReceiver 会记录并打印到 logcat（CabinCanvasReply）。",
                ),
            )
            .addAction(action)
            .setAutoCancel(true)
            .build()
        return runCatching {
            context.getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, notification)
            true
        }.getOrDefault(false)
    }

    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "CabinCanvas 回复测试", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    internal fun record(text: String) {
        lastReply = text
    }
}

class ReplyEchoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent)
        val text = results?.getCharSequence(NotificationReplyTest.KEY)?.toString().orEmpty()
        NotificationReplyTest.record(text)
        Log.i("CabinCanvasReply", "reply=$text")
    }
}
