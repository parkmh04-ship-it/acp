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

@Serializable
data class PaymentApproveRequest(
    val merchantOrderId: String,
    val pgToken: String
)

@Serializable
data class PaymentApproveResponse(
    val paymentId: String,
    val status: String,
    val approvedAt: String? = null,
    val totalAmount: Long,
    val method: String? = null, // CARD, MONEY
    val cardInfo: CardInfo? = null
)

@Serializable
data class CardInfo(
    val issuerName: String,
    val purchaseName: String? = null,
    val cardType: String? = null,
    val installMonth: String? = null
)

@Serializable
data class PaymentCancelRequest(
    val merchantOrderId: String,
    val reason: String,
    val amount: Long // 전체 취소일 경우 전체 금액, 부분 취소일 경우 해당 금액
)

@Serializable
data class PaymentCancelResponse(
    val paymentId: String,
    val status: String,
    val canceledAt: String,
    val canceledAmount: Long
)
