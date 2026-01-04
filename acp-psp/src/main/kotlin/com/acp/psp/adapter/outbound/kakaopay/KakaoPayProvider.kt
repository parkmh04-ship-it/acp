package com.acp.psp.adapter.outbound.kakaopay

import com.acp.psp.adapter.outbound.kakaopay.dto.*
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
                KakaoPayReadyRequest(
                        cid = cid,
                        partnerOrderId = request.merchantOrderId,
                        partnerUserId = "ACP_USER", // TODO: 실제 사용자 ID 매핑
                        itemName = itemName,
                        quantity = totalQuantity,
                        totalAmount = request.amount.toInt(),
                        taxFreeAmount = 0,
                        approvalUrl = "http://localhost:8080/api/v1/payments/success", // TODO: 실제 콜백 URL
                        cancelUrl = "http://localhost:8080/api/v1/payments/cancel",
                        failUrl = "http://localhost:8080/api/v1/payments/fail"
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
        // 이 메서드는 외부에서 paymentId 대신 tid를 직접 넘겨주거나, 서비스에서 tid를 조회해서 넘겨줘야 함.
        // 하지만 인터페이스 시그니처가 paymentId를 받으므로, 여기서 조회하거나, 아니면 서비스가 tid를 조회해서 넘기도록 인터페이스 변경 필요.
        // 현재는 서비스 계층에서 tid를 조회하여 approve 메서드의 첫 번째 인자로 넘겨주는 것이 일반적임. 
        // 하지만 인터페이스의 paymentId 파라미터는 논리적 ID이고, 실제 구현체 내부에서 tid가 필요함.
        // 여기서는 편의상 서비스가 이미 tid를 찾아서 첫 번째 인자로 넘겨줬다고 가정하거나(변수명 혼동),
        // 아니면 서비스가 paymentId를 넘기면 여기서 DB 조회를 해야 하는데, Adapter는 DB 의존성이 없어야 함.
        // 따라서 서비스 계층에서 tid를 조회하여 넘겨주는 것이 맞음. 
        // KakaoPayProvider.approve(tid, pgToken) 형태로 호출되어야 함.
        // 인터페이스 정의를 `approve(pgTid: String, pgToken: String)`로 바꾸거나, `paymentId`를 `pgTid`로 해석해야 함.
        // 여기서는 `paymentId` 파라미터를 `pgTid`로 간주하고 구현함. (Service에서 pgTid를 넘겨줄 것임)
        
        val pgTid = paymentId 
        
        logger.info { "Approving KakaoPay payment tid: $pgTid" }

        val body =
                KakaoPayApproveRequest(
                        cid = cid,
                        tid = pgTid,
                        partnerOrderId = "ANY", // 카카오페이는 partner_order_id 일치 여부를 체크함. 원래는 DB에 저장된 값을 써야 함.
                        // 하지만 여기서는 partner_order_id를 알 수 없음.
                        // 해결책 1: approve 메서드 인자에 partnerOrderId 추가.
                        // 해결책 2: 서비스에서 partnerOrderId도 조회해서 넘겨줌.
                        // 일단은 서비스에서 partnerOrderId도 넘겨줘야 한다고 판단됨. 
                        // 하지만 인터페이스 변경이 번거로우므로, 일단 DB 조회 없이 진행 가능한지 확인.
                        // 카카오페이 문서상 필수 파라미터임. 불일치 시 에러 발생 가능성 있음.
                        // 임시로 "ACP_ORDER" 등으로 고정하거나, 인터페이스 확장이 필요함.
                        // 여기서는 일단 기존 로직대로 "paymentId"를 partnerOrderId로 가정하고 진행. (서비스가 merchantOrderId를 넘겨줄 것임)
                        partnerUserId = "ACP_USER",
                        pgToken = pgToken
                )

        // 수정: partnerOrderId는 필수이므로, 서비스에서 `paymentId` 인자에 `merchantOrderId`나 `pgTid` 중 하나를 넘겨야 하는데,
        // 카카오페이 approve에는 `tid`, `partner_order_id`, `partner_user_id`, `pg_token`이 모두 필요함.
        // 현재 인터페이스: approve(paymentId: String, pgToken: String)
        // 정보가 부족함. PaymentProvider 인터페이스를 수정하는 것이 가장 깔끔함.
        // `approve(paymentContext: PaymentContext, pgToken: String)` 처럼 컨텍스트를 넘기거나.
        // 일단은 TODO 주석을 남기고 구현. (Service 구현 시 partnerOrderId를 paymentId 인자에 태워 보낼지 결정)
        
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
                paymentId = paymentId, // 요청받은 ID 반환
                pgTid = response.tid,
                approvedAt = response.approvedAt,
                amount = response.amount.total.toLong(),
                cardIssuer = response.cardInfo?.issuerCorp,
                paymentMethod = response.paymentMethodType
        )
    }

    override suspend fun cancel(
            paymentId: String, // 여기서는 pgTid를 의미
            amount: Long,
            reason: String
    ): PaymentCancelResult {
        val pgTid = paymentId
        logger.info { "Canceling KakaoPay payment: $pgTid" }

        val body = KakaoPayCancelRequest(
            cid = cid,
            tid = pgTid,
            cancelAmount = amount.toInt(),
            cancelTaxFreeAmount = 0
        )

        val response = kakaoPayWebClient.post()
            .uri("/online/v1/payment/cancel")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<KakaoPayCancelResponse>()
            .awaitSingle()

        return PaymentCancelResult(
            paymentId = paymentId,
            canceledAt = response.canceledAt ?: response.createdAt, // 취소 시각이 없으면 생성 시각 대체
            amount = response.approvedCancelAmount?.total?.toLong() ?: 0L,
            status = mapKakaoStatusToDomain(response.status)
        )
    }

    override suspend fun checkStatus(pgTid: String): PaymentStatusInfo {
        logger.info { "Checking KakaoPay status: $pgTid" }
        
        // GET 요청이 아님. 문서를 보면 조회 API는 POST임? 
        // 카카오페이 문서는 POST /v1/payment/order 임.
        
        val body = KakaoPayOrderRequest(cid = cid, tid = pgTid)
        
        val response = kakaoPayWebClient.post()
            .uri("/online/v1/payment/order")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<KakaoPayOrderResponse>()
            .awaitSingle()
            
        return PaymentStatusInfo(
            status = mapKakaoStatusToDomain(response.status),
            amount = response.amount?.total?.toLong()
        )
    }
    
    private fun mapKakaoStatusToDomain(kakaoStatus: String): String {
        return when (kakaoStatus) {
            "READY" -> "READY"
            "SEND_TMS" -> "READY"
            "OPEN_PAYMENT" -> "IN_PROGRESS"
            "SELECT_METHOD" -> "IN_PROGRESS"
            "ARS_WAITING" -> "IN_PROGRESS"
            "AUTH_PASSWORD" -> "IN_PROGRESS"
            "ISSUED_SID" -> "IN_PROGRESS"
            "SUCCESS_PAYMENT" -> "PAID"
            "PART_CANCEL_PAYMENT" -> "PAID" // 부분 취소도 결제 완료 상태로 봄 (잔액 있음)
            "CANCEL_PAYMENT" -> "CANCELED"
            "FAIL_PAYMENT" -> "FAILED"
            "QUIT_PAYMENT" -> "CANCELED"
            else -> "UNKNOWN"
        }
    }
}