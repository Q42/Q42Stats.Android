package com.q42.q42stats.library

import androidx.annotation.WorkerThread
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@WorkerThread
object HttpService {

    fun sendStatsSync(config: Q42StatsConfig, data: JSONObject) {
        sendStatsSync(
            "https://firestore.googleapis.com/v1/projects/${config.fireBaseProject}/" +
                    "databases/(default)/documents/${config.firebaseCollection}?mask.fieldPaths=_",
            data
        )
    }

    private fun sendStatsSync(url: String, data: JSONObject) {
        httpPost(url, data)
    }

    private fun httpPost(url: String, jsonObject: JSONObject) {
        try {
            val conn = URL(url).openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            sendPostRequestContent(conn, jsonObject)
        } catch (e: Throwable) {
            throw Q42StatsException("Could not send stats to server", e)
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
            Q42StatsLogger.d(TAG, "Response: ${conn.responseCode} ${conn.responseMessage}")
        } catch (e: Throwable) {
            throw Q42StatsException("Could not add data to POST request", e)
        } finally {
            conn.disconnect()
        }
    }
}