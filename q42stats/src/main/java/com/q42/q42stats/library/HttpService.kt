package com.q42.q42stats.library

import androidx.annotation.WorkerThread
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@WorkerThread
internal object HttpService {

    fun sendStatsSync(config: Q42StatsConfig, data: JSONObject) {
        httpPost(
            "https://q42stats.ew.r.appspot.com/add/${config.firestoreCollectionId}",
            data,
            config.apiKey
        )
    }

    private fun httpPost(url: String, jsonObject: JSONObject, apiKey: String) {
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
            sendPostRequestContent(conn, jsonObject)
        } catch (e: Throwable) {
            Q42StatsLogger.e(TAG, "Could not send stats to server", e)
        } finally {
            conn.disconnect()
        }
    }

    private fun sendPostRequestContent(conn: HttpURLConnection, jsonObject: JSONObject) {
        try {
            conn.outputStream.use { os ->
                BufferedWriter(OutputStreamWriter(os, "UTF-8")).use { writer ->
                    writer.write(jsonObject.toString())
                    Q42StatsLogger.d(TAG, "Sending JSON: $jsonObject")
                    writer.flush()
                }
            }
            // Only when reading the response, the request gets executed
            Q42StatsLogger.d(TAG, "Response: ${conn.responseCode} ${conn.responseMessage}")
        } catch (e: Throwable) {
            Q42StatsLogger.e(TAG, "Could not add data to POST request", e)
        }
    }
}
