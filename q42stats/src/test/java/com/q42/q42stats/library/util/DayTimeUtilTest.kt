package com.q42.q42stats.library.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

private const val dayTimeMillis = 1624622400 * 1000L // Amsterdam june 25 2021 14:00
private const val twilightTimeMillis = 1624647600 * 1000L // Amsterdam june 25 2021 21:00
private const val nightTimeMillis = 1624579200 * 1000L // Amsterdam june 25 2021 02:00

class DayTimeUtilTest {

    private val amsterdam = TimeZone.getTimeZone("Europe/Amsterdam")

    @Test
    fun dayNight() {
        assertEquals(
            "Claims to know daytime for other TZ than Amsterdam",
            DayTime.Unknown,
            DayTimeUtil.dayNight(Date(nightTimeMillis), TimeZone.getTimeZone("UTC"))
        )

        assertEquals(
            DayTime.Night,
            DayTimeUtil.dayNight(Date(nightTimeMillis), amsterdam)
        )

        assertEquals(
            DayTime.Day,
            DayTimeUtil.dayNight(Date(dayTimeMillis), amsterdam)
        )

        assertEquals(
            DayTime.Twilight,
            DayTimeUtil.dayNight(Date(twilightTimeMillis), amsterdam)
        )


    }
}