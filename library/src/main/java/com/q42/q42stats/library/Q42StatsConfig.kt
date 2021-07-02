package com.q42.q42stats.library

data class Q42StatsConfig(
    val fireBaseProject: String,
    val firebaseCollection: String,
    /** Data collection is skipped when less than this many seconds have passed
     * since the previous run */
    val minimumSubmitInterval: Long
)