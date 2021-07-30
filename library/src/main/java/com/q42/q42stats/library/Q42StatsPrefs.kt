package com.q42.q42stats.library

import android.content.Context
import android.content.SharedPreferences
import java.util.*

private const val SHARED_PREFS_NAME = "Q42StatsPrefs"
private const val LAST_SUBMIT_TIMESTAMP_KEY = "lastSubmitTimestamp"
private const val INSTALLATION_ID_KEY = "installationId"

class Q42StatsPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrCreateInstallationId(): String {
        val uuid = prefs.getString(INSTALLATION_ID_KEY, null)
        return uuid ?: createInstallationId()
    }

    fun withinSubmitInterval(interval: Long) =
        System.currentTimeMillis() <
                prefs.getLong(LAST_SUBMIT_TIMESTAMP_KEY, 0) + interval

    fun updateSubmitTimestamp() = with(prefs.edit()) {
        putLong(LAST_SUBMIT_TIMESTAMP_KEY, System.currentTimeMillis())
        apply()
    }

    private fun createInstallationId(): String = with(prefs.edit()) {
        val uuid = UUID.randomUUID().toString()
        putString(INSTALLATION_ID_KEY, uuid)
        apply()
        return uuid
    }

}