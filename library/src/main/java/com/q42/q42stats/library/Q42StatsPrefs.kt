package com.q42.q42stats.library

import android.content.Context
import android.content.SharedPreferences

private const val SHARED_PREFS_NAME = "Q42StatsPrefs"
private const val LAST_SUBMIT_TIMESTAMP_KEY = "lastSubmitTimestamp"

class Q42StatsPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    fun withinSubmitInterval(interval: Long) =
        System.currentTimeMillis() <
                prefs.getLong(LAST_SUBMIT_TIMESTAMP_KEY, 0) + interval

    fun updateSubmitTimestamp() {
        prefs.edit().apply() {
            putLong(LAST_SUBMIT_TIMESTAMP_KEY, System.currentTimeMillis())
            apply()
        }
    }

}