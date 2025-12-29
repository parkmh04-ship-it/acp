package com.acp.merchant.application.port.output

import com.acp.merchant.adapter.outbound.cafe24.dto.Cafe24Product
import com.acp.merchant.adapter.outbound.cafe24.dto.Cafe24ProductsResponse

/**
 * Cafe24 상품 API 클라이언트 인터페이스 (Output Port)
 * 
 * 헥사고날 아키텍처의 Output Port로, 외부 Cafe24 API와의 통신을 추상화합니다.
 */
interface Cafe24ProductClient {
    
    /**
     * 상품 목록 조회
     * 
     * @param limit 조회할 상품 개수 (기본: 100, 최대: 100)
     * @param offset 시작 위치 (페이지네이션)
     * @param display 진열 여부 필터 (T: 진열, F: 미진열, null: 전체)
     * @param selling 판매 여부 필터 (T: 판매중, F: 판매안함, null: 전체)
     * @return Cafe24 상품 목록 응답
     */
    suspend fun getProducts(
        limit: Int = 100,
        offset: Int = 0,
        display: String? = "T",
        selling: String? = "T"
    ): Cafe24ProductsResponse
    
    /**
     * 상품 상세 조회
     * 
     * @param productNo 상품 번호
     * @return Cafe24 상품 상세 정보
     */
    suspend fun getProduct(productNo: Long): Cafe24Product
    
    /**
     * 상품 검색
     * 
     * @param keyword 검색 키워드
     * @param limit 조회할 상품 개수
     * @param offset 시작 위치
     * @return Cafe24 상품 목록 응답
     */
    suspend fun searchProducts(
        keyword: String,
        limit: Int = 100,
        offset: Int = 0
    ): Cafe24ProductsResponse
}
