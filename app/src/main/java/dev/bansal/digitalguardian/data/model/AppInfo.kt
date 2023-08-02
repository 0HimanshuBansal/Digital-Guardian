package dev.bansal.digitalguardian.data.model

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState

data class AppInfo(
    val applicationLabel: String,
    val packageName: String,
    val logo: Bitmap,
    var isRestricted: MutableState<Boolean>
)
