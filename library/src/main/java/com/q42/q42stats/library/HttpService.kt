package com.q42.q42stats.library

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object HttpService {
    fun sendStats(data: JSONObject) {
        httpPost("https://example.com/stats/android", data)
    }

    @Throws(IOException::class, JSONException::class)
    private fun httpPost(url: String, jsonObject: JSONObject) {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL(url).openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setPostRequestContent(conn, jsonObject)
            conn.connect()

            Log.d(TAG, "${conn.responseCode} ${conn.responseMessage}")
        }
    }
//
//    private fun checkNetworkConnection(context: Context): Boolean {
//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
//        return activeNetwork?.isConnectedOrConnecting == true
//    }

    @Throws(IOException::class)
    private fun setPostRequestContent(conn: HttpURLConnection, jsonObject: JSONObject) {

        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(jsonObject.toString())
        Log.i(TAG, jsonObject.toString())
        writer.flush()
        writer.close()
        os.close()
    }
}