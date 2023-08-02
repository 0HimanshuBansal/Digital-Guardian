package dev.bansal.digitalguardian.receiver

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.Settings
import dev.bansal.digitalguardian.MainActivity
import dev.bansal.digitalguardian.service.DigitalGuardianAccessibilityService
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceOwnerReceiver : DeviceAdminReceiver() {

//    private lateinit var devicePolicyManager: DevicePolicyManager
//    private lateinit var timerThread: Timer
//    private var startTime = 0L

    override fun onReceive(context: Context, intent: Intent) {
        Timber.e("DeviceOwnerReceiver: $intent")
        val action = intent.action
        if (ACTION_DEVICE_ADMIN_DISABLE_REQUESTED == action) {
            val res = onDisableRequested(context, intent)
            val extras = getResultExtras(true)
            extras.putCharSequence(EXTRA_DISABLE_WARNING, res)
        } else if (ACTION_DEVICE_ADMIN_DISABLED == action) {
            Timber.e("Disabled")
        }
    }

    //When user moves too
    //Settings > Apps > Special Access apps > Device Admins
    //And tries to disable admin access
    //A confirmation Dialog wil be presented
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        Timber.e("onDisableRequested: $intent")
        DigitalGuardianAccessibilityService.instance?.goBack()
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(1000)
//            val startMain = Intent(Settings.ACTION_SETTINGS)
//            startMain.setFlags(FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(startMain)
//        }
        return "Are you sure?"
    }

//    override fun onDisabled(context: Context, intent: Intent) {
//        Timber.e("onDisabled: $intent")
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(6000)
//            context.startActivity(
//                Intent(context, MainActivity::class.java)
//                    .addFlags(FLAG_ACTIVITY_NEW_TASK)
//                    .addFlags(FLAG_ACTIVITY_CLEAR_TOP)
//            )
//        }
//        super.onDisabled(context, intent)
//    }

    companion object {

        /**
         * @return A newly instantiated [android.content.ComponentName] for this
         * DeviceAdminReceiver.
         */
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, DeviceOwnerReceiver::class.java)
        }
    }
}