package com.acp.merchant.adapter.outbound.cafe24.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cafe24 API 상품 목록 응답
 */
@Serializable
data class Cafe24ProductsResponse(
    val products: List<Cafe24Product>
)

/**
 * Cafe24 API 상품 상세 정보
 * 
 * @see https://developers.cafe24.com/docs/ko/api/front/#products
 */
@Serializable
data class Cafe24Product(
    @SerialName("shop_no")
    val shopNo: Int,
    
    @SerialName("product_no")
    val productNo: Long,
    
    @SerialName("product_code")
    val productCode: String,
    
    @SerialName("product_name")
    val productName: String,
    
    @SerialName("eng_product_name")
    val engProductName: String? = null,
    
    @SerialName("supply_product_name")
    val supplyProductName: String? = null,
    
    @SerialName("internal_product_name")
    val internalProductName: String? = null,
    
    @SerialName("model_name")
    val modelName: String? = null,
    
    @SerialName("price")
    val price: String,
    
    @SerialName("retail_price")
    val retailPrice: String? = null,
    
    @SerialName("supply_price")
    val supplyPrice: String? = null,
    
    @SerialName("display")
    val display: String, // T or F
    
    @SerialName("selling")
    val selling: String, // T or F
    
    @SerialName("product_condition")
    val productCondition: String, // N(new), U(used), R(refurbished)
    
    @SerialName("product_used_month")
    val productUsedMonth: Int? = null,
    
    @SerialName("summary_description")
    val summaryDescription: String? = null,
    
    @SerialName("product_tag")
    val productTag: List<String>? = null,
    
    @SerialName("simple_description")
    val simpleDescription: String? = null,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("mobile_description")
    val mobileDescription: String? = null,
    
    @SerialName("separated_mobile_description")
    val separatedMobileDescription: String? = null,
    
    @SerialName("detail_image")
    val detailImage: String? = null,
    
    @SerialName("list_image")
    val listImage: String? = null,
    
    @SerialName("tiny_image")
    val tinyImage: String? = null,
    
    @SerialName("small_image")
    val smallImage: String? = null,
    
    @SerialName("additional_images")
    val additionalImages: List<AdditionalImage>? = null,
    
    @SerialName("stock_quantity")
    val stockQuantity: Int,
    
    @SerialName("brand_code")
    val brandCode: String? = null,
    
    @SerialName("manufacturer_code")
    val manufacturerCode: String? = null,
    
    @SerialName("trend_code")
    val trendCode: String? = null,
    
    @SerialName("category")
    val category: List<Category>? = null,
    
    @SerialName("created_date")
    val createdDate: String? = null,
    
    @SerialName("updated_date")
    val updatedDate: String? = null,
    
    @SerialName("custom_product_code")
    val customProductCode: String? = null,
    
    @SerialName("custom_variant_code")
    val customVariantCode: String? = null,
    
    @SerialName("options")
    val options: List<ProductOption>? = null,
    
    @SerialName("variants")
    val variants: List<ProductVariant>? = null
)

@Serializable
data class AdditionalImage(
    @SerialName("big")
    val big: String? = null,
    
    @SerialName("medium")
    val medium: String? = null,
    
    @SerialName("small")
    val small: String? = null
)

@Serializable
data class Category(
    @SerialName("category_no")
    val categoryNo: Int,
    
    @SerialName("category_depth")
    val categoryDepth: Int,
    
    @SerialName("category_name")
    val categoryName: String,
    
    @SerialName("full_category_name")
    val fullCategoryName: CategoryName? = null
)

@Serializable
data class CategoryName(
    @SerialName("1")
    val depth1: String? = null,
    
    @SerialName("2")
    val depth2: String? = null,
    
    @SerialName("3")
    val depth3: String? = null,
    
    @SerialName("4")
    val depth4: String? = null
)

@Serializable
data class ProductOption(
    @SerialName("option_name")
    val optionName: String,
    
    @SerialName("option_value")
    val optionValue: List<String>
)

@Serializable
data class ProductVariant(
    @SerialName("variant_code")
    val variantCode: String,
    
    @SerialName("options")
    val options: List<VariantOption>,
    
    @SerialName("price")
    val price: String? = null,
    
    @SerialName("stock_quantity")
    val stockQuantity: Int
)

@Serializable
data class VariantOption(
    @SerialName("name")
    val name: String,
    
    @SerialName("value")
    val value: String
)
