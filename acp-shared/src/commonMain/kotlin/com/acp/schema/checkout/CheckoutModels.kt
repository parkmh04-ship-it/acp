package com.acp.schema.checkout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCheckoutSessionRequest(
    val items: List<CheckoutItem>,
    val buyer: Buyer? = null,
    @SerialName("fulfillment_address")
    val fulfillmentAddress: Address? = null
)

@Serializable
data class CheckoutItem(
    val id: String,
    val quantity: Int
)

@Serializable
data class CheckoutSessionResponse(
    val id: String,
    
    @SerialName("payment_provider")
    val paymentProvider: PaymentProvider,
    
    val status: CheckoutStatus,
    val currency: String,
    
    @SerialName("line_items")
    val lineItems: List<LineItem>,
    
    val totals: List<Total>,
    val messages: List<Message>,
    val links: List<Link>,
    
    @SerialName("next_action_url")
    val nextActionUrl: String? = null
)

@Serializable
data class LineItem(
    val id: String,
    val item: CheckoutItem,
    @SerialName("base_amount") val baseAmount: Long,
    val discount: Long = 0,
    val subtotal: Long,
    val tax: Long,
    val total: Long
)

@Serializable
data class PaymentProvider(
    val provider: String,
    @SerialName("supported_payment_methods")
    val supportedPaymentMethods: List<String> = listOf("card")
)

@Serializable
enum class CheckoutStatus {
    @SerialName("not_ready_for_payment") NOT_READY,
    @SerialName("ready_for_payment") READY,
    @SerialName("completed") COMPLETED,
    @SerialName("canceled") CANCELED
}

@Serializable
data class Total(
    val type: TotalType,
    @SerialName("display_text") val displayText: String,
    val amount: Long
)

@Serializable
enum class TotalType {
    @SerialName("items_base_amount") ITEMS_BASE_AMOUNT,
    @SerialName("items_discount") ITEMS_DISCOUNT,
    @SerialName("subtotal") SUBTOTAL,
    @SerialName("discount") DISCOUNT,
    @SerialName("fulfillment") FULFILLMENT,
    @SerialName("tax") TAX,
    @SerialName("fee") FEE,
    @SerialName("total") TOTAL
}

@Serializable
data class Message(
    val type: MessageType,
    val content: String,
    @SerialName("content_type") val contentType: String = "plain",
    val code: String? = null
)

@Serializable
enum class MessageType {
    @SerialName("info") INFO,
    @SerialName("error") ERROR
}

@Serializable
data class Link(
    val type: LinkType,
    val value: String
)

@Serializable
enum class LinkType {
    @SerialName("terms_of_use") TERMS,
    @SerialName("privacy_policy") PRIVACY,
    @SerialName("seller_shop_policies") SELLER_POLICY
}

@Serializable
data class Buyer(
    val email: String? = null,
    val name: String? = null
)

@Serializable
data class Address(
    @SerialName("country_code") val countryCode: String,
    val postal_code: String? = null
)