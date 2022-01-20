package com.q42.q42stats.library.collector

import android.content.Context
import com.q42.q42stats.library.pay.GooglePayManager
import com.q42.q42stats.library.pay.cardNetworks
import com.q42.q42stats.library.pay.isGooglePlayEnabled
import java.io.Serializable

/** Collects Google Pay settings of the user
 *
 *  NOTE: The Google Pay "add payment method" does not support PayPal in NL atm, so we don't check
 *  for PayPal. We need to be a PayPal merchant to be able to do so.
 */
internal object GooglePayCollector {

    suspend fun collect(context: Context) = mutableMapOf<String, Serializable>().apply {

        val googlePayManager = GooglePayManager(context)
        val cardNetworkResponses = googlePayManager.getSupportedCardNetworks(cardNetworks)

        put("googlePayEnabled", cardNetworkResponses.isGooglePlayEnabled())
        cardNetworkResponses.forEach { (network, supportResponse) ->
            put(
                "googlePayCard_${network.value}",
                supportResponse
            ) // TODO or other format?
        }
    }
}