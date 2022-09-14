package com.q42.q42stats.library

import android.content.Context
import android.content.SharedPreferences

private const val SHARED_PREFS_NAME = "Q42StatsPrefs"
private const val LAST_SUBMIT_TIMESTAMP_KEY = "lastSubmitTimestamp"
private const val PREVIOUS_MEASUREMENT_KEY = "previousMeasurement"
private const val LAST_BATCH_ID_KEY = "lastBatchId"

internal class Q42StatsPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    var previousMeasurement: String?
        get() =
            prefs.getString(PREVIOUS_MEASUREMENT_KEY, null)
        set(value) = with(prefs.edit()) {
            value?.let {
                putString(PREVIOUS_MEASUREMENT_KEY, value)
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

}
