package com.q42.q42stats.library.util

/**
 * Returns a map containing all entries whose values are not null
 */
@Suppress("UNCHECKED_CAST")
fun <K, V : Any> Map<K, V?>.filterValueNotNull(): Map<K, V> =
    filterValues { it != null } as Map<K, V>
