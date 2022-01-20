package com.q42.q42stats.library.collector

import android.content.Context
import com.example.googlepay.GooglePayConfiguration.cardNetworks
import com.example.googlepay.GooglePayManager
import com.example.googlepay.GooglePayManager.IsReadyToPayResponse
import java.io.Serializable

/** Collects Google Pay settings of the user */
internal object GooglePayCollector {

    suspend fun collect(context: Context) = mutableMapOf<String, Serializable>().apply {

        val googlePayManager = GooglePayManager(context)
        val cardNetworkResponses = googlePayManager.getSupportedCardNetworks(cardNetworks)
        val googlePayEnabled =
            when {
                cardNetworkResponses.any { it.value === IsReadyToPayResponse.TRUE } ->
                    IsReadyToPayResponse.TRUE
                cardNetworkResponses.any { it.value === IsReadyToPayResponse.UNKNOWN } ->
                    IsReadyToPayResponse.UNKNOWN
                else -> IsReadyToPayResponse.FALSE
            }

        put("googlePayEnabled", googlePayEnabled)
        cardNetworkResponses.forEach { (network, supportResponse) ->
            put(
                "googlePayCard_${network.value}",
                supportResponse
            ) // TODO or other format?
        }
    }
}