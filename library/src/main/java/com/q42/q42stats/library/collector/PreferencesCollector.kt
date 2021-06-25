package com.q42.q42stats.library.collector

import android.content.Context
import android.os.Build
import com.q42.q42stats.library.util.DayTimeUtil
import java.io.Serializable

/** Collects System settings such as default locale */
object PreferencesCollector {

    fun collect(context: Context) = mutableMapOf<String, Serializable>().apply {
        val configuration = context.resources.configuration

        put(
            "isNightModeEnabled",
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                configuration.isNightModeActive
            } else {
                false
            }
        )
        put("daytime", DayTimeUtil.dayNight().serverValue)
    }
}