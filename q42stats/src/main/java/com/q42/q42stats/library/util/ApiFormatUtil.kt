package com.q42.q42stats.library

import java.io.Serializable

/** Returns a version of the input where all values are stringified
 *  We do this to prevent serialize -> deserialize issues where the float 1.0 is transformed to 1,
 *  for example. (this change is undesirable because the changed data type causes the object to have
 *  a different hash, not being equal in comparisons etc.
 */
internal fun Map<String, Any>.toQ42StatsApiFormat(): Map<String, Any> {
    val q42StatsMap = this.mapValues { entry ->
        mapFieldValue(entry)
    }
    return q42StatsMap
}

@Suppress("UNCHECKED_CAST")
private fun mapFieldValue(entry: Map.Entry<String, Any>): Any =
    ((entry.value as? Map<String, Serializable>)
        ?.toQ42StatsApiFormat() // if this is a map, recurse
        ?: entry.value.toString()) // else, stringify the value
