package com.acp.psp.service

import com.acp.psp.generated.jooq.tables.references.PAYMENTS
import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class PaymentService(
    private val dsl: DSLContext
) {

    @Transactional
    suspend fun preparePayment(request: PaymentPrepareRequest): PaymentPrepareResponse = withContext(Dispatchers.IO) {
        // 1. Check Idempotency (Existing payment for this merchant order)
        val existingRecord = dsl.selectFrom(PAYMENTS)
            .where(PAYMENTS.MERCHANT_ORDER_ID.eq(request.merchantOrderId))
            .fetchOne()

        if (existingRecord != null) {
            // If already exists, return the existing state (Simulation)
            return@withContext PaymentPrepareResponse(
                paymentId = existingRecord.id!!,
                merchantOrderId = existingRecord.merchantOrderId!!,
                redirectUrl = "https://mock-kakaopay.com/pay/${existingRecord.pgTid}", // Mock URL
                status = existingRecord.status!!
            )
        }

        // 2. Create New Payment Record
        val newPaymentId = UUID.randomUUID().toString()
        val mockPgTid = "TID_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val mockRedirectUrl = "https://mock-kakaopay.com/pay/$mockPgTid"

        dsl.insertInto(PAYMENTS)
            .set(PAYMENTS.ID, newPaymentId)
            .set(PAYMENTS.MERCHANT_ORDER_ID, request.merchantOrderId)
            .set(PAYMENTS.AMOUNT, BigDecimal.valueOf(request.amount))
            .set(PAYMENTS.CURRENCY, request.currency)
            .set(PAYMENTS.STATUS, "READY")
            .set(PAYMENTS.PG_TID, mockPgTid)
            .execute()

        // 3. Return Response
        PaymentPrepareResponse(
            paymentId = newPaymentId,
            merchantOrderId = request.merchantOrderId,
            redirectUrl = mockRedirectUrl,
            status = "READY"
        )
    }
}
