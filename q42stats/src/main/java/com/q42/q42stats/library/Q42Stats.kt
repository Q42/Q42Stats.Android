package com.q42.q42stats.library

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.q42.q42stats.library.collector.AccessibilityCollector
import com.q42.q42stats.library.collector.PaymentMethodsCollector
import com.q42.q42stats.library.collector.PreferencesCollector
import com.q42.q42stats.library.collector.SystemCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * Version code for the data format that is sent to the server. Increment by 1 every time
 * you add / remove / change a field in any of the Collector classes
 */
internal const val DATA_MODEL_VERSION = 2

class Q42Stats(private val config: Q42StatsConfig) {

    /* Collects stats and sends it to the server. This method is safe to be called from anywhere
    in your app and will do nothing if it is running or has already run before */
    @AnyThread
    fun runAsync(context: Context, coroutineScope: CoroutineScope = MainScope()) {
        Q42StatsLogger.d("Q42Stats: Checking Preconditions")
        // check preconditions on the main thread to prevent concurrency issues
        coroutineScope.launch(Dispatchers.Main) {
            if (job?.isActive != true) { // job is null or not active
                // Do the actual work on a worker thread
                job = coroutineScope.launch(Dispatchers.IO) { runSync(context) }
            } else {
                Q42StatsLogger.i("Q42Stats is already running. Exit.")
            }
        }
    }

    @WorkerThread
    private suspend fun runSync(context: Context) {
        try {
            val prefs = Q42StatsPrefs(context)
            if (prefs.withinSubmitInterval(config.minimumSubmitIntervalSeconds * 1000L)) {
                Q42StatsLogger.i(
                    "Q42Stats were already sent in the last ${config.minimumSubmitIntervalSeconds} seconds."
                )
                return
            }
            Q42StatsLogger.i("Q42Stats: Start")
            val collected = collect(context, prefs).toFireStoreFormat()
            HttpService.sendStatsSync(config, collected)
            prefs.updateSubmitTimestamp()
        } catch (e: Throwable) {
            Q42StatsLogger.e("Q42Stats encountered an error", e)
            if (BuildConfig.DEBUG) {
                throw e
            }
        } finally {
            Q42StatsLogger.i("Q42Stats: Exit")
        }
    }

    private suspend fun collect(
        context: Context,
        prefs: Q42StatsPrefs
    ): MutableMap<String, Serializable> {
        val collected = mutableMapOf<String, Serializable>()

        collected["Stats Model Version"] = DATA_MODEL_VERSION
        collected["Stats Version"] = "Android ${BuildConfig.LIB_BUILD_DATE}"
        collected["Stats instance ID"] = prefs.getOrCreateInstallationId()
        collected["Stats timestamp"] = System.currentTimeMillis() / 1000L

        collected += AccessibilityCollector.collect(context)
        collected += PreferencesCollector.collect(context)
        collected += PaymentMethodsCollector.collect(context)
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

        /** A static job ensures that only a single instance of Q42Stats can be running */
        private var job: Job? = null
    }
}