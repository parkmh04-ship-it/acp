package com.acp.schema.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductFeedItem(
        val id: String,
        val title: String,
        val description: String,
        val link: String,
        @SerialName("image_link") val imageLink: String,
        @SerialName("additional_image_links") val additionalImageLinks: List<String> = emptyList(),
        val price: String,
        val currency: String = "KRW",
        @SerialName("sale_price") val salePrice: String? = null,
        @SerialName("sale_price_effective_date") val salePriceEffectiveDate: String? = null,
        @SerialName("availability") val availability: Availability,
        @SerialName("product_category") val productCategory: String? = null,
        val brand: String? = null,
        val gtin: String? = null,
        val mpn: String? = null,
        val condition: Condition = Condition.NEW,

        // Merchant Info
        @SerialName("seller_name") val sellerName: String? = null,
        @SerialName("merchant_name") val merchantName: String? = null,
        @SerialName("merchant_url") val merchantUrl: String? = null,

        // Shipping & Return
        @SerialName("shipping_weight") val shippingWeight: String? = null,
        @SerialName("return_policy_days") val returnPolicyDays: Int? = null,

        // Ratings
        @SerialName("reviews_average_rating") val reviewsAverageRating: Double? = null,
        @SerialName("reviews_count") val reviewsCount: Int? = null
)

@Serializable
enum class Availability {
    @SerialName("in_stock") IN_STOCK,
    @SerialName("out_of_stock") OUT_OF_STOCK,
    @SerialName("preorder") PREORDER
}

@Serializable
enum class Condition {
    @SerialName("new") NEW,
    @SerialName("refurbished") REFURBISHED,
    @SerialName("used") USED
}
