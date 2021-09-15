package com.q42.q42stats.library

import org.junit.Assert.assertEquals
import org.junit.Test

class FireStoreUtilsTest {
    @Test
    fun testToFireStoreFormat() {
        val expected =
            """{"fields":{"screen width":{"stringValue":"360"},"textScale":{"stringValue":"1.6"},"language":{"stringValue":"nl"},"talkbackEnabled":{"stringValue":"true"}}}"""
        val actual = mapOf(
            "screen width" to 360,
            "textScale" to 1.6,
            "talkbackEnabled" to true,
            "language" to "nl"
        ).toFireStoreFormat().toString()
        assertEquals(expected, actual)
    }
}