package dev.bansal.digitalguardian

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.bansal.digitalguardian.data.local.Preferences
import dev.bansal.digitalguardian.data.model.AppInfo
import dev.bansal.digitalguardian.utils.toBitmap

class MainViewModel : ViewModel() {

    val appList = mutableStateListOf<AppInfo>()
    private val restrictedApplications = mutableSetOf<String>()

    fun updateRestrictedAppList(
        packageName: String,
        shouldRestrict: Boolean,
        preferences: Preferences
    ) {
        if (shouldRestrict) {
            appList.find { it.packageName == packageName }?.isRestricted?.value = true
            restrictedApplications.add(packageName)
        } else {
            appList.find { it.packageName == packageName }?.isRestricted?.value = false
            restrictedApplications.remove(packageName)
        }
        preferences.saveRestrictedPackages(restrictedApplications)
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun initializeUserAppInfoAndRestrictionPreferences(
        packageManager: PackageManager,
        preferences: Preferences
    ) {
        preferences.getRestrictedPackages()?.let { restrictedApplications.addAll(it) }
        val applicationsInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        val userAppInfoList = arrayListOf<AppInfo>()
        applicationsInfo
            .filter {
                it.packageName == "com.android.settings" ||
                (it.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                        it.packageName != BuildConfig.APPLICATION_ID)
            }
            .sortedBy { packageManager.getApplicationLabel(it).toString() }
            .map {
                userAppInfoList.add(
                    AppInfo(
                        applicationLabel = it.loadLabel(packageManager).toString(),
                        packageName = it.packageName,
                        logo = packageManager.getApplicationIcon(it.packageName).toBitmap(),
                        isRestricted = mutableStateOf(restrictedApplications.contains(it.packageName))
                    )
                )
            }
        appList.clear()
        appList.addAll(userAppInfoList)
    }
}
