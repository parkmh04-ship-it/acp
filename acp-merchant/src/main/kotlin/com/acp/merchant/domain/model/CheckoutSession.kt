package com.acp.merchant.domain.model

import java.math.BigDecimal
import java.time.ZonedDateTime

data class CheckoutSession(
    val id: String,
    val status: CheckoutStatus,
    val currency: String,
    val items: List<CheckoutItem>,
    val buyer: Buyer? = null,
    val shippingAddress: Address? = null,
    val totals: Totals = Totals.ZERO,
    val nextActionUrl: String? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),
    val expiresAt: ZonedDateTime? = null
) {
    fun isReadyForPayment(): Boolean {
        return status == CheckoutStatus.READY || (buyer != null && shippingAddress != null && items.isNotEmpty())
    }
}

enum class CheckoutStatus {
    NOT_READY, READY, COMPLETED, CANCELED
}

data class CheckoutItem(
    val productId: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
)

data class Buyer(
    val email: String? = null,
    val name: String? = null
)

data class Address(
    val countryCode: String,
    val postalCode: String? = null
)

data class Totals(
    val itemsBaseAmount: BigDecimal,
    val itemsDiscount: BigDecimal,
    val subtotal: BigDecimal,
    val tax: BigDecimal,
    val shipping: BigDecimal,
    val total: BigDecimal
) {
    companion object {
        val ZERO = Totals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
    }
}
