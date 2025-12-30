package com.acp.merchant.application.port.output

import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse

interface PaymentClient {
    suspend fun preparePayment(request: PaymentPrepareRequest): PaymentPrepareResponse
}
