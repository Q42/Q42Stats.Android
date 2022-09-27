package com.q42.q42stats.library

import androidx.annotation.WorkerThread
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@WorkerThread
internal object HttpService {

    /** Sends stats and returns body as String if successful */
    fun sendStatsSync(config: Q42StatsConfig, data: String, lastBatchId: String?): String? =
        httpPost(
            "https://q42stats.ew.r.appspot.com/add/${config.firestoreCollectionId}",
            data,
            config.apiKey,
            lastBatchId
        )
}

private fun httpPost(
    url: String,
    data: String,
    apiKey: String,
    lastBatchId: String?
): String? {
    val conn = URL(url).openConnection() as HttpsURLConnection
    try {
        conn.requestMethod = "POST"
        conn.setRequestProperty("Connection", "close")
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        // Explicitly tell the server to not gzip the response.
        // Otherwise, HttpUrlsConnection will open a GzipInflater and not close it,
        // which triggers a StrictMode violation
        // https://issuetracker.google.com/issues/37069164#comment11
        conn.setRequestProperty("Accept-Encoding", "identity")
        conn.setRequestProperty("X-Api-Key", apiKey)
        lastBatchId?.let {
            conn.setRequestProperty("batchId", it)
        }
        return sendPostRequestContent(conn, data)
    } catch (e: Throwable) {
        Q42StatsLogger.e(TAG, "Could not send stats to server", e)
    } finally {
        conn.disconnect()
    }

    return null
}

private fun sendPostRequestContent(conn: HttpURLConnection, data: String): String? {
    try {
        conn.outputStream.use { os ->
            BufferedWriter(OutputStreamWriter(os, "UTF-8")).use { writer ->
                writer.write(data)
                Q42StatsLogger.d(TAG, "Sending JSON: $data")
                writer.flush()
            }
        }
        // Only when reading the response, the request gets executed
        Q42StatsLogger.d(TAG, "Response: ${conn.responseCode} ${conn.responseMessage}")
        if (conn.responseCode in 200..299) {
            return conn.getResponseText()
        }
    } catch (e: Throwable) {
        Q42StatsLogger.e(TAG, "Could not add data to POST request", e)
    }
    return null
}

private fun HttpURLConnection.getResponseText(): String {
    BufferedReader(InputStreamReader(inputStream)).use {
        return it.readText()
    }
}
