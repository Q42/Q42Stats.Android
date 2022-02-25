package com.q42.q42stats.library

import org.json.JSONObject
import java.io.Serializable

/**
 * Transforms a Map to a JsonObject with the following structure:
 * ```
 *     {
 *       "fields": {
 *          "Screen window width": {
 *              "stringValue": "390"
 *          },
 *         [..]
 *     }
 * ```
 */
internal fun Map<String, Any>.toFireStoreFormat(): JSONObject {
    val fireStoreMap = mapOf(
        "fields" to this.mapValues { entry ->
            mapFieldValue(entry)
        }
    )
    return JSONObject(fireStoreMap)
}

@Suppress("UNCHECKED_CAST")
private fun mapFieldValue(entry: Map.Entry<String, Any>): Map<String, Any> =
    ((entry.value as? Map<String, Serializable>) // is the value a Map?
        ?.let {
            mapOf("mapValue" to it.toFireStoreFormat())
        }
        ?: mapOf("stringValue" to entry.value.toString()))
