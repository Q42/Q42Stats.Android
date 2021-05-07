package com.q42.q42stats.library.collector

import java.io.Serializable
import java.util.*

/** Collects System settings such as default locale */
object SystemCollector {

    fun collect() = mutableMapOf<String, Serializable>().apply {
        put("defaultLanguage", Locale.getDefault().language) // language code like en or nl
    }
}