package dev.bansal.digitalguardian.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.bansal.digitalguardian.MainActivity
import dev.bansal.digitalguardian.R
import dev.bansal.digitalguardian.utils.NotificationUtils.USAGE_ACCESS_MONITOR_CHANNEL_ID
import dev.bansal.digitalguardian.utils.NotificationUtils.USAGE_ACCESS_MONITOR_CHANNEL_NAME

fun createUsageAccessMonitorNotificationChannel(context: Context) {
    val notificationChannel = NotificationChannel(
        USAGE_ACCESS_MONITOR_CHANNEL_ID,
        USAGE_ACCESS_MONITOR_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_LOW
    )

    notificationChannel.enableLights(true)
    notificationChannel.setShowBadge(false)
    notificationChannel.lightColor = Color.BLUE
    notificationChannel.description = "notification_channel_description"

    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(notificationChannel)
}

fun getContentPendingIntent(context: Context): PendingIntent =
    PendingIntent.getActivity(
        context,
        123,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

fun getUsageAccessMonitorNotification(context: Context): Notification {
    val activityIntent = Intent(context.applicationContext, MainActivity::class.java)
    activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

    val pendingIntent = PendingIntent.getActivity(
        context.applicationContext, 0, activityIntent,
        if (Build.VERSION.SDK_INT >= 30) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
    )

    val builder = NotificationCompat.Builder(context, USAGE_ACCESS_MONITOR_CHANNEL_ID)
        .setContentTitle("Monitoring")
        .setContentText("checking...")
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setSmallIcon(R.drawable.ic_notification)
        .setSilent(true)
        .setContentIntent(pendingIntent)
    return builder.build()
}

object NotificationUtils {

    const val USAGE_ACCESS_MONITOR_CHANNEL_ID = "usage_access_monitor_channel_id"
    const val USAGE_ACCESS_MONITOR_CHANNEL_NAME = "Usage access monitoring"
    const val USAGE_ACCESS_MONITOR_NOTIFICATION_ID = 1579
}
