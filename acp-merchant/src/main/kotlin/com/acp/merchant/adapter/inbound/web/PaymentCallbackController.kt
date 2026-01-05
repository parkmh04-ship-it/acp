package com.acp.merchant.adapter.inbound.web

import com.acp.merchant.application.port.input.CheckoutUseCase
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api/v1/payments")
class PaymentCallbackController(
    private val checkoutUseCase: CheckoutUseCase
) {

    @GetMapping("/success")
    suspend fun success(
        @RequestParam("session_id") sessionId: String,
        @RequestParam("pg_token") pgToken: String
    ): String {
        checkoutUseCase.confirmPayment(sessionId, pgToken)
        return "redirect:/api/v1/payments/completed?session_id=$sessionId"
    }

    @GetMapping("/completed")
    @ResponseBody
    suspend fun completed(@RequestParam("session_id") sessionId: String): String {
        return "Order completed successfully! Session ID: $sessionId"
    }

    @GetMapping("/cancel")
    @ResponseBody
    suspend fun cancel(@RequestParam("session_id") sessionId: String): String {
        return "Payment canceled for session: $sessionId"
    }

    @GetMapping("/fail")
    @ResponseBody
    suspend fun fail(@RequestParam("session_id") sessionId: String): String {
        return "Payment failed for session: $sessionId"
    }
}
