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
import com.q42.q42stats.library.Q42StatsLogger
import com.q42.q42stats.library.TAG
import java.io.Serializable
import java.util.*

/** Collects Accessibility-related settings and preferences, such as font scaling */
internal object AccessibilityCollector {

    fun collect(context: Context) = mutableMapOf<String, Serializable>().apply {
        val accessibilityManager =
            context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val configuration = context.resources.configuration

        val serviceNamesLower = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).mapNotNull {
            it.resolveInfo?.serviceInfo?.name?.lowercase(Locale.ROOT)
        }

        put("isAccessibilityManagerEnabled", accessibilityManager.isEnabled)
        put("isTouchExplorationEnabled", accessibilityManager.isTouchExplorationEnabled)
        put(
            "isTalkBackEnabled",
            // match the service name specifically, as talkback packages might contain other services
            serviceNamesLower.any { it.contains("talkbackservice") }
        )
        put(
            "isSamsungTalkBackEnabled",
            serviceNamesLower.any { it == "com.samsung.android.app.talkback.talkbackservice" }
        )
        put(
            "isSelectToSpeakEnabled",
            serviceNamesLower.any { it.contains("selecttospeak") }
        )
        put(
            "isSwitchAccessEnabled",
            serviceNamesLower.any { it.contains("switchaccess") }
        )
        put(
            "isBrailleBackEnabled",
            serviceNamesLower.any { it.contains("brailleback") }
        )
        put(
            "isVoiceAccessEnabled",
            serviceNamesLower.any { it.contains("voiceaccess", ignoreCase = true) })
        put("fontScale", configuration.fontScale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            put(
                "displayScale",
                context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEVICE_STABLE.toDouble()
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isClosedCaptioningEnabled(context)?.let {
                put(
                    "isClosedCaptioningEnabled",
                    it
                )
            }
        }

        put("enabledAccessibilityServices", serviceNamesLower.toString())

        put(
            "screenOrientation",
            configuration.orientation.let { intValue ->
                when (intValue) {
                    ORIENTATION_LANDSCAPE -> "landscape"
                    ORIENTATION_PORTRAIT -> "portrait"
                    else -> "unknown"
                }
            })


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSystemIntAsBool(
                context,
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED
            )?.let {
                put(
                    "isColorInversionEnabled",
                    it
                )
            }
        }

        getSystemIntAsBool(context, "accessibility_display_daltonizer_enabled")?.let {
            put(
                "isColorBlindModeEnabled",
                it
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun isClosedCaptioningEnabled(context: Context): Boolean? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.getSystemService(CAPTIONING_SERVICE) as CaptioningManager).isEnabled
        } else {
            // KitKat
            getSystemIntAsBool(context, "accessibility_captioning_enabled")
        }

    /**
     * @return null when the value could not be read
     */
    private fun getSystemIntAsBool(context: Context, name: String): Boolean? = try {
        val notFoundValue = -9001
        val value = Settings.Secure.getInt(
            context.contentResolver,
            name,
            notFoundValue
        )
        if (value == notFoundValue) (null) else value == 1
    } catch (e: Throwable) {
        Q42StatsLogger.e(TAG, "Could not read system int $name. Returning null", e)
        null
    }
}
