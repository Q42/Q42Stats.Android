package com.q42.q42stats.library

data class Q42StatsConfig(
    val firestoreCollectionId: String,
    val apiKey: String,
    /** Data collection is skipped when less than this many seconds have passed
     * since the previous run */
    val minimumSubmitIntervalSeconds: Long
)
