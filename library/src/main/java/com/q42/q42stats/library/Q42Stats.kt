package com.q42.q42stats.library

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.q42.q42stats.library.collector.AccessibilityCollector
import com.q42.q42stats.library.collector.PreferencesCollector
import com.q42.q42stats.library.collector.SystemCollector
import kotlinx.coroutines.*
import java.io.Serializable

const val TAG = "Q42Stats"

class Q42Stats(private val config: Q42StatsConfig) {

    /* Collects stats and sends it to the server. This method is safe to be called from anywhere
    in your app and will do nothing if it is running or has already run before */
    @AnyThread
    fun runAsync(context: Context, coroutineScope: CoroutineScope = MainScope()) {
        Q42StatsLogger.d(TAG, "Q42Stats: Checking Preconditions")
        // check preconditions on the main thread to prevent concurrency issues
        coroutineScope.launch(Dispatchers.Main) {
            if (job?.isActive != true) { // job is null or not active
                // Do the actual work on a worker thread
                job = coroutineScope.launch(Dispatchers.IO) { runSync(context) }
            } else {
                Q42StatsLogger.i(TAG, "Q42Stats is already running. Exit.")
            }
        }
    }

    @WorkerThread
    private fun runSync(context: Context) {
        try {
            val prefs = Q42StatsPrefs(context)
            if (prefs.withinSubmitInterval(config.minimumSubmitIntervalSeconds * 1000L)
                && !BuildConfig.DEBUG
            ) {
                Q42StatsLogger.i(
                    TAG,
                    "Q42Stats were already sent in the last ${config.minimumSubmitIntervalSeconds} seconds."
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

        private var job: Job? = null
    }
}