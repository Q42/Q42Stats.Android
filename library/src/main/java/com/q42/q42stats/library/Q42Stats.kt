package com.q42.q42stats.library

import android.content.Context
import android.util.Log
import com.q42.q42stats.library.collector.AccessibilityCollector
import com.q42.q42stats.library.collector.SystemCollector
import org.json.JSONObject
import java.io.Serializable

const val TAG = "Q42Stats"
const val SUBMIT_INTERVAL_MILLIS = 60 * 60 * 24 * 1000L

class Q42Stats {
    val name = "Q42 Stats lib"

    /* Collects stats and sends it to the server */
    fun run(context: Context) {
        try {
            val prefs = Q42StatsPrefs(context)
            if (prefs.withinSubmitInterval(SUBMIT_INTERVAL_MILLIS) && !BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "Q42Stats were already sent in the last ${SUBMIT_INTERVAL_MILLIS / 1000} seconds. Exit. "
                )
                return
            }
            val collected = collect(context)
            HttpService.sendStats(JSONObject(collected as Map<*, *>))
            prefs.updateSubmitTimestamp()

        } catch (e: Throwable) {
            Log.e(TAG, "Q42Stats encountered an error", e)
            if (BuildConfig.DEBUG) {
                throw e
            }
        }
    }

    private fun collect(context: Context): MutableMap<String, Serializable> {
        val collected = mutableMapOf<String, Serializable>()
        collected += AccessibilityCollector.collect(context)
        collected += SystemCollector.collect()
        return collected
    }
}