package com.q42.q42stats.library

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

private const val SHARED_PREFS_NAME = "Q42StatsPrefs"
private const val LAST_SUBMIT_TIMESTAMP_KEY = "lastSubmitTimestamp"

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

private const val INSTALLATION_ID_KEY = "installationId"
private const val PREVIOUS_MEASUREMENT_KEY = "previousMeasurement"
