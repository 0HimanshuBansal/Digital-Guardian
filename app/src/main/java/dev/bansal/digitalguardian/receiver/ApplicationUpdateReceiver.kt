package dev.bansal.digitalguardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.bansal.digitalguardian.service.MonitorAppUsageService
import timber.log.Timber

class ApplicationUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val mIntent = Intent(context, MonitorAppUsageService::class.java).apply {
                action = MonitorAppUsageService.ACTION_START_DEVICE_MONITORING
            }
            context.startService(mIntent)
            Timber.e("ACTION_MY_PACKAGE_REPLACED: $intent")
        }
    }
}
