package com.q42.q42stats.library.collector

import android.content.Context
import android.os.Build
import java.io.Serializable
import java.util.*

/** Collects System settings such as default locale */
internal object SystemCollector {

    fun collect(context: Context) = mutableMapOf<String, Serializable>().apply {
        put("applicationId", context.packageName)
        put("defaultLanguage", Locale.getDefault().language) // language code like en or nl
        put("sdkVersion", Build.VERSION.SDK_INT) // eg 16 for Android 4.1 Jelly Bean
        put("manufacturer", Build.MANUFACTURER)
        // Usually code names used during production. ie. D5503 For Sony Z1
        put("modelName", Build.MODEL)
    }
}
