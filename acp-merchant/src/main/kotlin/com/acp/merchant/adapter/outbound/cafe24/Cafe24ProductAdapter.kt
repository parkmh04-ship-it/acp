package com.acp.merchant.adapter.outbound.cafe24

import com.acp.merchant.adapter.outbound.cafe24.dto.Cafe24Product
import com.acp.merchant.adapter.outbound.cafe24.dto.Cafe24ProductsResponse
import com.acp.merchant.application.port.output.Cafe24ProductClient
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

private val logger = KotlinLogging.logger {}

/**
 * Cafe24 상품 API 클라이언트 구현체 (Adapter)
 *
 * Cafe24 Open API를 호출하여 상품 정보를 조회합니다. 헥사고날 아키텍처의 Outbound Adapter 역할을 수행합니다.
 *
 * @see https://developers.cafe24.com/docs/ko/api/front/#products
 */
@Component
class Cafe24ProductAdapter(private val cafe24WebClient: WebClient) : Cafe24ProductClient {

    companion object {
        private const val PRODUCTS_PATH = "/api/v2/products"
        private const val DEFAULT_SHOP_NO = 1
    }

    /**
     * 상품 목록 조회
     *
     * Cafe24 API: GET /api/v2/products
     */
    override suspend fun getProducts(
            limit: Int,
            offset: Int,
            display: String?,
            selling: String?
    ): Cafe24ProductsResponse {
        logger.info { "Fetching products from Cafe24: limit=$limit, offset=$offset" }

        return try {
            cafe24WebClient
                    .get()
                    .uri { uriBuilder ->
                        uriBuilder
                                .path(PRODUCTS_PATH)
                                .queryParam("shop_no", DEFAULT_SHOP_NO)
                                .queryParam("limit", limit)
                                .queryParam("offset", offset)
                                .apply {
                                    display?.let { queryParam("display", it) }
                                    selling?.let { queryParam("selling", it) }
                                }
                                .build()
                    }
                    .retrieve()
                    .bodyToMono<Cafe24ProductsResponse>()
                    .doOnSuccess { response ->
                        logger.info {
                            "Successfully fetched ${response.products.size} products from Cafe24"
                        }
                    }
                    .doOnError { error ->
                        logger.error(error) { "Failed to fetch products from Cafe24" }
                    }
                    .awaitSingle()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching products from Cafe24" }
            // 에러 발생 시 빈 응답 반환 (또는 예외 재throw)
            Cafe24ProductsResponse(products = emptyList())
        }
    }

    /**
     * 상품 상세 조회
     *
     * Cafe24 API: GET /api/v2/products/{product_no}
     */
    override suspend fun getProduct(productNo: Long): Cafe24Product {
        logger.info { "Fetching product detail from Cafe24: productNo=$productNo" }

        return cafe24WebClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                            .path("$PRODUCTS_PATH/$productNo")
                            .queryParam("shop_no", DEFAULT_SHOP_NO)
                            .build()
                }
                .retrieve()
                .bodyToMono<Cafe24Product>()
                .doOnSuccess { product ->
                    logger.info { "Successfully fetched product: ${product.productName}" }
                }
                .doOnError { error ->
                    logger.error(error) { "Failed to fetch product $productNo from Cafe24" }
                }
                .awaitSingle()
    }

    /**
     * 상품 검색
     *
     * Cafe24 API에는 직접적인 검색 API가 없으므로, 전체 상품을 조회한 후 클라이언트 측에서 필터링합니다.
     *
     * TODO: 성능 개선을 위해 Cafe24 검색 API 또는 Elasticsearch 연동 고려
     */
    override suspend fun searchProducts(
            keyword: String,
            limit: Int,
            offset: Int
    ): Cafe24ProductsResponse {
        logger.info { "Searching products in Cafe24: keyword='$keyword'" }

        // 전체 상품 조회
        val allProducts = getProducts(limit = limit, offset = offset)

        // 키워드로 필터링
        val filteredProducts =
                allProducts.products.filter { product ->
                    product.productName.contains(keyword, ignoreCase = true) ||
                            product.description?.contains(keyword, ignoreCase = true) == true ||
                            product.summaryDescription?.contains(keyword, ignoreCase = true) == true
                }

        logger.info { "Found ${filteredProducts.size} products matching keyword '$keyword'" }

        return Cafe24ProductsResponse(products = filteredProducts)
    }
}
