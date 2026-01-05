package com.acp.psp.application.port.input

import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse
import com.acp.schema.payment.PaymentApproveRequest
import com.acp.schema.payment.PaymentApproveResponse
import com.acp.schema.payment.PaymentCancelRequest
import com.acp.schema.payment.PaymentCancelResponse

/** 결제 처리 Use Case (Input Port) */
interface PaymentUseCase {
    suspend fun preparePayment(request: PaymentPrepareRequest): PaymentPrepareResponse
    suspend fun approvePayment(request: PaymentApproveRequest): PaymentApproveResponse
    suspend fun cancelPayment(request: PaymentCancelRequest): PaymentCancelResponse
}
