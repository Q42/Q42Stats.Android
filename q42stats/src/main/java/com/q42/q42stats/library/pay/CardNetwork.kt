package com.q42.q42stats.library.pay

@JvmInline
value class CardNetwork(val value: String)

enum class IsReadyToPayResponse {
    TRUE, FALSE, UNKNOWN
}

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

fun Map<CardNetwork, IsReadyToPayResponse>.isGooglePlayEnabled(): IsReadyToPayResponse =
    when {
        any { it.value === IsReadyToPayResponse.TRUE } ->
            IsReadyToPayResponse.TRUE
        any { it.value === IsReadyToPayResponse.UNKNOWN } ->
            IsReadyToPayResponse.UNKNOWN
        else -> IsReadyToPayResponse.FALSE
    }