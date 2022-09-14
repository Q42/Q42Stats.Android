package com.q42.q42stats.library

import org.junit.Test

class ApiFormatUtilTest {
    @Test
    fun testToQ42StatsApiFormat() {
        val actual = mapOf(
            "screen width" to 360,
            "fontSize" to 1.0, // expected is "1.0". should not be truncated to "1"
            "textScale" to 1.6,
            "talkbackEnabled" to true,
            "language" to "nl",
            "currentMeasurement" to mapOf(
                "createdAt" to 1337L
            )
        ).toQ42StatsApiFormat()

        testQ42StatsMap(actual)
    }

    private fun testQ42StatsMap(actual: Map<*, *>) {
        actual.forEach {
            when (it.value) {
                is Map<*, *> -> {
                    testQ42StatsMap(it.value as Map<*, *>)
                }
                else -> {
                    assert(it.value is String) { "Expected $it to have a string type but was ${it.value}" }
                }
            }
        }
    }
}
