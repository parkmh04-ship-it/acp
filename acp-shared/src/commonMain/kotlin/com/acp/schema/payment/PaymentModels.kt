package com.acp.schema.payment

import kotlinx.serialization.Serializable

@Serializable
data class PaymentPrepareRequest(
    val merchantOrderId: String,
    val amount: Long,
    val currency: String = "KRW",
    val items: List<PaymentItem>
)

@Serializable
data class PaymentItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Long,
    val currency: String = "KRW"
)

@Serializable
data class PaymentPrepareResponse(
    val paymentId: String,
    val merchantOrderId: String,
    val redirectUrl: String, // KakaoPay 'next_redirect_pc_url'
    val status: String // READY
)
