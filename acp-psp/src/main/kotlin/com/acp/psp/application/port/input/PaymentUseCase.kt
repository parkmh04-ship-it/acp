package com.acp.psp.application.port.input

import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse

/** 결제 처리 Use Case (Input Port) */
interface PaymentUseCase {
    suspend fun preparePayment(request: PaymentPrepareRequest): PaymentPrepareResponse
}
