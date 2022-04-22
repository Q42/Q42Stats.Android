package com.q42.q42stats.library

import org.json.JSONObject
import java.io.Serializable

internal fun Map<String, Any>.toQ42StatsApiFormat(): JSONObject {
    val fireStoreMap = this.mapValues { entry ->
        mapFieldValue(entry)
    }
    return JSONObject(fireStoreMap)
}

@Suppress("UNCHECKED_CAST")
private fun mapFieldValue(entry: Map.Entry<String, Any>): Any =
    ((entry.value as? Map<String, Serializable>)
        ?.toQ42StatsApiFormat() // if this is a map, recurse
        ?: entry.value.toString()) // else, stringify the value
