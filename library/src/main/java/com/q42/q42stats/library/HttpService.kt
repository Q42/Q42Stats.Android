package com.q42.q42stats.library

import androidx.annotation.WorkerThread
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.IOException
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

    /** Synchronously send the stats. Make sure to run this on a worker thread */
    private fun sendStatsSync(url: String, data: JSONObject) {
        httpPost(url, data)
    }

    @Throws(IOException::class, JSONException::class)
    private fun httpPost(url: String, jsonObject: JSONObject) {
        val conn = URL(url).openConnection() as HttpsURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        setPostRequestContent(conn, jsonObject)
        conn.connect()
        Q42StatsLogger.d(TAG, "Response: ${conn.responseCode} ${conn.responseMessage}")
    }

    @Throws(IOException::class)
    private fun setPostRequestContent(conn: HttpURLConnection, jsonObject: JSONObject) {

        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(jsonObject.toString())
        Q42StatsLogger.d(TAG, "Sending JSON: $jsonObject")
        writer.flush()
        writer.close()
        os.close()
    }
}