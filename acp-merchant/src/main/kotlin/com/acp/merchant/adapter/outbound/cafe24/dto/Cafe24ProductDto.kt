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
        val shopNo: Int,
        val productNo: Long,
        val productCode: String,
        val productName: String,
        val engProductName: String? = null,
        val supplyProductName: String? = null,
        val internalProductName: String? = null,
        val modelName: String? = null,
        val price: String,
        val retailPrice: String? = null,
        val supplyPrice: String? = null,
        val display: String, // T or F
        val selling: String, // T or F
        val productCondition: String, // N(new), U(used), R(refurbished)
        val productUsedMonth: Int? = null,
        val summaryDescription: String? = null,
        val productTag: List<String>? = null,
        val simpleDescription: String? = null,
        val description: String? = null,
        val mobileDescription: String? = null,
        val separatedMobileDescription: String? = null,
        val detailImage: String? = null,
        val listImage: String? = null,
        val tinyImage: String? = null,
        val smallImage: String? = null,
        val additionalImages: List<AdditionalImage>? = null,
        val stockQuantity: Int,
        val brandCode: String? = null,
        val manufacturerCode: String? = null,
        val trendCode: String? = null,
        val category: List<Category>? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val customProductCode: String? = null,
        val customVariantCode: String? = null,
        val options: List<ProductOption>? = null,
        val variants: List<ProductVariant>? = null
)

data class AdditionalImage(
        val big: String? = null,
        val medium: String? = null,
        val small: String? = null
)

data class Category(
        val categoryNo: Int,
        val categoryDepth: Int,
        val categoryName: String,
        val fullCategoryName: CategoryName? = null
)

data class CategoryName(
        @JsonProperty("1") val depth1: String? = null,
        @JsonProperty("2") val depth2: String? = null,
        @JsonProperty("3") val depth3: String? = null,
        @JsonProperty("4") val depth4: String? = null
)

data class ProductOption(
        val optionName: String,
        val optionValue: List<String>
)

data class ProductVariant(
        val variantCode: String,
        val options: List<VariantOption>,
        val price: String? = null,
        val stockQuantity: Int
)

data class VariantOption(
        val name: String,
        val value: String
)