package dev.bansal.digitalguardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.bansal.digitalguardian.service.MonitorAppUsageService
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val mIntent = Intent(context, MonitorAppUsageService::class.java).apply {
                action = MonitorAppUsageService.ACTION_START_DEVICE_MONITORING
            }
            context.startService(mIntent)
            Timber.e("ACTION_BOOT_COMPLETED: $intent")
        }
    }
}
