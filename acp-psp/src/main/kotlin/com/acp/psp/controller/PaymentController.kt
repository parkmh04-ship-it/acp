package com.acp.psp.controller

import com.acp.psp.service.PaymentService
import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/prepare")
    suspend fun preparePayment(@RequestBody request: PaymentPrepareRequest): PaymentPrepareResponse {
        return paymentService.preparePayment(request)
    }
}
