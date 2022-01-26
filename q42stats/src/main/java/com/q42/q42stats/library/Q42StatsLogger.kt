package com.q42.q42stats.library

import android.util.Log

private const val TAG = "Q42Stats"

internal object Q42StatsLogger {
    /** logs with lower importance will be ignored */
    var logLevel = if (BuildConfig.DEBUG) Q42StatsLogLevel.Verbose else Q42StatsLogLevel.Error

    fun v(message: String) {
        if (logLevel <= Q42StatsLogLevel.Verbose) {
            Log.v(TAG, message)
        }
    }

    fun d(message: String) {
        if (logLevel <= Q42StatsLogLevel.Debug) {
            Log.d(TAG, message)
        }
    }

    fun i(message: String) {
        if (logLevel <= Q42StatsLogLevel.Info) {
            Log.i(TAG, message)
        }
    }

    fun w(message: String, e: Throwable? = null) {
        if (logLevel <= Q42StatsLogLevel.Warn) {
            Log.w(TAG, message, e)
        }
    }

    fun e(message: String, e: Throwable? = null) {
        if (logLevel <= Q42StatsLogLevel.Error) {
            Log.e(TAG, "$message: ${e?.message}", e)
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