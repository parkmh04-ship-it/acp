package com.acp.schema.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductFeedItem(
    val id: String,
    val title: String,
    val description: String,
    val link: String,
    
    @SerialName("image_link")
    val imageLink: String,
    
    val price: String, // "100.00 USD" format handling might be needed, but spec says Number + Currency. Let's strictly follow spec logic later. Spec says "price" field.
    
    @SerialName("availability")
    val availability: Availability,
    
    @SerialName("product_category")
    val productCategory: String? = null,
    
    val brand: String? = null,
    val condition: Condition = Condition.NEW,
    
    // Merchant Info
    @SerialName("seller_name")
    val sellerName: String? = null
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
