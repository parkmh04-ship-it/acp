package com.acp.psp.adapter.outbound.kakaopay.dto

/** 카카오페이 결제 준비 요청 */
data class KakaoPayReadyRequest(
        val cid: String,
        val partnerOrderId: String,
        val partnerUserId: String,
        val itemName: String,
        val quantity: Int,
        val totalAmount: Int,
        val taxFreeAmount: Int,
        val approvalUrl: String,
        val cancelUrl: String,
        val failUrl: String
)

/** 카카오페이 결제 준비 응답 */
data class KakaoPayReadyResponse(
        val tid: String, // 결제 고유 번호
        val nextRedirectPcUrl: String, // PC 웹용 결제 페이지
        val nextRedirectMobileUrl: String, // 모바일 웹용 결제 페이지
        val nextRedirectAppUrl: String, // 모바일 앱용 결제 페이지
        val createdAt: String // 결제 준비 요청 시각
)

/** 카카오페이 결제 승인 요청 */
data class KakaoPayApproveRequest(
        val cid: String,
        val tid: String,
        val partnerOrderId: String,
        val partnerUserId: String,
        val pgToken: String
)

/** 카카오페이 결제 승인 응답 */
data class KakaoPayApproveResponse(
        val aid: String, // 요청 고유 번호
        val tid: String, // 결제 고유 번호
        val cid: String, // 가맹점 코드
        val partnerOrderId: String, // 가맹점 주문번호
        val partnerUserId: String, // 가맹점 회원 id
        val paymentMethodType: String, // 결제 수단 (CARD, MONEY)
        val amount: KakaoPayAmount, // 결제 금액 정보
        val cardInfo: KakaoPayCardInfo? = null, // 카드 결제 시 상세 정보
        val itemName: String? = null, // 상품 이름
        val quantity: Int? = null, // 상품 수량
        val approvedAt: String // 결제 완료 시각
)

data class KakaoPayAmount(
        val total: Int, // 전체 결제 금액
        val taxFree: Int, // 비과세 금액
        val vat: Int, // 부가세 금액
        val point: Int, // 포인트 금액
        val discount: Int // 할인 금액
)

data class KakaoPayCardInfo(
        val purchaseCorp: String, // 매입 카드사 한글명
        val issuerCorp: String, // 발급 카드사 한글명
        val bin: String, // 카드 BIN
        val cardType: String, // 카드 타입
        val installMonth: String, // 할부 개월 수
        val approvedId: String // 카드사 승인번호
)