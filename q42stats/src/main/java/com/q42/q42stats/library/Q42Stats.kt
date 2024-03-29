package com.q42.q42stats.library

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.q42.q42stats.library.collector.AccessibilityCollector
import com.q42.q42stats.library.collector.PreferencesCollector
import com.q42.q42stats.library.collector.SystemCollector
import com.q42.q42stats.library.util.deserializeMeasurement
import com.q42.q42stats.library.util.filterValueNotNull
import com.q42.q42stats.library.util.serializeMeasurement
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.Serializable

internal const val TAG = "Q42Stats"

/**
 * Version code for the data format that is sent to the server. Increment by 1 every time
 * you add / remove / change a field in any of the Collector classes
 */
internal const val DATA_MODEL_VERSION = 4

class Q42Stats(private val config: Q42StatsConfig) {

    /**
     *  Collects stats and sends it to the server. This method is safe to be called from anywhere
     *  in your app and will do nothing if it is running or has already run before
     */
    @AnyThread
    fun runAsync(
        context: Context,
        coroutineScope: CoroutineScope = MainScope() + coroutineExceptionHandler
    ) {
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
        withPrefs(context) { prefs ->
            try {
                if (prefs.withinSubmitInterval(config.minimumSubmitIntervalSeconds * 1000L)) {
                    Q42StatsLogger.i(
                        TAG,
                        "Q42Stats were already sent in the last ${config.minimumSubmitIntervalSeconds} seconds."
                    )
                    return@withPrefs
                }
                Q42StatsLogger.i(TAG, "Q42Stats: Start")

                val currentMeasurement = collect(context)

                val previousMeasurement: Map<String, Any?>? =
                    prefs.previousMeasurement?.let { deserializeMeasurement(it) }
                val payload: Map<String, Any> = mapOf<String, Any?>(
                    "currentMeasurement" to currentMeasurement,
                    "previousMeasurement" to previousMeasurement,
                ).filterValueNotNull()
                val serializedPayload = serializeMeasurement(payload.toQ42StatsApiFormat())
                val responseBody = HttpService.sendStatsSync(
                    config,
                    serializedPayload,
                    prefs.lastBatchId
                )
                responseBody?.let { body ->
                    val batchId = JSONObject(body).getString("batchId") // throws if not found
                    prefs.lastBatchId = batchId
                    prefs.previousMeasurement = currentMeasurement
                        .toQ42StatsApiFormat()
                        .let { q42StatsCurrentMeasurement ->
                            serializeMeasurement(q42StatsCurrentMeasurement)
                        }
                }
            } catch (e: Throwable) {
                handleException(e)
            } finally {
                prefs.updateSubmitTimestamp() // make sure to always update the submit timestamp
                Q42StatsLogger.i(TAG, "Q42Stats: Exit")
            }
        }
    }

    private fun withPrefs(context: Context, action: (prefs: Q42StatsPrefs) -> Unit) {
        try {
            val prefs = Q42StatsPrefs(context)
            action(prefs)
        } catch (e: Throwable) {
            handleException(e)
            Q42StatsLogger.i(TAG, "Q42Stats: Exit")
        }
    }

    private fun collect(context: Context): MutableMap<String, Serializable> {
        val collected = mutableMapOf<String, Serializable>()

        collected["Stats Version"] = "Android ${BuildConfig.LIB_BUILD_DATE}"
        collected["Stats Model Version"] = DATA_MODEL_VERSION
        collected["Stats timestamp"] = System.currentTimeMillis() / 1000L

        collected += AccessibilityCollector.collect(context)
        collected += PreferencesCollector.collect(context)
        collected += SystemCollector.collect(context)
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

        private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            handleException(throwable)
        }

        private fun handleException(e: Throwable) {
            Q42StatsLogger.e(TAG, "Q42Stats encountered an error", e)
            if (BuildConfig.DEBUG) {
                throw e
            }
        }
    }
}
