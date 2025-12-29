package com.acp.psp.application.service

import com.acp.psp.application.port.input.PaymentUseCase
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
class PaymentService(private val paymentRepositoryPort: PaymentRepositoryPort) : PaymentUseCase {

    @Transactional
    override suspend fun preparePayment(request: PaymentPrepareRequest): PaymentPrepareResponse {
        // 1. Check Idempotency
        val existingRecord = paymentRepositoryPort.findByMerchantOrderId(request.merchantOrderId)

        if (existingRecord != null) {
            return PaymentPrepareResponse(
                    paymentId = existingRecord.id!!,
                    merchantOrderId = existingRecord.merchantOrderId!!,
                    redirectUrl = "https://mock-kakaopay.com/pay/${existingRecord.pgTid}",
                    status = existingRecord.status!!
            )
        }

        // 2. Create New Payment
        val newPaymentId = UUID.randomUUID().toString()
        val mockPgTid = "TID_${UUID.randomUUID().toString().replace("-", "").take(10)}"

        val payment =
                Payments(
                        id = newPaymentId,
                        merchantOrderId = request.merchantOrderId,
                        amount = BigDecimal.valueOf(request.amount),
                        currency = request.currency,
                        status = "READY",
                        pgTid = mockPgTid
                )

        paymentRepositoryPort.save(payment)

        return PaymentPrepareResponse(
                paymentId = newPaymentId,
                merchantOrderId = request.merchantOrderId,
                redirectUrl = "https://mock-kakaopay.com/pay/$mockPgTid",
                status = "READY"
        )
    }
}
