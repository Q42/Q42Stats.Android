package com.q42.q42stats.library

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

private const val SHARED_PREFS_NAME = "Q42StatsPrefs"
private const val LAST_SUBMIT_TIMESTAMP_KEY = "lastSubmitTimestamp"
private const val PREVIOUS_MEASUREMENT_KEY = "previousMeasurement"
private const val LAST_BATCH_ID_KEY = "lastBatchId"

internal class Q42StatsPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    var previousMeasurement: Map<String, Any?>?
        get() =
            prefs.getString(PREVIOUS_MEASUREMENT_KEY, null)?.let {
                JSONObject(it).toMap()
            }
        set(value) = with(prefs.edit()) {
            value?.let {
                putString(PREVIOUS_MEASUREMENT_KEY, JSONObject(value).toString())
            } ?: run {
                remove(PREVIOUS_MEASUREMENT_KEY)
            }
            apply()
        }

    var lastBatchId: String?
        get() = prefs.getString(LAST_BATCH_ID_KEY, null)
        set(value) = with(prefs.edit()) {
            value?.let {
                putString(LAST_BATCH_ID_KEY, it)
            } ?: run {
                remove(LAST_BATCH_ID_KEY)
            }
            apply()
        }

    fun withinSubmitInterval(interval: Long) =
        System.currentTimeMillis() <
                prefs.getLong(LAST_SUBMIT_TIMESTAMP_KEY, 0) + interval

    fun updateSubmitTimestamp() = with(prefs.edit()) {
        putLong(LAST_SUBMIT_TIMESTAMP_KEY, System.currentTimeMillis())
        apply()
    }

    private fun JSONObject.toMap(): Map<String, Any?> = keys().asSequence().associateWith {
        when (val value = this[it]) {
            is JSONArray -> {
                val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
                JSONObject(map).toMap().values.toList()
            }
            is JSONObject -> value.toMap()
            JSONObject.NULL -> null
            else -> value
        }
    }

}
