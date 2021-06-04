package com.q42.q42stats.library

import android.util.Log

object Q42StatsLogger {
    /** logs with lower importance will be ignored */
    private val logLevel = if (BuildConfig.DEBUG) LogLevel.Verbose else LogLevel.Info

    fun v(tag: String, message: String) {
        if (logLevel <= LogLevel.Verbose) {
            Log.v(tag, message)
        }
    }

    fun d(tag: String, message: String) {
        if (logLevel <= LogLevel.Debug) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (logLevel <= LogLevel.Info) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String, message: String) {
        if (logLevel <= LogLevel.Warn) {
            Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String, e: Throwable? = null) {
        if (logLevel <= LogLevel.Error) {
            Log.e(tag, message, e)
        }
    }
}

enum class LogLevel {
    //Log levels in order of importance
    Verbose,
    Debug,
    Info,
    Warn,
    Error;
}