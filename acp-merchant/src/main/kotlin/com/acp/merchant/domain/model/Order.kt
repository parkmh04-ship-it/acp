package com.acp.merchant.domain.model

import java.math.BigDecimal
import java.time.ZonedDateTime

data class Order(
    val id: String,
    val userId: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val currency: String,
    val paymentRequestIds: String? = null,
    val items: List<OrderLineItem>,
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)

enum class OrderStatus {
    PENDING,
    AUTHORIZED,
    COMPLETED,
    CANCELED,
    FAILED
}

data class OrderLineItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
)
