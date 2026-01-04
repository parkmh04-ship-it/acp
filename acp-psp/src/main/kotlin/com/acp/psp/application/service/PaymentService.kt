package com.acp.psp.application.service

import com.acp.psp.application.port.input.PaymentUseCase
import com.acp.psp.application.port.output.PaymentProvider
import com.acp.psp.application.port.output.PaymentRepositoryPort
import com.acp.psp.generated.jooq.tables.pojos.Payments
import com.acp.schema.payment.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClientRequestException

private val logger = KotlinLogging.logger {}

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
                // 1. 멱등성 체크 (기존 주문번호로 생성된 PREPARE 결제가 있는지 확인)
                val existingRecord =
                        paymentRepositoryPort.findLastByMerchantOrderIdAndType(request.merchantOrderId, "PREPARE")

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

                // 3. 결제 정보 생성 및 DB 저장 (불변: INSERT)
                val paymentId = UUID.randomUUID().toString()
                val payment =
                        Payments(
                                id = paymentId,
                                merchantOrderId = request.merchantOrderId,
                                orgPaymentId = null,
                                type = "PREPARE",
                                status = "READY",
                                amount = request.amount,
                                taxFreeAmount = 0L, // TODO: 요청에 포함되면 매핑
                                currency = request.currency,
                                pgProvider = paymentProvider.providerName,
                                pgTid = prepareResult.pgTid,
                                createdAt = OffsetDateTime.now()
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

        @Transactional
        override suspend fun approvePayment(request: PaymentApproveRequest): PaymentApproveResponse {
                // 1. 원본 결제(PREPARE) 조회
                val prepareRecord = paymentRepositoryPort.findLastByMerchantOrderIdAndType(request.merchantOrderId, "PREPARE")
                    ?: throw IllegalArgumentException("Payment PREPARE record not found for order: ${request.merchantOrderId}")

                // 2. 이미 승인된 건인지 확인 (APPROVE 레코드 존재 여부)
                val existingApprove = paymentRepositoryPort.findLastByMerchantOrderIdAndType(request.merchantOrderId, "APPROVE")
                if (existingApprove != null && existingApprove.status == "SUCCESS") {
                    logger.info { "Payment already approved: ${existingApprove.id}" }
                    return PaymentApproveResponse(
                        paymentId = existingApprove.id!!,
                        status = "COMPLETED",
                        totalAmount = existingApprove.amount!!.toLong()
                    )
                }

                try {
                    val approval = paymentProvider.approve(prepareRecord.pgTid!!, request.pgToken)
                    
                    // 3. 승인 정보 저장 (불변: INSERT)
                    val approveId = UUID.randomUUID().toString()
                    val approvePayment = Payments(
                        id = approveId,
                        merchantOrderId = request.merchantOrderId,
                        orgPaymentId = prepareRecord.id, // 원본(PREPARE) 참조
                        type = "APPROVE",
                        status = "SUCCESS",
                        amount = approval.amount,
                        currency = prepareRecord.currency, // 원본 통화 유지
                        pgProvider = paymentProvider.providerName,
                        pgTid = approval.pgTid,
                        paymentMethodType = approval.paymentMethod,
                        cardIssuer = approval.cardIssuer,
                        createdAt = OffsetDateTime.now()
                    )
                    paymentRepositoryPort.save(approvePayment)
                    
                    return PaymentApproveResponse(
                        paymentId = approveId,
                        status = "COMPLETED",
                        approvedAt = approval.approvedAt,
                        totalAmount = approval.amount,
                        method = approval.paymentMethod,
                        cardInfo = approval.cardIssuer?.let { CardInfo(it) }
                    )

                } catch (e: Exception) {
                    logger.error(e) { "Payment approval failed" }
                    
                    // 망취소 로직 (Network Cancellation)
                    if (isNetworkError(e)) {
                        handleNetCancel(prepareRecord)
                    }
                    // 실패 기록도 남겨야 하나? (사용자 요구: 결제 상태 업데이트보다는 불변)
                    // 실패 이력도 남기는 것이 좋음.
                    val failId = UUID.randomUUID().toString()
                    val failPayment = Payments(
                        id = failId,
                        merchantOrderId = request.merchantOrderId,
                        orgPaymentId = prepareRecord.id,
                        type = "APPROVE",
                        status = "FAIL",
                        amount = prepareRecord.amount,
                        currency = prepareRecord.currency,
                        pgProvider = paymentProvider.providerName,
                        pgTid = prepareRecord.pgTid,
                        createdAt = OffsetDateTime.now()
                    )
                    paymentRepositoryPort.save(failPayment)
                    
                    throw e
                }
        }

        private suspend fun handleNetCancel(prepareRecord: Payments) {
            logger.warn { "Executing Net Cancel for payment: ${prepareRecord.pgTid}" }
            try {
                val statusInfo = paymentProvider.checkStatus(prepareRecord.pgTid!!)
                if (statusInfo.status == "PAID") { // 카카오페이 쪽은 결제됨
                    logger.info { "Payment was PAID at PG, canceling now..." }
                    paymentProvider.cancel(
                        prepareRecord.pgTid!!, 
                        prepareRecord.amount!!.toLong(), 
                        "Net Cancel due to system timeout"
                    )
                    
                    // 취소 이력 저장
                    val cancelId = UUID.randomUUID().toString()
                    val cancelPayment = Payments(
                        id = cancelId,
                        merchantOrderId = prepareRecord.merchantOrderId,
                        orgPaymentId = prepareRecord.id, // PREPARE 참조 (APPROVE 레코드가 없으므로)
                        type = "CANCEL",
                        status = "SUCCESS",
                        amount = prepareRecord.amount,
                        currency = prepareRecord.currency,
                        pgProvider = paymentProvider.providerName,
                        pgTid = prepareRecord.pgTid,
                        createdAt = OffsetDateTime.now()
                    )
                    paymentRepositoryPort.save(cancelPayment)
                    
                } else {
                    logger.info { "Payment status at PG is ${statusInfo.status}, no cancel needed" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Net Cancel failed!" }
                // 이 경우 시스템 알람 필요
            }
        }

        private fun isNetworkError(e: Exception): Boolean {
            return e is WebClientRequestException || 
                   e.cause is java.net.SocketTimeoutException ||
                   e.message?.contains("timeout", ignoreCase = true) == true
        }
}
