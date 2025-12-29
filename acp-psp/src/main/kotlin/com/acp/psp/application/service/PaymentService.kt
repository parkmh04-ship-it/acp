package com.acp.psp.application.service

import com.acp.psp.application.port.input.PaymentUseCase
import com.acp.psp.application.port.output.PaymentProvider
import com.acp.psp.application.port.output.PaymentRepositoryPort
import com.acp.psp.generated.jooq.tables.pojos.Payments
import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse
import java.math.BigDecimal
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 결제 처리 서비스 (Application Service) */
@Service
class PaymentService(
        private val paymentRepositoryPort: PaymentRepositoryPort,
        private val paymentProvider: PaymentProvider
) : PaymentUseCase {

        @Transactional
        override suspend fun preparePayment(
                request: PaymentPrepareRequest
        ): PaymentPrepareResponse {
                // 1. 멱등성 체크 (기존 주문번호로 생성된 결제가 있는지 확인)
                val existingRecord =
                        paymentRepositoryPort.findByMerchantOrderId(request.merchantOrderId)

                if (existingRecord != null) {
                        return PaymentPrepareResponse(
                                paymentId = existingRecord.id!!,
                                merchantOrderId = existingRecord.merchantOrderId!!,
                                redirectUrl =
                                        "https://mock-kakaopay.com/pay/${existingRecord.pgTid}", // TODO: 실제 저장된 URL 사용 고려
                                status = existingRecord.status!!
                        )
                }

                // 2. 외부 PG사(카카오페이 등) 결제 준비 요청
                val prepareResult = paymentProvider.prepare(request)

                // 3. 결제 정보 생성 및 DB 저장
                val paymentId = UUID.randomUUID().toString()
                val payment =
                        Payments(
                                id = paymentId,
                                merchantOrderId = request.merchantOrderId,
                                amount = BigDecimal.valueOf(request.amount),
                                currency = request.currency,
                                status = "READY",
                                pgTid = prepareResult.pgTid
                        )

                paymentRepositoryPort.save(payment)

                // 4. 최종 응답 반환
                return PaymentPrepareResponse(
                        paymentId = paymentId,
                        merchantOrderId = request.merchantOrderId,
                        redirectUrl = prepareResult.redirectUrl,
                        status = "READY"
                )
        }
}
