package com.acp.schema.checkout

import kotlinx.serialization.Serializable

@Serializable
data class CreateCheckoutSessionRequest(
    val items: List<CheckoutItem>,
    val buyer: Buyer? = null,
    val fulfillmentAddress: Address? = null
)

@Serializable
data class UpdateCheckoutSessionRequest(
    val items: List<CheckoutItem>? = null,
    val buyer: Buyer? = null,
    val fulfillmentAddress: Address? = null,
    val fulfillmentOptionId: String? = null
)

@Serializable
data class CheckoutItem(
    val id: String,
    val quantity: Int
)

@Serializable
data class CheckoutSessionResponse(
    val id: String,
    val paymentProvider: PaymentProvider,
    val status: CheckoutStatus,
    val currency: String,
    val lineItems: List<LineItem>,
    val fulfillmentOptions: List<FulfillmentOption> = emptyList(),
    val totals: List<Total>,
    val messages: List<Message>,
    val links: List<Link>,
    val nextActionUrl: String? = null
)

@Serializable
data class Total(
    val type: TotalType,
    val displayText: String,
    val amount: Long
)

@Serializable
data class FulfillmentOption(
    val id: String,
    val name: String,
    val description: String,
    val estimatedMinDays: Int,
    val estimatedMaxDays: Int,
    val amount: Long,
    val currency: String = "KRW"
)

@Serializable
data class LineItem(
    val id: String,
    val item: CheckoutItem,
    val baseAmount: Long,
    val discount: Long = 0,
    val subtotal: Long,
    val tax: Long,
    val total: Long
)

@Serializable
data class PaymentProvider(
    val provider: String,
    val supportedPaymentMethods: List<String> = listOf("card")
)

@Serializable
enum class CheckoutStatus {
    NOT_READY,
    READY,
    COMPLETED,
    CANCELED
}

@Serializable
enum class TotalType {
    ITEMS_BASE_AMOUNT,
    ITEMS_DISCOUNT,
    SUBTOTAL,
    DISCOUNT,
    FULFILLMENT,
    TAX,
    FEE,
    TOTAL
}

@Serializable
data class Message(
    val type: MessageType,
    val content: String,
    val contentType: String = "plain",
    val code: String? = null
)

@Serializable
enum class MessageType {
    INFO,
    ERROR
}

@Serializable
data class Link(
    val type: LinkType,
    val value: String
)

@Serializable
enum class LinkType {
    TERMS,
    PRIVACY,
    SELLER_POLICY
}

@Serializable
data class Buyer(
    val email: String? = null,
    val name: String? = null
)

@Serializable
data class Address(
    val countryCode: String,
    val postalCode: String? = null
)