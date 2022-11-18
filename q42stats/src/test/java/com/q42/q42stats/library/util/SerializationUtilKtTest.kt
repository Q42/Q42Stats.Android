package com.q42.q42stats.library.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SerializationUtilKtTest {

    @Test
    fun `Deserializing a serialized map results in an equal Map`() {
        val expected = mapOf(
            "screen width" to "360",
            "fontSize" to "1.0", // expected is "1.0". should not be truncated to "1"
            "numWheels" to "1",
            "talkbackEnabled" to "true",
            "language" to "nl",
            "currentMeasurement" to mapOf(
                "createdAt" to "1337L"
            )
        )
        val serialized = serializeMeasurement(expected)
        val actual = deserializeMeasurement(serialized)
        assertEquals(expected, actual)
    }
}
