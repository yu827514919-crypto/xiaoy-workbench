package com.example.xiaoy.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.xiaoy.MainActivity
import com.example.xiaoy.R
import com.example.xiaoy.data.AppData
import com.google.gson.Gson
import java.io.File

/** 收到每日提醒闹钟后发出本地通知 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 读取当前设置，若已关闭提醒则不打扰
        val data = try {
            val text = File(context.filesDir, "xiaoy_data.json").readText()
            Gson().fromJson(text, AppData::class.java)
        } catch (_: Exception) { null }
        if (data == null || !data.reminderEnabled) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(nm)

        val contentIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "小芽 · 睡前提醒"
        val text = "今天也来记一笔孩子的小事吧，把每一次长大温柔留下来。"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Android 13+ 需要通知权限
        if (Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            nm.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createChannel(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "每日提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "每晚提醒记录孩子的小事" }
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "xiaoy_reminder"
        const val NOTIFICATION_ID = 1001
    }
}
