package com.q42.q42stats.library.util

import org.json.JSONArray
import org.json.JSONObject

internal fun serializeMeasurement(value: Map<String, Any?>) =
    JSONObject(value).toString()

internal fun deserializeMeasurement(it: String): Map<String, Any?> =
    JSONObject(it).toMap()

private fun JSONObject.toMap(): Map<String, Any?> = keys().asSequence().associateWith {
    when (val value = this[it]) {
        is JSONArray -> {
            val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
            JSONObject(map).toMap().values.toList()
        }
        is JSONObject -> value.toMap()
        JSONObject.NULL -> null
        else -> {
            value
        }
    }
}
