package com.q42.q42stats.library.util

import org.jetbrains.annotations.TestOnly
import java.util.*
import kotlin.math.abs

object DayTimeUtil {

    // Real hacky function for determening day/night/twilight
    // With very wide margin (3 hours), because we don't know how
    // Android implements this for Dark mode switch
    // Source: https://github.com/Q42/Q42Stats/blob/master/Sources/Q42Stats/Q42Stats.swift
    fun dayNight(): DayTime =
        dayNight(Date(), TimeZone.getDefault())

    @TestOnly
    fun dayNight(date: Date, tz: TimeZone): DayTime {
        if (tz.id != "Europe/Amsterdam") {
            return DayTime.Unknown
        }
        val cal = Calendar.getInstance(tz).apply {
            time = date
        }
        val month = cal.get(Calendar.MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        // Hacky hardcoded table for sun raise/set in Amsterdam
        val sunRaiseSetHours = listOf(
            Pair(8, 17), // jan
            Pair(7, 18), // feb
            Pair(7, 19), // mar
            Pair(6, 20), // apr
            Pair(5, 21), // may
            Pair(5, 22), // jun
            Pair(5, 21), // jul
            Pair(6, 20), // aug
            Pair(7, 19), // sep
            Pair(7, 18), // oct
            Pair(8, 17), // nov
            Pair(8, 16), // dec
        )

        val (raise, set) = sunRaiseSetHours[month]
        val twilight = abs(hour - raise) < 2 || abs(hour - set) < 2
        val daytime = hour in (raise + 1) until set
        return when {
            twilight -> DayTime.Twilight
            daytime -> DayTime.Day
            else -> DayTime.Night
        }
    }

}

enum class DayTime(val serverValue: String) {
    Unknown("unknown"),
    Twilight("twilight"),
    Day("day"),
    Night("night");
}