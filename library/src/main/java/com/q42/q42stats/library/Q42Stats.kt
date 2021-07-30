package com.q42.q42stats.library

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.q42.q42stats.library.collector.AccessibilityCollector
import com.q42.q42stats.library.collector.PreferencesCollector
import com.q42.q42stats.library.collector.SystemCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.concurrent.atomic.AtomicBoolean

const val TAG = "Q42Stats"

private val MUTEX = Unit

class Q42Stats(private val config: Q42StatsConfig) {
    private val isRunning = AtomicBoolean(false)

    /* Collects stats and sends it to the server. This method is safe to be called from anywhere
    in your app and will do nothing if it has already run before */
    @AnyThread
    fun runAsync(context: Context) {
        Q42StatsLogger.d(TAG, "Q42Stats: Checking Preconditions")
        if (isRunning.get()) {
            Q42StatsLogger.i(
                TAG,
                "Q42Stats is already running. Exit."
            )
            return
        }
        GlobalScope.launch(Dispatchers.IO) { synchronized(MUTEX) { runSync(context) } }
    }

    @WorkerThread
    private fun runSync(context: Context) {
        try {
            isRunning.set(true)
            val prefs = Q42StatsPrefs(context)
            if (prefs.withinSubmitInterval(config.minimumSubmitInterval * 1000L) && !BuildConfig.DEBUG) {
                Q42StatsLogger.i(
                    TAG,
                    "Q42Stats were already sent in the last ${config.minimumSubmitInterval} seconds."
                )
                return
            }
            Q42StatsLogger.i(TAG, "Q42Stats: Start")
            val collected = collect(context, prefs).toFireStoreFormat()
            HttpService.sendStatsSync(config, collected)
            prefs.updateSubmitTimestamp()

        } catch (e: Throwable) {
            Q42StatsLogger.e(TAG, "Q42Stats encountered an error", e)
            if (BuildConfig.DEBUG) {
                throw e
            }
        } finally {
            Q42StatsLogger.i(TAG, "Q42Stats: Exit")
            isRunning.set(false)
        }
    }

    private fun collect(context: Context, prefs: Q42StatsPrefs): MutableMap<String, Serializable> {
        val collected = mutableMapOf<String, Serializable>()

        collected["Stats Version"] = "Android ${BuildConfig.LIB_BUILD_DATE}"
        collected["Stats instance ID"] = prefs.getOrCreateInstallationId()
        collected["Stats timestamp"] = System.currentTimeMillis() / 1000L

        collected += AccessibilityCollector.collect(context)
        collected += PreferencesCollector.collect(context)
        collected += SystemCollector.collect()
        return collected
    }

    companion object {
        @Suppress("unused")
        var logLevel: Q42StatsLogLevel
            get() = Q42StatsLogger.logLevel
            set(value) {
                Q42StatsLogger.logLevel = value
            }
    }
}