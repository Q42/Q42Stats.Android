package com.example.googlepay

@JvmInline
value class CardNetwork(val value: String)

object GooglePayConfiguration {

    /**
     * The allowed networks to be requested from the API. If the user has cards from networks not
     * specified here in their account, these will not be offered for them to choose in the popup.
     * @value #SUPPORTED_NETWORKS
     */
    var cardNetworks = listOf(
        CardNetwork("AMEX"),
        CardNetwork("DISCOVER"),
        CardNetwork("JCB"),
        CardNetwork("MASTERCARD"),
        CardNetwork("VISA")
    )

    /**
     * The Google Pay API may return cards on file on Google.com (PAN_ONLY) and/or a device token on
     * an Android device authenticated with a 3-D Secure cryptogram (CRYPTOGRAM_3DS).
     *
     * More info: https://developers.google.com/pay/api/web/reference/request-objects#CardParameters
     *
     * @value #SUPPORTED_METHODS
     */
    var paymentMethods = listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"
    )
}
