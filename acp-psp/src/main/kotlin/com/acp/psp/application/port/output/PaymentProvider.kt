package com.acp.psp.application.port.output

import com.acp.schema.payment.PaymentPrepareRequest

/**
 * 외부 결제 서비스 제공자 인터페이스 (Output Port)
 *
 * 카카오페이, 토스페이 등 실제 PG사와의 연동을 추상화합니다.
 */
interface PaymentProvider {

    /** 결제 서비스 제공자 식별자 (예: KAKAOPAY) */
    val providerName: String

    /**
     * 결제 준비 (Ready)
     *
     * 외부 PG사에 결제 요청을 보내고, 리다이렉트 URL과 트랜잭션 ID 등을 받아오는 내부 처리 결과를 반환합니다.
     */
    suspend fun prepare(request: PaymentPrepareRequest): PaymentPrepareResult

    /**
     * 결제 승인 (Approve)
     *
     * 사용자가 결제를 승인한 후 발급된 토큰을 사용하여 최종 결제 승인을 요청합니다.
     */
    suspend fun approve(paymentId: String, pgToken: String): PaymentApproval

    /** 결제 취소 (Cancel) */
    suspend fun cancel(paymentId: String, amount: Long, reason: String): PaymentCancelResult
}

/** 결제 승인 결과 데이터 클래스 */
data class PaymentApproval(
        val paymentId: String,
        val pgTid: String,
        val approvedAt: String,
        val amount: Long,
        val cardIssuer: String? = null,
        val paymentMethod: String? = null
)

/** 결제 취소 결과 데이터 클래스 */
data class PaymentCancelResult(
        val paymentId: String,
        val canceledAt: String,
        val amount: Long,
        val status: String
)

/** 결제 준비 결과 데이터 클래스 */
data class PaymentPrepareResult(
        val pgTid: String,
        val redirectUrl: String,
        val status: String = "READY"
)
