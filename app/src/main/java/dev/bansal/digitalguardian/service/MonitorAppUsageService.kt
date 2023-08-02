package dev.bansal.digitalguardian.service

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
import android.os.Build
import android.os.IBinder
import dev.bansal.digitalguardian.MainActivity
import dev.bansal.digitalguardian.data.local.Preferences
import dev.bansal.digitalguardian.utils.NotificationUtils.USAGE_ACCESS_MONITOR_NOTIFICATION_ID
import dev.bansal.digitalguardian.utils.createUsageAccessMonitorNotificationChannel
import dev.bansal.digitalguardian.utils.getUsageAccessMonitorNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class MonitorAppUsageService : Service() {

    private var isServiceRunning = false
    private lateinit var usageStatsManager: UsageStatsManager
    private val TOLERANCE = 500L
    private lateinit var preferences: Preferences
    private val restrictedPackages = mutableSetOf<String>()

    override fun onCreate() {
        createUsageAccessMonitorNotificationChannel(this)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        preferences = Preferences(this)
        preferences.getRestrictedPackages()?.let { restrictedPackages.addAll(it) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DEVICE_MONITORING -> start()
            ACTION_STOP_DEVICE_MONITORING -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        if (isServiceRunning) return
        isServiceRunning = true

        monitorUsageAccess(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                USAGE_ACCESS_MONITOR_NOTIFICATION_ID,
                getUsageAccessMonitorNotification(this),
                FOREGROUND_SERVICE_TYPE_NONE
            )
        } else {
            startForeground(
                USAGE_ACCESS_MONITOR_NOTIFICATION_ID,
                getUsageAccessMonitorNotification(this)
            )
        }
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        isServiceRunning = false
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun monitorUsageAccess(intent: Intent) {
        //val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        CoroutineScope(Dispatchers.Main).launch {
            val time = System.currentTimeMillis()
            //val lUsageStatsMap =
            //usageStatsManager.queryAndAggregateUsageStats(time - 14 * 60 * 60 * 1000, time)
            //Timber.e("UsageStatsMap: ${lUsageStatsMap["com.android.settings"]?.totalTimeInForeground}")
            while (true) {
                val currentTime = System.currentTimeMillis()
                val usageEvents =
                    usageStatsManager.queryEvents(currentTime - TOLERANCE, currentTime + TOLERANCE)
                val usageEvent = UsageEvents.Event()
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(usageEvent)
                    if (
                        usageEvent.packageName == "com.google.android.packageinstaller"
                        ||
                        usageEvent.className == "com.android.settings.applications.InstalledAppDetailsTop"
                        ||
                        isItRestrictedApp(usageEvent.packageName)
                    ) {
                        Timber.e("startActivity: ${usageEvent.packageName}")
                        startActivity(intent)
                        //activityManager.killBackgroundProcesses(usageEvent.packageName)
                    }
                    if (usageEvent.className == "com.android.settings.applications.specialaccess.deviceadmin.DeviceAdminAdd") {
                        DigitalGuardianAccessibilityService.instance?.goBack()
                    }
                    val eventType = when (usageEvent.eventType) {
                        UsageEvents.Event.NONE -> "NONE"
                        UsageEvents.Event.ACTIVITY_PAUSED -> "ACTIVITY_PAUSED"
                        UsageEvents.Event.ACTIVITY_RESUMED -> "ACTIVITY_RESUMED"
                        UsageEvents.Event.CONFIGURATION_CHANGE -> "CONFIGURATION_CHANGE"
                        UsageEvents.Event.USER_INTERACTION -> "USER_INTERACTION"
                        UsageEvents.Event.STANDBY_BUCKET_CHANGED -> "STANDBY_BUCKET_CHANGED"
                        UsageEvents.Event.FOREGROUND_SERVICE_START -> "FOREGROUND_SERVICE_START"
                        UsageEvents.Event.FOREGROUND_SERVICE_STOP -> "FOREGROUND_SERVICE_STOP"
                        UsageEvents.Event.ACTIVITY_STOPPED -> "ACTIVITY_STOPPED"
                        else -> {
                            usageEvent.eventType.toString()
                        }
                    }
                    Timber.e("Package: ${usageEvent.packageName}, Event: $eventType")
                }
                //Timber.e("Interval")
                delay(500)
            }
        }
    }

    private fun isItRestrictedApp(packageName: String): Boolean {
        if (Preferences.isUserPreferenceUpdated) {
            preferences.getRestrictedPackages()?.let {
                restrictedPackages.clear()
                restrictedPackages.addAll(it)
            } ?: return false
            Preferences.isUserPreferenceUpdated = false
        }
        return restrictedPackages.contains(packageName)
    }

    companion object {

        const val ACTION_START_DEVICE_MONITORING = "ACTION_START_DEVICE_MONITORING"
        const val ACTION_STOP_DEVICE_MONITORING = "ACTION_STOP_DEVICE_MONITORING"
    }
}