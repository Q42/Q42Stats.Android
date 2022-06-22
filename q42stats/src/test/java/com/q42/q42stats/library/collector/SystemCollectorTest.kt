package com.q42.q42stats.library.collector

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class SystemCollectorTest {

    @Test
    fun getLocaleString() {
        assertEquals("en-US", SystemCollector.getLocaleString(Locale.US))
        assertEquals("nl-NL", SystemCollector.getLocaleString(Locale("nl", "NL")))
        assertEquals("nl-BE", SystemCollector.getLocaleString(Locale("nl", "BE")))
        assertEquals("nl", SystemCollector.getLocaleString(Locale("nl")))
    }
}
