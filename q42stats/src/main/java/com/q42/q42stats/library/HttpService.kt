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
            conn.setRequestProperty("connection", "close")
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            sendPostRequestContent(conn, jsonObject)
            // Don't read any response from the server, the HttUrlConnection code would trigger
            // a StrictMode violation related to a Zip inflator that is unclosed
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
        } catch (e: Throwable) {
            Q42StatsLogger.e(TAG, "Could not add data to POST request", e)
        }
    }
}