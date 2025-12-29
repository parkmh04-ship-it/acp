package com.acp.merchant.adapter.outbound.cafe24.dto

import com.fasterxml.jackson.annotation.JsonProperty

/** Cafe24 API 상품 목록 응답 */
data class Cafe24ProductsResponse(val products: List<Cafe24Product> = emptyList())

/**
 * Cafe24 API 상품 상세 정보
 *
 * @see https://developers.cafe24.com/docs/ko/api/front/#products
 */
data class Cafe24Product(
        @JsonProperty("shop_no") val shopNo: Int,
        @JsonProperty("product_no") val productNo: Long,
        @JsonProperty("product_code") val productCode: String,
        @JsonProperty("product_name") val productName: String,
        @JsonProperty("eng_product_name") val engProductName: String? = null,
        @JsonProperty("supply_product_name") val supplyProductName: String? = null,
        @JsonProperty("internal_product_name") val internalProductName: String? = null,
        @JsonProperty("model_name") val modelName: String? = null,
        @JsonProperty("price") val price: String,
        @JsonProperty("retail_price") val retailPrice: String? = null,
        @JsonProperty("supply_price") val supplyPrice: String? = null,
        @JsonProperty("display") val display: String, // T or F
        @JsonProperty("selling") val selling: String, // T or F
        @JsonProperty("product_condition")
        val productCondition: String, // N(new), U(used), R(refurbished)
        @JsonProperty("product_used_month") val productUsedMonth: Int? = null,
        @JsonProperty("summary_description") val summaryDescription: String? = null,
        @JsonProperty("product_tag") val productTag: List<String>? = null,
        @JsonProperty("simple_description") val simpleDescription: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("mobile_description") val mobileDescription: String? = null,
        @JsonProperty("separated_mobile_description")
        val separatedMobileDescription: String? = null,
        @JsonProperty("detail_image") val detailImage: String? = null,
        @JsonProperty("list_image") val listImage: String? = null,
        @JsonProperty("tiny_image") val tinyImage: String? = null,
        @JsonProperty("small_image") val smallImage: String? = null,
        @JsonProperty("additional_images") val additionalImages: List<AdditionalImage>? = null,
        @JsonProperty("stock_quantity") val stockQuantity: Int,
        @JsonProperty("brand_code") val brandCode: String? = null,
        @JsonProperty("manufacturer_code") val manufacturerCode: String? = null,
        @JsonProperty("trend_code") val trendCode: String? = null,
        @JsonProperty("category") val category: List<Category>? = null,
        @JsonProperty("created_date") val createdDate: String? = null,
        @JsonProperty("updated_date") val updatedDate: String? = null,
        @JsonProperty("custom_product_code") val customProductCode: String? = null,
        @JsonProperty("custom_variant_code") val customVariantCode: String? = null,
        @JsonProperty("options") val options: List<ProductOption>? = null,
        @JsonProperty("variants") val variants: List<ProductVariant>? = null
)

data class AdditionalImage(
        @JsonProperty("big") val big: String? = null,
        @JsonProperty("medium") val medium: String? = null,
        @JsonProperty("small") val small: String? = null
)

data class Category(
        @JsonProperty("category_no") val categoryNo: Int,
        @JsonProperty("category_depth") val categoryDepth: Int,
        @JsonProperty("category_name") val categoryName: String,
        @JsonProperty("full_category_name") val fullCategoryName: CategoryName? = null
)

data class CategoryName(
        @JsonProperty("1") val depth1: String? = null,
        @JsonProperty("2") val depth2: String? = null,
        @JsonProperty("3") val depth3: String? = null,
        @JsonProperty("4") val depth4: String? = null
)

data class ProductOption(
        @JsonProperty("option_name") val optionName: String,
        @JsonProperty("option_value") val optionValue: List<String>
)

data class ProductVariant(
        @JsonProperty("variant_code") val variantCode: String,
        @JsonProperty("options") val options: List<VariantOption>,
        @JsonProperty("price") val price: String? = null,
        @JsonProperty("stock_quantity") val stockQuantity: Int
)

data class VariantOption(
        @JsonProperty("name") val name: String,
        @JsonProperty("value") val value: String
)
