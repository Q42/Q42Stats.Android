package com.q42.q42stats.library

internal class Q42StatsException(message: String, cause: Throwable) :
    Throwable("$message: ${cause.message}", cause)