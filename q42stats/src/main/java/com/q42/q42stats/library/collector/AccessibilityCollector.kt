package com.q42.q42stats.library.collector

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.TargetApi
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.Context.CAPTIONING_SERVICE
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.CaptioningManager
import java.io.Serializable
import java.util.*

/** Collects Accessibility-related settings and preferences, such as font scaling */
internal object AccessibilityCollector {

    fun collect(context: Context) = mutableMapOf<String, Serializable>().apply {
        val accessibilityManager =
            context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val configuration = context.resources.configuration

        val services = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).mapNotNull {
            it.resolveInfo?.serviceInfo?.name?.toLowerCase(Locale.ROOT)
        }

        put("isAccessibilityManagerEnabled", accessibilityManager.isEnabled)
        put("isTouchExplorationEnabled", accessibilityManager.isTouchExplorationEnabled)
        put("isTalkBackEnabled", services.any { it.contains("talkback") })
        put(
            "isSamsungTalkbackEnabled",
            services.any { it == "com.samsung.android.app.talkback.talkbackservice" }
        )
        put("isVoiceAccessEnabled", services.any { it.contains("voiceaccess") })
        put("fontScale", configuration.fontScale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            put(
                "displayScale",
                context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEVICE_STABLE.toDouble()
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            put(
                "isClosedCaptioningEnabled",
                isClosedCaptioningEnabled(context)
            )
        }

        put("enabledAccessibilityServices", services.toString())

        put(
            "screenOrientation",
            configuration.orientation.let { intValue ->
                when (intValue) {
                    ORIENTATION_LANDSCAPE -> "landscape"
                    ORIENTATION_PORTRAIT -> "portrait"
                    else -> "unknown"
                }
            })
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun isClosedCaptioningEnabled(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.getSystemService(CAPTIONING_SERVICE) as CaptioningManager).isEnabled
        } else {
            // KitKat
            Settings.Secure.getInt(
                context.contentResolver,
                "accessibility_captioning_enabled",
                0
            ) == 1
        }
}
