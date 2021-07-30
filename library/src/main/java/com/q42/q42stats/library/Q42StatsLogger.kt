package com.q42.q42stats.library

import android.util.Log

internal object Q42StatsLogger {
    /** logs with lower importance will be ignored */
    var logLevel = if (BuildConfig.DEBUG) Q42StatsLogLevel.Verbose else Q42StatsLogLevel.Error

    fun v(tag: String, message: String) {
        if (logLevel <= Q42StatsLogLevel.Verbose) {
            Log.v(tag, message)
        }
    }

    fun d(tag: String, message: String) {
        if (logLevel <= Q42StatsLogLevel.Debug) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (logLevel <= Q42StatsLogLevel.Info) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String, message: String) {
        if (logLevel <= Q42StatsLogLevel.Warn) {
            Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String, e: Throwable? = null) {
        if (logLevel <= Q42StatsLogLevel.Error) {
            Log.e(tag, "$message: ${e?.message}", e)
        }
    }
}

enum class Q42StatsLogLevel {
    //Log levels in order of importance
    Verbose,
    Debug,
    Info,
    Warn,
    Error;
}