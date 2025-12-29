package com.acp.psp.adapter.outbound.kakaopay.dto

import com.fasterxml.jackson.annotation.JsonProperty

/** 카카오페이 결제 준비 응답 */
data class KakaoPayReadyResponse(
        val tid: String, // 결제 고유 번호
        @JsonProperty("next_redirect_pc_url") val nextRedirectPcUrl: String, // PC 웹용 결제 페이지
        @JsonProperty("next_redirect_mobile_url")
        val nextRedirectMobileUrl: String, // 모바일 웹용 결제 페이지
        @JsonProperty("next_redirect_app_url") val nextRedirectAppUrl: String, // 모바일 앱용 결제 페이지
        @JsonProperty("created_at") val createdAt: String // 결제 준비 요청 시각
)

/** 카카오페이 결제 승인 응답 */
data class KakaoPayApproveResponse(
        val aid: String, // 요청 고유 번호
        val tid: String, // 결제 고유 번호
        val cid: String, // 가맹점 코드
        @JsonProperty("partner_order_id") val partnerOrderId: String, // 가맹점 주문번호
        @JsonProperty("partner_user_id") val partnerUserId: String, // 가맹점 회원 id
        @JsonProperty("payment_method_type") val paymentMethodType: String, // 결제 수단 (CARD, MONEY)
        val amount: KakaoPayAmount, // 결제 금액 정보
        @JsonProperty("card_info") val cardInfo: KakaoPayCardInfo? = null, // 카드 결제 시 상세 정보
        @JsonProperty("item_name") val itemName: String? = null, // 상품 이름
        @JsonProperty("quantity") val quantity: Int? = null, // 상품 수량
        @JsonProperty("approved_at") val approvedAt: String // 결제 완료 시각
)

data class KakaoPayAmount(
        val total: Int, // 전체 결제 금액
        val tax_free: Int, // 비과세 금액
        val vat: Int, // 부가세 금액
        val point: Int, // 포인트 금액
        val discount: Int // 할인 금액
)

data class KakaoPayCardInfo(
        @JsonProperty("purchase_corp") val purchaseCorp: String, // 매입 카드사 한글명
        @JsonProperty("issuer_corp") val issuerCorp: String, // 발급 카드사 한글명
        @JsonProperty("bin") val bin: String, // 카드 BIN
        @JsonProperty("card_type") val cardType: String, // 카드 타입
        @JsonProperty("install_month") val installMonth: String, // 할부 개월 수
        @JsonProperty("approved_id") val approvedId: String // 카드사 승인번호
)
