package dev.bansal.digitalguardian

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import dev.bansal.digitalguardian.data.local.Preferences
import dev.bansal.digitalguardian.receiver.DeviceOwnerReceiver
import dev.bansal.digitalguardian.service.MonitorAppUsageService
import dev.bansal.digitalguardian.ui.theme.ApplicationTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var deviceManger: DevicePolicyManager
    private lateinit var compName: ComponentName
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent()
        compName = DeviceOwnerReceiver.getComponentName(this)
        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        preferences = Preferences(this)
        checkAndRequestAdminAccess()
        startService(Intent(this, MonitorAppUsageService::class.java))
        viewModel.initializeUserAppInfoAndRestrictionPreferences(packageManager, preferences)
    }

    private fun setContent() {
        setContent {
            ApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(25.dp)
                    ) {
                        itemsIndexed(viewModel.appList) { index, appInfo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    modifier = Modifier.size(50.dp),
                                    bitmap = appInfo.logo.asImageBitmap(),
                                    contentDescription = null
                                )
                                Text(
                                    modifier = Modifier.weight(1F),
                                    text = appInfo.applicationLabel
                                )
                                Switch(
                                    checked = appInfo.isRestricted.value,
                                    onCheckedChange = {
                                        viewModel.updateRestrictedAppList(appInfo.packageName, it, preferences)
                                    }
                                )
                            }
                            Divider()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MonitorAppUsageService::class.java).apply {
            action = MonitorAppUsageService.ACTION_START_DEVICE_MONITORING
        }
        startService(intent)
    }

    private fun checkAndRequestAdminAccess() {
        if (deviceManger.isAdminActive(compName)) {
            Timber.e("The app is the device admin.")
        } else {
            Timber.e("The app is not the device admin.")
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "The data will not be stored on our servers," +
                            " nor will be shared with anyone except parent!"
                )
            }
            startActivity(intent)
        }
    }
}

