package com.acp.merchant.application.port.input

import com.acp.schema.feed.ProductFeedItem

/**
 * 상품 피드 조회 Use Case (Input Port)
 *
 * 헥사고날 아키텍처의 Primary Port로, 애플리케이션의 비즈니스 로직을 정의합니다. 외부(Controller, CLI 등)에서 이 인터페이스를 통해 애플리케이션 로직을
 * 호출합니다.
 */
interface GetProductFeedUseCase {

    /**
     * 전체 상품 피드 조회
     *
     * @param limit 조회할 상품 개수
     * @param offset 시작 위치 (페이지네이션)
     * @return ACP 포맷의 상품 목록
     */
    suspend fun execute(limit: Int = 100, offset: Int = 0): List<ProductFeedItem>

    /**
     * 키워드로 상품 검색
     *
     * @param keyword 검색 키워드
     * @param limit 조회할 상품 개수
     * @param offset 시작 위치
     * @return ACP 포맷의 상품 목록
     */
    suspend fun search(keyword: String, limit: Int = 100, offset: Int = 0): List<ProductFeedItem>
}
