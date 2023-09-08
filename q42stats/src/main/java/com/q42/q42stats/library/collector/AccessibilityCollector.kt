package com.q42.q42stats.library.collector

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.Context.CAPTIONING_SERVICE
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.CaptioningManager
import androidx.annotation.RequiresApi
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
        put(
            "fontScale",
            configuration.fontScale
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            put("fontWeightAdjustment", configuration.fontWeightAdjustment)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isMagnificationEnabled(context, serviceNamesLower)?.let {
                put("isMagnificationEnabled", it)
            }
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            put(
                "isAnimationsDisabled",
                isAnimationsDisabled(context)
            )
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

        getScreenSize(context)?.let {
            put(
                "screenWidth",
                it
            )
        }

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

        getSystemIntAsBool(context, "high_text_contrast_enabled")?.let {
            put(
                "isHighTextContrastEnabled",
                it
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun isClosedCaptioningEnabled(context: Context): Boolean? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.getSystemService(CAPTIONING_SERVICE) as CaptioningManager).isEnabled
        } else {
            // KitKat
            getSystemIntAsBool(context, "accessibility_captioning_enabled")
        }

    /**
     * This is a best-effort means of checking whether magnification is enabled or not. It involves checking by which
     * method the user can toggle magnification. Ideally, we want to read MagnificationController for this check, but this would
     * require creating an AccessibilityService together with necessary permissions which this library should certainly not do.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun isMagnificationEnabled(context: Context, serviceNames: List<String>): Boolean? = try {
        val isMagnificationByTripleTapGesturesEnabled = getSystemIntAsBool(context, "accessibility_display_magnification_enabled") ?: false
        val isMagnificationByVolumeButtonsEnabled = serviceNames.map { s -> s.lowercase() }.contains("com.example.android.apis.accessibility.magnificationservice")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val isMagnificationByNavigationButtonEnabled =
                Settings.Secure.getString(context.contentResolver, "accessibility_button_targets").lowercase().contains("com.android.server.accessibility.magnificationcontroller")

            isMagnificationByTripleTapGesturesEnabled || isMagnificationByVolumeButtonsEnabled || isMagnificationByNavigationButtonEnabled
        } else {
            isMagnificationByTripleTapGesturesEnabled || isMagnificationByVolumeButtonsEnabled
        }
    } catch (e: Throwable) {
        Q42StatsLogger.w(TAG, "Could not read magnification, user likely has never used magnification before. Returning null.")
        null
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun isAnimationsDisabled(context: Context): Boolean =
        (Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f) == 0f
                && Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1.0f) == 0f
                && Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1.0f) == 0f)

    /**
     * @return null when the value could not be read
     */
    private fun getSystemIntAsBool(context: Context, name: String): Boolean? = try {
        Settings.Secure.getInt(
            context.contentResolver,
            name,
            0
        ) == 1
    } catch (e: Throwable) {
        Q42StatsLogger.e(TAG, "Could not read system int $name. Returning null", e)
        null
    }

    /**
     * Gets the screen size in density independent pixels with portrait orientation.
     *
     * Note: calculates size given portrait mode.
     */
    private fun getScreenSize(context: Context): Int? {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val resources = context.resources

            // get screen size in pixels
            val pixelScreenSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager.currentWindowMetrics
                with(windowMetrics.bounds) {
                    Pair(right, bottom)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                val point = Point()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealSize(point)
                Pair(point.x, point.y)
            } else {
                with(resources.displayMetrics) {
                    Pair(widthPixels, heightPixels)
                }
            }

            // get screen size in portrait mode
            val portraitScreenSize = with(pixelScreenSize) {
                if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
                    second
                } else {
                    first
                }
            }

            val portraitDpScreenSize = with(portraitScreenSize) {
                val density = resources.displayMetrics.density
                (this / density).toInt()
            }
            portraitDpScreenSize
        } catch (e: Throwable) {
            Q42StatsLogger.e(TAG, "Could not read screen size. Returning null", e)
            null
        }
    }
}
