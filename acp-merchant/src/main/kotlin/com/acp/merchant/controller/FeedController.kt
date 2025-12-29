package com.acp.merchant.adapter.inbound.web

import com.acp.merchant.application.port.input.GetProductFeedUseCase
import com.acp.schema.feed.ProductFeedItem
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

/**
 * Product Feed API Controller (Inbound Adapter)
 *
 * 헥사고날 아키텍처의 Primary/Driving Adapter로, HTTP 요청을 받아 Input Port(Use Case)를 호출합니다.
 *
 * OpenAI ACP Product Feed Spec:
 * @see https://developers.openai.com/commerce/specs/feed
 */
@RestController
@RequestMapping
class FeedController(private val getProductFeedUseCase: GetProductFeedUseCase) {

    /**
     * GET /feed
     *
     * 상품 피드 조회 엔드포인트
     *
     * @param q 검색 키워드 (선택)
     * @param limit 조회할 상품 개수 (기본: 100)
     * @param offset 시작 위치 (기본: 0)
     * @return ACP 포맷의 상품 목록
     */
    @GetMapping("/feed")
    suspend fun getFeed(
            @RequestParam(required = false) q: String?,
            @RequestParam(defaultValue = "100") limit: Int,
            @RequestParam(defaultValue = "0") offset: Int
    ): List<ProductFeedItem> {
        logger.info { "GET /feed - q=$q, limit=$limit, offset=$offset" }

        return if (q.isNullOrBlank()) {
            // 전체 상품 조회
            getProductFeedUseCase.execute(limit = limit, offset = offset)
        } else {
            // 키워드 검색
            getProductFeedUseCase.search(keyword = q, limit = limit, offset = offset)
        }
    }
}
