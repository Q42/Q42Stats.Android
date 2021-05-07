package com.q42.q42stats.library

import android.content.Context
import android.util.Log
import com.q42.q42stats.library.collector.AccessibilityCollector
import com.q42.q42stats.library.collector.SystemCollector
import org.json.JSONObject
import java.io.Serializable

const val TAG = "Q42Stats"

class Q42Stats {
    val name = "Q42 Stats lib"

    /* Collects stats and sends it to the server */
    fun run(context: Context) {
        try {
            val collected = collect(context)
            HttpService.sendStats(JSONObject(collected as Map<*, *>))
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