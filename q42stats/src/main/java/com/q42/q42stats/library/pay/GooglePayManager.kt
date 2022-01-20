package com.q42.q42stats.library.pay

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.q42.q42stats.library.Q42StatsLogger
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GooglePayManager(context: Context) {

    // TODO add PAYPAL, needs PAYPAL_ACCOUNT_ID/merchant_id

    enum class IsReadyToPayResponse {
        TRUE, FALSE, UNKNOWN
    }

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
                                true -> IsReadyToPayResponse.TRUE
                                else -> IsReadyToPayResponse.FALSE
                            }
                        } catch (exception: Exception) {
                            Q42StatsLogger.w("Error fetching payment method", exception)
                            IsReadyToPayResponse.UNKNOWN // TODO is UNKNOWN correct here?
                        }
                    )
                }
            } catch (exception: Exception) {
                Q42StatsLogger.e("Unexpected error fetching payment method", exception)
                cont.resume(IsReadyToPayResponse.UNKNOWN)
            }
        }

    /**
     * An object describing accepted forms of payment by your app, used to determine a viewer's
     * readiness to pay.
     * @return API version and payment methods supported by the app.
     * @see [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
     */
    private fun createReadyToPayRequest(cardNetworks: List<String>) = IsReadyToPayRequest.fromJson(
        JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", createPaymentMethodsJson(cardNetworks))
            put("existingPaymentMethodRequired", true)
        }.toString()
    )

    /**
     * Describe your app's support for the CARD payment method.
     * The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest.
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException
     * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
// Optionally, you can add billing address/phone number associated with a CARD payment method.
    private fun createPaymentMethodsJson(cardNetworks: List<String>) =
        JSONArray().put(
            JSONObject().apply {
                put("type", "CARD") // TODO PayPall
                put("parameters", JSONObject().apply {
                    put(
                        "allowedAuthMethods",
                        JSONArray(GooglePayConfiguration.paymentMethods)
                    )
                    put("allowedCardNetworks", JSONArray(cardNetworks))
                })
            }
        )
}
