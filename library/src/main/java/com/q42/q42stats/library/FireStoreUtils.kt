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
fun Map<String, Serializable>.toFireStoreFormat(): JSONObject {
    val fireStoreMap = mapOf(
        "fields" to this.mapValues {
            mapOf("stringValue" to it.value.toString())
        }
    )
    return JSONObject(fireStoreMap)
}