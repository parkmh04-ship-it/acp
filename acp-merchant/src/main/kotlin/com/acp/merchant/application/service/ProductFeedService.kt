package com.acp.merchant.application.service

import com.acp.merchant.application.port.input.GetProductFeedUseCase
import com.acp.merchant.application.port.output.Cafe24ProductClient
import com.acp.merchant.domain.service.Cafe24ToAcpConverter
import com.acp.schema.feed.ProductFeedItem
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * 상품 피드 조회 Use Case 구현체
 *
 * 헥사고날 아키텍처의 Application Service로, Input Port(GetProductFeedUseCase)를 구현하고 Output
 * Port(Cafe24ProductClient)를 사용합니다.
 *
 * 책임:
 * 1. Cafe24 API를 통해 상품 데이터 조회
 * 2. Cafe24 데이터를 ACP 포맷으로 변환
 * 3. 캐싱 및 성능 최적화 (향후)
 */
@Service
class ProductFeedService(
        private val cafe24ProductClient: Cafe24ProductClient,
        private val cafe24ToAcpConverter: Cafe24ToAcpConverter
) : GetProductFeedUseCase {

    /**
     * 전체 상품 피드 조회
     *
     * 플로우:
     * 1. Cafe24 API에서 상품 목록 조회 (Output Port 사용)
     * 2. Cafe24 데이터를 ACP 포맷으로 변환 (Domain Service 사용)
     * 3. ACP 포맷 상품 목록 반환
     */
    override suspend fun execute(limit: Int, offset: Int): List<ProductFeedItem> {
        logger.info { "Executing GetProductFeedUseCase: limit=$limit, offset=$offset" }

        return try {
            // 1. Cafe24에서 상품 조회 (Output Port)
            val cafe24Response =
                    cafe24ProductClient.getProducts(
                            limit = limit,
                            offset = offset,
                            display = "T", // 진열 중인 상품만
                            selling = "T" // 판매 중인 상품만
                    )

            logger.info { "Fetched ${cafe24Response.products.size} products from Cafe24" }

            // 2. ACP 포맷으로 변환 (Domain Service)
            val acpProducts = cafe24ToAcpConverter.convertAll(cafe24Response.products)

            logger.info { "Converted ${acpProducts.size} products to ACP format" }

            acpProducts
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute GetProductFeedUseCase" }
            emptyList()
        }
    }

    /**
     * 키워드로 상품 검색
     *
     * 플로우:
     * 1. Cafe24 API에서 상품 검색 (Output Port 사용)
     * 2. Cafe24 데이터를 ACP 포맷으로 변환
     * 3. ACP 포맷 상품 목록 반환
     */
    override suspend fun search(keyword: String, limit: Int, offset: Int): List<ProductFeedItem> {
        logger.info { "Searching products: keyword='$keyword', limit=$limit, offset=$offset" }

        return try {
            // 1. Cafe24에서 상품 검색 (Output Port)
            val cafe24Response =
                    cafe24ProductClient.searchProducts(
                            keyword = keyword,
                            limit = limit,
                            offset = offset
                    )

            logger.info { "Found ${cafe24Response.products.size} products matching '$keyword'" }

            // 2. ACP 포맷으로 변환 (Domain Service)
            val acpProducts = cafe24ToAcpConverter.convertAll(cafe24Response.products)

            logger.info { "Converted ${acpProducts.size} search results to ACP format" }

            acpProducts
        } catch (e: Exception) {
            logger.error(e) { "Failed to search products with keyword '$keyword'" }
            emptyList()
        }
    }
}
