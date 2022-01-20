package com.q42.q42stats.library.collector

import android.content.Context
import com.example.googlepay.GooglePayConfiguration.supportedCardNetworks
import com.example.googlepay.GooglePayManager
import java.io.Serializable

/** Collects Google Pay settings of the user */
internal object GooglePayCollector {

    suspend fun collect(context: Context) = mutableMapOf<String, Serializable>().apply {

        val googlePayManager = GooglePayManager(context)

        put("googlePayEnabled", googlePayManager.checkIsReadyToPay(supportedCardNetworks))
    }
}