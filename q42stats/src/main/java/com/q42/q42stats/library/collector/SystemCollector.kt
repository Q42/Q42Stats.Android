package com.q42.q42stats.library.collector

import java.io.Serializable
import java.util.*

/** Collects System settings such as default locale */
internal object SystemCollector {

    fun collect() = mutableMapOf<String, Serializable>().apply {
        put("defaultLanguage", Locale.getDefault().language) // language code like en or nl
        put("sdkVersion", android.os.Build.VERSION.SDK_INT) // eg 16 for Android 4.1 Jelly Bean
    }
}