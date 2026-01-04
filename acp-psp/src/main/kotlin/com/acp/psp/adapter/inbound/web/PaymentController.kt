package com.acp.psp.adapter.inbound.web

import com.acp.psp.application.port.input.PaymentUseCase
import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse
import com.acp.schema.payment.PaymentApproveRequest
import com.acp.schema.payment.PaymentApproveResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** 결제 API 컨트롤러 (Inbound Adapter) */
@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(private val paymentUseCase: PaymentUseCase) {

    @PostMapping("/prepare")
    suspend fun preparePayment(
            @RequestBody request: PaymentPrepareRequest
    ): PaymentPrepareResponse {
        return paymentUseCase.preparePayment(request)
    }

    @PostMapping("/approve")
    suspend fun approvePayment(
            @RequestBody request: PaymentApproveRequest
    ): PaymentApproveResponse {
        return paymentUseCase.approvePayment(request)
    }
}
