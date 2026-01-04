package com.acp.schema.feed

import kotlinx.serialization.Serializable

@Serializable
data class ProductFeedItem(
        val id: String,
        val title: String,
        val description: String,
        val link: String,
        val imageLink: String,
        val additionalImageLinks: List<String> = emptyList(),
        val price: String,
        val currency: String = "KRW",
        val salePrice: String? = null,
        val salePriceEffectiveDate: String? = null,
        val availability: Availability,
        val productCategory: String? = null,
        val brand: String? = null,
        val gtin: String? = null,
        val mpn: String? = null,
        val condition: Condition = Condition.NEW,

        // Merchant Info
        val sellerName: String? = null,
        val merchantName: String? = null,
        val merchantUrl: String? = null,

        // Shipping & Return
        val shippingWeight: String? = null,
        val returnPolicyDays: Int? = null,

        // Ratings
        val reviewsAverageRating: Double? = null,
        val reviewsCount: Int? = null
)

@Serializable
enum class Availability {
    IN_STOCK,
    OUT_OF_STOCK,
    PREORDER
}

@Serializable
enum class Condition {
    NEW,
    REFURBISHED,
    USED
}
