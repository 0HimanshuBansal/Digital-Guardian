package dev.bansal.digitalguardian.data.local

import android.content.Context

class Preferences(context: Context) {

    companion object {

        var isUserPreferenceUpdated = false
        private const val DEFAULT_PREFERENCES = "default_preferences"
        private const val RESTRICTED_PACKAGES = "restricted_packages"
    }

    private var sharedPreferences =
        context.getSharedPreferences(DEFAULT_PREFERENCES, Context.MODE_PRIVATE)

    fun saveRestrictedPackages(restrictedPackages: Set<String>) {
        isUserPreferenceUpdated = true
        with(sharedPreferences.edit()) {
            putStringSet(RESTRICTED_PACKAGES, restrictedPackages)
            apply()
        }
    }

    fun getRestrictedPackages(): Set<String>? =
        sharedPreferences.getStringSet(RESTRICTED_PACKAGES, null)

}
