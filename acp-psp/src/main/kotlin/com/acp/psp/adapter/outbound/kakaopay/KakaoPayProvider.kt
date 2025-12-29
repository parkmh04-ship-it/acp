package com.acp.psp.adapter.outbound.kakaopay

import com.acp.psp.adapter.outbound.kakaopay.dto.KakaoPayApproveResponse
import com.acp.psp.adapter.outbound.kakaopay.dto.KakaoPayReadyResponse
import com.acp.psp.application.port.output.*
import com.acp.schema.payment.PaymentPrepareRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

private val logger = KotlinLogging.logger {}

/**
 * 카카오페이 결제 서비스 제공자 구현체 (Outbound Adapter)
 *
 * 카카오페이 API를 사용하여 결제 준비 및 승인을 처리합니다.
 */
@Component
class KakaoPayProvider(
        private val kakaoPayWebClient: WebClient,
        @Value("\${kakaopay.cid:TC0ONETIME}") private val cid: String
) : PaymentProvider {

    override val providerName: String = "KAKAOPAY"

    /**
     * 카카오페이 결제 준비 (Ready API)
     *
     * @see https://developers.kakaopay.com/docs/payment/online/single-payment#prepare-payment
     */
    override suspend fun prepare(request: PaymentPrepareRequest): PaymentPrepareResult {
        logger.info { "Preparing KakaoPay payment for order: ${request.merchantOrderId}" }

        val itemName =
                if (request.items.size > 1) {
                    "${request.items.first().name} 외 ${request.items.size - 1}건"
                } else {
                    request.items.firstOrNull()?.name ?: "상품"
                }
        val totalQuantity = request.items.sumOf { it.quantity }

        val body =
                mapOf(
                        "cid" to cid,
                        "partner_order_id" to request.merchantOrderId,
                        "partner_user_id" to "ACP_USER", // TODO: 실제 사용자 ID 매핑
                        "item_name" to itemName,
                        "quantity" to totalQuantity.toString(),
                        "total_amount" to request.amount.toString(),
                        "tax_free_amount" to "0",
                        "approval_url" to
                                "http://localhost:8080/api/v1/payments/success", // TODO: 실제 콜백 URL
                        // (Merchant 쪽
                        // 엔드포인트)
                        "cancel_url" to "http://localhost:8080/api/v1/payments/cancel",
                        "fail_url" to "http://localhost:8080/api/v1/payments/fail"
                )

        val response =
                kakaoPayWebClient
                        .post()
                        .uri("/online/v1/payment/ready")
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono<KakaoPayReadyResponse>()
                        .awaitSingle()

        logger.info { "KakaoPay payment ready: ${response.tid}" }

        return PaymentPrepareResult(pgTid = response.tid, redirectUrl = response.nextRedirectPcUrl)
    }

    /**
     * 카카오페이 결제 승인 (Approve API)
     *
     * @see https://developers.kakaopay.com/docs/payment/online/single-payment#approve-payment
     */
    override suspend fun approve(paymentId: String, pgToken: String): PaymentApproval {
        logger.info { "Approving KakaoPay payment: $paymentId" }

        // TODO: DB에서 tid 가져오는 로직 필요. 여기서는 예시로 paymentId를 통해 조회한다고 가정
        val tid = "TODO_GET_FROM_DB"

        val body =
                mapOf(
                        "cid" to cid,
                        "tid" to tid,
                        "partner_order_id" to paymentId,
                        "partner_user_id" to "ACP_USER",
                        "pg_token" to pgToken
                )

        val response =
                kakaoPayWebClient
                        .post()
                        .uri("/online/v1/payment/approve")
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono<KakaoPayApproveResponse>()
                        .awaitSingle()

        logger.info { "KakaoPay payment approved: ${response.tid}" }

        return PaymentApproval(
                paymentId = paymentId,
                pgTid = response.tid,
                approvedAt = response.approvedAt,
                amount = response.amount.total.toLong(),
                cardIssuer = response.cardInfo?.issuerCorp,
                paymentMethod = response.paymentMethodType
        )
    }

    override suspend fun cancel(
            paymentId: String,
            amount: Long,
            reason: String
    ): PaymentCancelResult {
        // TODO: 카카오페이 취소 API 구현 (Phase 3.1)
        throw UnsupportedOperationException("Not yet implemented")
    }
}
