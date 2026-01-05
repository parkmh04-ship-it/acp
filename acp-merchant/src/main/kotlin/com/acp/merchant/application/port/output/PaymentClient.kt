package com.acp.merchant.application.port.output

import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse
import com.acp.schema.payment.PaymentApproveRequest
import com.acp.schema.payment.PaymentApproveResponse

interface PaymentClient {
    suspend fun preparePayment(request: PaymentPrepareRequest): PaymentPrepareResponse
    suspend fun approvePayment(request: PaymentApproveRequest): PaymentApproveResponse
    suspend fun cancelPayment(request: com.acp.schema.payment.PaymentCancelRequest): com.acp.schema.payment.PaymentCancelResponse
}
