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
        sendStatsSync(
            "https://firestore.googleapis.com/v1/projects/${config.firebaseProjectId}/" +
                "databases/(default)/documents/${config.firestoreCollectionId}?mask.fieldPaths=_",
            data
        )
    }

    private fun sendStatsSync(url: String, data: JSONObject) {
        httpPost(url, data)
    }

    private fun httpPost(url: String, jsonObject: JSONObject) {
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
            sendPostRequestContent(conn, jsonObject)
        } catch (e: Throwable) {
            Q42StatsLogger.e("Could not send stats to server", e)
        } finally {
            conn.disconnect()
        }
    }

    private fun sendPostRequestContent(conn: HttpURLConnection, jsonObject: JSONObject) {
        try {
            conn.outputStream.use { os ->
                BufferedWriter(OutputStreamWriter(os, "UTF-8")).use { writer ->
                    writer.write(jsonObject.toString())
                    Q42StatsLogger.d("Sending JSON: ${jsonObject.toString(4)}")
                    writer.flush()
                }
            }
            // Only when reading the response, the request gets executed
            Q42StatsLogger.d("Response: ${conn.responseCode} ${conn.responseMessage}")
        } catch (e: Throwable) {
            Q42StatsLogger.e("Could not add data to POST request", e)
        }
    }
}