package com.acp.merchant.domain.service

import com.acp.merchant.adapter.outbound.cafe24.dto.Cafe24Product
import com.acp.schema.feed.Availability
import com.acp.schema.feed.Condition
import com.acp.schema.feed.ProductFeedItem
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Cafe24 상품 데이터를 ACP(Agentic Commerce Protocol) 포맷으로 변환하는 서비스
 *
 * OpenAI Product Feed Spec에 맞게 Cafe24 API 응답을 변환합니다.
 *
 * @see https://developers.openai.com/commerce/specs/feed
 */
@Service
class Cafe24ToAcpConverter(@Value("\${cafe24.mall-id}") private val mallId: String) {

    /** Cafe24 상품을 ACP ProductFeedItem으로 변환 */
    fun convert(cafe24Product: Cafe24Product): ProductFeedItem {
        return ProductFeedItem(
                id = generateProductId(cafe24Product.productNo),
                title = cafe24Product.productName,
                description = extractDescription(cafe24Product),
                link = generateProductLink(cafe24Product.productNo),
                imageLink = cafe24Product.detailImage ?: cafe24Product.listImage ?: "",
                price = formatPrice(cafe24Product.price),
                availability = mapAvailability(cafe24Product),
                productCategory = extractCategory(cafe24Product),
                brand = cafe24Product.brandCode,
                condition = mapCondition(cafe24Product.productCondition),
                sellerName = mallId
        )
    }

    /**
     * 상품 ID 생성
     *
     * Cafe24 상품 번호 앞에 "cafe24_" 접두사를 붙여 고유성 보장
     */
    private fun generateProductId(productNo: Long): String {
        return "cafe24_$productNo"
    }

    /**
     * 상품 설명 추출
     *
     * 우선순위: summary_description > simple_description > description HTML 태그 제거
     */
    private fun extractDescription(product: Cafe24Product): String {
        val rawDescription =
                product.summaryDescription
                        ?: product.simpleDescription ?: product.description ?: "상품 설명이 없습니다."

        return stripHtmlTags(rawDescription)
    }

    /** HTML 태그 제거 */
    private fun stripHtmlTags(html: String): String {
        return html.replace(Regex("<[^>]*>"), "") // HTML 태그 제거
                .replace(Regex("&nbsp;"), " ") // &nbsp; 제거
                .replace(Regex("\\s+"), " ") // 연속된 공백 제거
                .trim()
    }

    /** 상품 링크 생성 */
    private fun generateProductLink(productNo: Long): String {
        return "https://$mallId.cafe24.com/product/detail.html?product_no=$productNo"
    }

    /**
     * 가격 포맷팅
     *
     * Cafe24: "89000" (String) ACP: "89000" (현재는 String, 향후 Number + Currency 분리 고려)
     */
    private fun formatPrice(price: String): String {
        return try {
            // 숫자만 추출
            price.replace(Regex("[^0-9]"), "")
        } catch (e: Exception) {
            logger.warn(e) { "Failed to format price: $price" }
            price
        }
    }

    /**
     * 재고 상태 매핑
     *
     * Cafe24 → ACP:
     * - selling=T && stock_quantity>0 → IN_STOCK
     * - selling=F → OUT_OF_STOCK
     * - selling=T && stock_quantity=0 → PREORDER (또는 OUT_OF_STOCK)
     */
    private fun mapAvailability(product: Cafe24Product): Availability {
        return when {
            product.selling == "F" -> Availability.OUT_OF_STOCK
            product.selling == "T" && product.stockQuantity > 0 -> Availability.IN_STOCK
            product.selling == "T" && product.stockQuantity == 0 -> Availability.PREORDER
            else -> Availability.OUT_OF_STOCK
        }
    }

    /**
     * 카테고리 추출
     *
     * Cafe24의 카테고리 계층 구조를 경로 형태로 변환 예: "패션 > 남성의류 > 티셔츠"
     */
    private fun extractCategory(product: Cafe24Product): String? {
        val category = product.category?.firstOrNull() ?: return null

        return category.fullCategoryName?.let { fullName ->
            listOfNotNull(fullName.depth1, fullName.depth2, fullName.depth3, fullName.depth4)
                    .joinToString(" > ")
        }
                ?: category.categoryName
    }

    /**
     * 상품 상태 매핑
     *
     * Cafe24 → ACP:
     * - N (new) → NEW
     * - U (used) → USED
     * - R (refurbished) → REFURBISHED
     */
    private fun mapCondition(productCondition: String): Condition {
        return when (productCondition.uppercase()) {
            "N" -> Condition.NEW
            "U" -> Condition.USED
            "R" -> Condition.REFURBISHED
            else -> {
                logger.warn { "Unknown product condition: $productCondition, defaulting to NEW" }
                Condition.NEW
            }
        }
    }

    /** 여러 상품을 일괄 변환 */
    fun convertAll(cafe24Products: List<Cafe24Product>): List<ProductFeedItem> {
        logger.info { "Converting ${cafe24Products.size} Cafe24 products to ACP format" }

        return cafe24Products
                .map { product ->
                    try {
                        convert(product)
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to convert product: ${product.productNo}" }
                        null
                    }
                }
                .filterNotNull()
    }
}
