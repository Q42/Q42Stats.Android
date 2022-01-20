package com.q42.q42stats.library.pay

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.q42.q42stats.library.Q42StatsLogger
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GooglePayManager(context: Context) {

    private val paymentsClient =
        Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
                .build()
        )

    suspend fun getSupportedCardNetworks(cardNetworks: List<CardNetwork>): Map<CardNetwork, IsReadyToPayResponse> =
        mutableMapOf<CardNetwork, IsReadyToPayResponse>().apply {
            cardNetworks.forEach { cardNetwork ->
                put(cardNetwork, checkGooglePaySupport(cardNetwork))
            }
        }

    /**
     * We could test all cards at once, but we test every CardNetwork separately, so that we can
     * log the exact CardNetwork of the user.
     */
    private suspend fun checkGooglePaySupport(cardNetwork: CardNetwork): IsReadyToPayResponse =
        suspendCoroutine { cont ->
            try {
                val request = createReadyToPayRequest(listOf(cardNetwork.value))
                paymentsClient.isReadyToPay(request).addOnCompleteListener { completedTask ->
                    cont.resume(
                        try {
                            when (completedTask.getResult(ApiException::class.java)) {
                                true -> IsReadyToPayResponse.SUPPORTED
                                else -> IsReadyToPayResponse.UNSUPPORTED
                            }
                        } catch (exception: Exception) {
                            Q42StatsLogger.w("Error fetching payment method", exception)
                            IsReadyToPayResponse.SUPPORT_UNKNOWN
                        }
                    )
                }
            } catch (exception: Exception) {
                Q42StatsLogger.e("Unexpected error fetching payment method", exception)
                cont.resume(IsReadyToPayResponse.SUPPORT_UNKNOWN)
            }
        }
    // TODO test without play services

    /**
     * @see [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
     * @throws JSONException
     */
    private fun createReadyToPayRequest(cardNetworks: List<String>) =
        IsReadyToPayRequest.fromJson(
            JSONObject().apply {
                put("apiVersion", 2)
                put("apiVersionMinor", 0)
                put("allowedPaymentMethods", createPaymentMethodsJson(cardNetworks))
                put("existingPaymentMethodRequired", true)
            }.toString()
        )

    /**
     * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     * @throws JSONException
     */
    private fun createPaymentMethodsJson(cardNetworks: List<String>) =
        JSONArray().put(
            JSONObject().apply {
                put(
                    "type",
                    "CARD"
                ) // The Google Pay "add payment method" does not support PayPal in NL atm, so we don't check for PayPal.
                put("parameters", JSONObject().apply {
                    put(
                        "allowedAuthMethods",
                        JSONArray(
                            listOf(
                                "PAN_ONLY",
                                "CRYPTOGRAM_3DS"
                            )
                        )
                    )
                    put("allowedCardNetworks", JSONArray(cardNetworks))
                })
            }
        )
}
