package com.q42.q42stats.library.collector

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.Context.CAPTIONING_SERVICE
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Build
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.CaptioningManager
import java.io.Serializable

/** Collects Accessibility-related settings and preferences, such as font scaling */
object AccessibilityCollector {

    fun collect(context: Context) = mutableMapOf<String, Serializable>().apply {
        val accessibilityManager =
            context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val res = context.resources

        val services = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).map {
            it.packageNames
        }

        put("isAccessibilityManagerEnabled", accessibilityManager.isEnabled)
        put("isTouchExplorationEnabled", accessibilityManager.isTouchExplorationEnabled)
        put("fontScale", res.configuration.fontScale)

        put(
            "isClosedCaptioningEnabled",
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                (context.getSystemService(CAPTIONING_SERVICE) as CaptioningManager).isEnabled
            } else {
                false
            }
        )

        put("enabledAccessibilityServices", services.toString())

        put(
            "screenOrientation",
            res.configuration.orientation.let { intValue ->
                when (intValue) {
                    ORIENTATION_LANDSCAPE -> "landscape"
                    ORIENTATION_PORTRAIT -> "portrait"
                    else -> "unknown"
                }
            })
    }
}