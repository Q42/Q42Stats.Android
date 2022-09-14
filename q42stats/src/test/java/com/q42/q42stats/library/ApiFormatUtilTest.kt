package com.q42.q42stats.library

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiFormatUtilTest {
    @Test
    fun testToQ42StatsApiFormat() {
        val expected =
            """{"screen width":"360","fontSize":"1.0","textScale":"1.6","language":"nl","currentMeasurement":{"createdAt":"1337"},"talkbackEnabled":"true"}"""
        val actual = mapOf(
            "screen width" to 360,
            "fontSize" to 1.0, // expected is "1.0". should not be truncated to "1"
            "textScale" to 1.6,
            "talkbackEnabled" to true,
            "language" to "nl",
            "currentMeasurement" to mapOf(
                "createdAt" to 1337L
            )
        ).toQ42StatsApiFormat().toString()
        assertEquals(expected, actual)
    }
}
